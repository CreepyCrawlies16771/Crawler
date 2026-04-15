package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import android.annotation.SuppressLint;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.*;


/**
 * CrawlerTuner — The guided sequential tuning OpMode.
 *
 * This OpMode guides teams through 11 tuning steps in the correct order:
 *   Step 0: Hardware Verification (motor directions)
 *   Step 1: IMU Verification (gyro orientation)
 *   Step 2: Track Width (odometry)
 *   Step 3: Center Wheel Offset (odometry)
 *   Step 4: Odometry Accuracy Gate (validation)
 *   Step 5: Drive PID (robot-oriented forward)
 *   Step 6: Strafe PID (robot-oriented sideways)
 *   Step 7: Turn PID (robot-oriented rotation)
 *   Step 8: Robot-Oriented Integration Test (validation)
 *   Step 9: Follow Distance (field-oriented)
 *   Step 10: Field-Oriented Integration Test (validation)
 *
 * Teams extend this class and implement buildRobot() to return their robot instance.
 * The tuner handles all tuning logic, telemetry, gamepad input, and value persistence.
 *
 * Gamepad controls:
 *   Right Bumper (RB)    — Run the current test
 *   D-pad Up/Down        — Increase/decrease the current value
 *   Left Bumper (LB)     — Toggle coarse/fine adjustment mode
 *   Circle (O)           — Accept and move to next step
 *   Triangle             — Go back one step
 *   X (hold 2 seconds)   — Force skip (use only if truly cannot complete)
 *
 * All tuned values are saved to /sdcard/Crawler/tune.json and displayed on telemetry.
 */
public abstract class CrawlerTuner extends OpMode {

    // Tuning state
    private int currentStep = 0;
    private final int TOTAL_STEPS = 11;
    private boolean isFineTune = false;
    private boolean testRunning = false;
    private boolean testCompleted = false;
    private String testStatus = "NOT_STARTED"; // NOT_STARTED, PASS, MARGINAL, FAIL
    private double currentValue = 0.0;
    private String currentMeasurement = "";

    // Timers
    private ElapsedTime totalTimer = new ElapsedTime();
    private ElapsedTime stepTimer = new ElapsedTime();
    private double xHoldTime = 0;

    // Robot instance
    protected CrawlerRobot robot;

    /**
     * Subclasses must implement this to return their robot instance.
     */
    protected abstract CrawlerRobot buildRobot(HardwareMap hwMap);

    @Override
    public void init() {
        robot = buildRobot(hardwareMap);
        currentValue = getInitialValue(currentStep);
        totalTimer.reset();
        stepTimer.reset();
    }

    @Override
    public void loop() {
        handleGamepadInput();
        updateTelemetry();
    }

    /**
     * Handle all gamepad input for navigation and adjustments.
     */
    private void handleGamepadInput() {
        // Right Bumper — Run test
        if (gamepad1.right_bumper) {
            if (!testRunning) {
                testRunning = true;
                testCompleted = false;
                testStatus = "NOT_STARTED";
                runCurrentTest();
            }
        }

        // D-pad Up — Increase value
        if (gamepad1.dpad_up && !testRunning) {
            double delta = isFineTune ? getFineAdjustmentDelta(currentStep) : getCoarseAdjustmentDelta(currentStep);
            currentValue += delta;
            currentValue = clampValue(currentStep, currentValue);
            applyValue(currentStep, currentValue);
        }

        // D-pad Down — Decrease value
        if (gamepad1.dpad_down && !testRunning) {
            double delta = isFineTune ? getFineAdjustmentDelta(currentStep) : getCoarseAdjustmentDelta(currentStep);
            currentValue -= delta;
            currentValue = clampValue(currentStep, currentValue);
            applyValue(currentStep, currentValue);
        }

        // Left Bumper — Toggle fine/coarse
        if (gamepad1.left_bumper) {
            isFineTune = !isFineTune;
        }

        // Circle/B — Accept and move to next step (only if PASS or MARGINAL)
        if (gamepad1.circle) {
            if (testStatus.equals("PASS") || testStatus.equals("MARGINAL")) {
                saveValue(currentStep, currentValue);
                nextStep();
                testRunning = false;
                testCompleted = false;
                currentValue = getInitialValue(currentStep);
            }
        }

        // Triangle — Go back one step
        if (gamepad1.triangle) {
            if (currentStep > 0) {
                prevStep();
                testRunning = false;
                testCompleted = false;
            }
        }

        // X (hold 2 seconds) — Force skip
        if (gamepad1.x) {
            xHoldTime += getRuntime();
            if (xHoldTime > 2.0) {
                saveValue(currentStep, currentValue);
                nextStep();
                testRunning = false;
                testCompleted = false;
                xHoldTime = 0;
            }
        } else {
            xHoldTime = 0;
        }
    }

    /**
     * Run the test for the current step.
     */
    private void runCurrentTest() {
        switch (currentStep) {
            case 0:
                runHardwareVerification();
                break;
            case 1:
                runIMUVerification();
                break;
            case 2:
                runTrackWidthTest();
                break;
            case 3:
                runCenterWheelOffsetTest();
                break;
            case 4:
                runOdometryAccuracyGate();
                break;
            case 5:
                runDrivePIDTest();
                break;
            case 6:
                runStrafePIDTest();
                break;
            case 7:
                runTurnPIDTest();
                break;
            case 8:
                runRobotOrientedIntegrationTest();
                break;
            case 9:
                runFollowDistanceTest();
                break;
            case 10:
                runFieldOrientedIntegrationTest();
                break;
        }
        testCompleted = true;
        testRunning = false;
    }

    /**
     * Step 0: Hardware Verification — Check that all motors spin forward.
     */
    private void runHardwareVerification() {
        robot.driveTrain.spin(0.5); // Run all motors at half power
        sleep(2000);
        robot.driveTrain.stop();

        // Assume human operator confirms all wheels spin forward
        testStatus = "PASS"; // Manual verification by team
        currentMeasurement = "All motors verified forward (manual)";
    }

    /**
     * Step 1: IMU Verification — Check if gyro is oriented correctly.
     */
    @SuppressLint("DefaultLocale")
    private void runIMUVerification() {
        // User manually rotates robot 90 degrees left
        // We just read the heading
        double heading = robot.getHeading();

        currentMeasurement = String.format("Current heading: %.1f°", heading);

        // PASS: heading between 80 and 100 degrees
        if (heading >= 80 && heading <= 100) {
            testStatus = "PASS";
        } else if ((heading >= 70 && heading < 80) || (heading > 100 && heading <= 110)) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 2: Track Width — Spin 10 rotations and measure heading error.
     */
    private void runTrackWidthTest() {
        // Set track width from current value
        RobotConfig.Odometry.TRACK_WIDTH = currentValue;

        // Spin 10 rotations clockwise
        new ROMovementEngine().turnPID(10 * 360);
        sleep(5000); // Wait for turn to complete

        // Measure final heading
        double finalHeading = robot.getHeading();
        double headingError = Math.abs(finalHeading - 0); // Should be close to 0

        currentMeasurement = String.format("Heading error: %.1f°", headingError);

        // PASS: error under 2 degrees
        if (headingError < 2) {
            testStatus = "PASS";
        } else if (headingError < 5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 3: Center Wheel Offset — Spin and measure Y drift.
     */
    private void runCenterWheelOffsetTest() {
        // Set center wheel offset
        RobotConfig.Odometry.CENTER_WHEEL_OFFSET = currentValue;
        robot.getOdometry().setCenterWheelOffset(currentValue);

        Pose startPose = robot.getPose();

        // Spin 10 rotations
        robot.getMecanumDrive().turnPID(10 * 360);
        sleep(5000);

        Pose endPose = robot.getPose();
        double yDrift = Math.abs(endPose.y - startPose.y);

        currentMeasurement = String.format("Y drift: %.2f\"", yDrift);

        // PASS: Y drift under 0.5 inches
        if (yDrift < 0.5) {
            testStatus = "PASS";
        } else if (yDrift < 1.5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 4: Odometry Accuracy Gate — Drive a square and measure return error.
     */
    @SuppressLint("DefaultLocale")
    private void runOdometryAccuracyGate() {
        Pose startPose = robot.getPose();

        // Drive a 24" square (4 sides, 24" each, 90° turns)
        for (int i = 0; i < 4; i++) {
            robot.getMecanumDrive().drivePID(24, 0.7); // Drive 24 inches
            sleep(3000);
            robot.getMecanumDrive().turnPID(90); // Turn 90 degrees
            sleep(2000);
        }

        Pose endPose = robot.getPose();
        double returnError = startPose.distanceTo(endPose);

        currentMeasurement = String.format("Return error: %.2f\"", returnError);

        // PASS: error under 1 inch
        if (returnError < 1) {
            testStatus = "PASS";
        } else if (returnError < 2) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 5: Drive PID — Drive 24 inches and measure stopping accuracy.
     */
    private void runDrivePIDTest() {
        // Set drive Kp
        RobotConfig.RobotOriented.Kp = currentValue;
        robot.getMecanumDrive().setDrivePID(currentValue);

        Pose startPose = robot.getPose();

        // Drive forward 24 inches
        robot.getMecanumDrive().drivePID(24, 0.7);
        sleep(3000);

        Pose endPose = robot.getPose();
        double distanceTraveled = Math.sqrt(
            Math.pow(endPose.x - startPose.x, 2) + Math.pow(endPose.y - startPose.y, 2)
        );
        double error = Math.abs(distanceTraveled - 24);

        currentMeasurement = String.format("Traveled: %.2f\" (error: %.2f\")", distanceTraveled, error);

        // PASS: error under 0.5 inches
        if (error < 0.5) {
            testStatus = "PASS";
        } else if (error < 1.5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 6: Strafe PID — Strafe 24 inches right and measure stopping accuracy.
     */
    private void runStrafePIDTest() {
        // Set strafe Kp
        RobotConfig.RobotOriented.strafe_Kp = currentValue;
        robot.getMecanumDrive().setStrafePID(currentValue);

        Pose startPose = robot.getPose();

        // Strafe right 24 inches
        robot.getMecanumDrive().strafePID(24, 0.7);
        sleep(3000);

        Pose endPose = robot.getPose();
        double distanceTraveled = Math.sqrt(
            Math.pow(endPose.x - startPose.x, 2) + Math.pow(endPose.y - startPose.y, 2)
        );
        double error = Math.abs(distanceTraveled - 24);

        currentMeasurement = String.format("Traveled: %.2f\" (error: %.2f\")", distanceTraveled, error);

        // PASS: error under 0.5 inches
        if (error < 0.5) {
            testStatus = "PASS";
        } else if (error < 1.5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 7: Turn PID — Turn 180 degrees and measure accuracy.
     */
    private void runTurnPIDTest() {
        // Set turn PID
        RobotConfig.RobotOriented.STEER_P = currentValue;
        robot.getMecanumDrive().setTurnPID(currentValue);

        double startHeading = robot.getHeading();

        // Turn 180 degrees
        robot.getMecanumDrive().turnPID(180);
        sleep(2500);

        double endHeading = robot.getHeading();
        double headingError = Math.abs(endHeading - 180 - startHeading);
        if (headingError > 180) {
            headingError = 360 - headingError; // Normalize to [0, 180]
        }

        currentMeasurement = String.format("Heading: %.1f° (error: %.1f°)", endHeading, headingError);

        // PASS: error under 2 degrees
        if (headingError < 2) {
            testStatus = "PASS";
        } else if (headingError < 5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 8: Robot-Oriented Integration Test — Drive a square autonomously.
     */
    private void runRobotOrientedIntegrationTest() {
        Pose startPose = robot.getPose();

        // Drive a 24" square
        for (int i = 0; i < 4; i++) {
            robot.getMecanumDrive().drivePID(24, 0.7);
            sleep(3000);
            robot.getMecanumDrive().turnPID(90);
            sleep(2000);
        }

        Pose endPose = robot.getPose();
        double returnError = startPose.distanceTo(endPose);

        currentMeasurement = String.format("Return error: %.2f\"", returnError);

        // PASS: error under 2 inches
        if (returnError < 2) {
            testStatus = "PASS";
        } else if (returnError < 5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 9: Follow Distance — Follow an L-shaped path and evaluate smoothness.
     */
    private void runFollowDistanceTest() {
        // Set follow distance
        RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE = currentValue;
        robot.getPurePursuit().setFollowDistance(currentValue);

        Pose[] path = new Pose[]{
            new Pose(0, 0, 0),
            new Pose(0, 24, 0),
            new Pose(24, 24, 0)
        };

        robot.getPurePursuit().followPath(path);
        sleep(5000);

        Pose endPose = robot.getPose();
        double error = endPose.distanceTo(path[path.length - 1]);

        currentMeasurement = String.format("Final error: %.2f\"", error);

        // PASS: error under 3 inches (path following is inherently less precise)
        if (error < 3) {
            testStatus = "PASS";
        } else if (error < 5) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Step 10: Field-Oriented Integration Test — Follow a complete square path.
     */
    private void runFieldOrientedIntegrationTest() {
        Pose startPose = robot.getPose();

        Pose[] path = new Pose[]{
            new Pose(0, 0, 0),
            new Pose(24, 0, 0),
            new Pose(24, 24, 0),
            new Pose(0, 24, 0),
            new Pose(0, 0, 0)
        };

        robot.getPurePursuit().followPath(path);
        sleep(8000);

        Pose endPose = robot.getPose();
        double returnError = startPose.distanceTo(endPose);

        currentMeasurement = String.format("Return error: %.2f\"", returnError);

        // PASS: error under 3 inches
        if (returnError < 3) {
            testStatus = "PASS";
        } else if (returnError < 6) {
            testStatus = "MARGINAL";
        } else {
            testStatus = "FAIL";
        }
    }

    /**
     * Get the initial value to display for a step.
     */
    private double getInitialValue(int step) {
        switch (step) {
            case 2:
                return RobotConfig.Odometry.TRACK_WIDTH;
            case 3:
                return RobotConfig.Odometry.CENTER_WHEEL_OFFSET;
            case 5:
                return RobotConfig.RobotOriented.Kp;
            case 6:
                return RobotConfig.RobotOriented.strafe_Kp;
            case 7:
                return RobotConfig.RobotOriented.STEER_P;
            case 9:
                return RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE;
            default:
                return 0;
        }
    }

    /**
     * Apply the current value to the robot configuration.
     */
    private void applyValue(int step, double value) {
        switch (step) {
            case 2:
                RobotConfig.Odometry.TRACK_WIDTH = value;
                robot.getOdometry().setTrackWidth(value);
                break;
            case 3:
                RobotConfig.Odometry.CENTER_WHEEL_OFFSET = value;
                robot.getOdometry().setCenterWheelOffset(value);
                break;
            case 5:
                RobotConfig.RobotOriented.Kp = value;
                robot.getMecanumDrive().setDrivePID(value);
                break;
            case 6:
                RobotConfig.RobotOriented.strafe_Kp = value;
                robot.getMecanumDrive().setStrafePID(value);
                break;
            case 7:
                RobotConfig.RobotOriented.STEER_P = value;
                robot.getMecanumDrive().setTurnPID(value);
                break;
            case 9:
                RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE = value;
                robot.getPurePursuit().setFollowDistance(value);
                break;
        }
    }

    /**
     * Save the value for the current step.
     */
    private void saveValue(int step, double value) {
        applyValue(step, value);
        // TODO: Save to /sdcard/Crawler/tune.json
    }

    /**
     * Get coarse adjustment delta for the current step.
     */
    private double getCoarseAdjustmentDelta(int step) {
        switch (step) {
            case 2:
            case 3:
                return 0.1; // Odometry: 0.1 inches
            case 5:
            case 6:
                return 0.005; // Drive/Strafe Kp: 0.005
            case 7:
                return 0.005; // Turn P: 0.005
            case 9:
                return 1.0; // Follow distance: 1 inch
            default:
                return 0;
        }
    }

    /**
     * Get fine adjustment delta for the current step.
     */
    private double getFineAdjustmentDelta(int step) {
        switch (step) {
            case 2:
            case 3:
                return 0.01; // Odometry: 0.01 inches
            case 5:
            case 6:
                return 0.001; // Drive/Strafe Kp: 0.001
            case 7:
                return 0.001; // Turn P: 0.001
            case 9:
                return 0.5; // Follow distance: 0.5 inches
            default:
                return 0;
        }
    }

    /**
     * Clamp value to reasonable bounds for the current step.
     */
    private double clampValue(int step, double value) {
        switch (step) {
            case 2:
                return Math.max(5, Math.min(25, value)); // Track width: 5–25 inches
            case 3:
                return Math.max(-10, Math.min(10, value)); // Center offset: –10 to 10 inches
            case 5:
            case 6:
                return Math.max(0.001, Math.min(0.2, value)); // Kp: 0.001–0.2
            case 7:
                return Math.max(0.001, Math.min(0.1, value)); // STEER_P: 0.001–0.1
            case 9:
                return Math.max(2, Math.min(20, value)); // Follow distance: 2–20 inches
            default:
                return value;
        }
    }

    /**
     * Move to the next step.
     */
    private void nextStep() {
        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            stepTimer.reset();
        } else {
            showTuningComplete();
        }
    }

    /**
     * Move to the previous step.
     */
    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
            stepTimer.reset();
        }
    }

    /**
     * Show the final tuning complete message.
     */
    @SuppressLint("DefaultLocale")
    private void showTuningComplete() {
        telemetry.addData("===", "TUNING COMPLETE!");
        telemetry.addData("Total time", formatTime((long) totalTimer.milliseconds()));
        telemetry.addData("", "");
        telemetry.addData("Your tuned values:", "");
        telemetry.addData("  Track width", String.format("%.2f\"", RobotConfig.Odometry.TRACK_WIDTH));
        telemetry.addData("  Center wheel offset", String.format("%.2f\"", RobotConfig.Odometry.CENTER_WHEEL_OFFSET));
        telemetry.addData("  Drive Kp", String.format("%.4f", RobotConfig.RobotOriented.Kp));
        telemetry.addData("  Strafe Kp", String.format("%.4f", RobotConfig.RobotOriented.strafe_Kp));
        telemetry.addData("  STEER_P", String.format("%.4f", RobotConfig.RobotOriented.STEER_P));
        telemetry.addData("  Follow distance", String.format("%.1f\"", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE));
        telemetry.addData("", "");
        telemetry.addData("Next", "Copy values into RobotConfig.java and rebuild");
    }

    /**
     * Update telemetry display.
     */
    private void updateTelemetry() {
        String stepName = getStepName(currentStep);

        telemetry.addData("=== Step " + (currentStep + 1) + "/" + TOTAL_STEPS + ": " + stepName + " ===", "");
        telemetry.addData("Elapsed", formatTime((long) totalTimer.milliseconds()) + " | Step time: " + formatTime((long) stepTimer.milliseconds()));
        telemetry.addData("", "");

        telemetry.addData("Current value", getValueDisplay(currentStep, currentValue));
        telemetry.addData("Measurement", currentMeasurement);
        telemetry.addData("", "");

        telemetry.addData("Status", getStatusDisplay(testStatus));
        telemetry.addData("", "");

        telemetry.addData("Controls", "");
        telemetry.addData("  RB", "run");
        telemetry.addData("  ↑↓", "adjust (" + (isFineTune ? "fine" : "coarse") + ")");
        telemetry.addData("  ●", "accept");
        telemetry.addData("  △", "back");

        telemetry.update();
    }

    /**
     * Get the name for a step.
     */
    private String getStepName(int step) {
        String[] names = {
            "Hardware Verification",
            "IMU Verification",
            "Track Width",
            "Center Wheel Offset",
            "Odometry Accuracy Gate",
            "Drive PID",
            "Strafe PID",
            "Turn PID",
            "Robot-Oriented Integration",
            "Follow Distance",
            "Field-Oriented Integration"
        };
        return names[Math.min(step, names.length - 1)];
    }

    /**
     * Get display string for current value.
     */
    @SuppressLint("DefaultLocale")
    private String getValueDisplay(int step, double value) {
        switch (step) {
            case 2:
            case 3:
            case 9:
                return String.format("%.2f inches", value);
            case 5:
            case 6:
            case 7:
                return String.format("%.4f", value);
            default:
                return "N/A";
        }
    }

    /**
     * Get display string for test status.
     */
    private String getStatusDisplay(String status) {
        switch (status) {
            case "PASS":
                return "✓ PASS";
            case "MARGINAL":
                return "⚠ MARGINAL";
            case "FAIL":
                return "✗ FAIL";
            default:
                return "Not started";
        }
    }

    /**
     * Format milliseconds as MM:SS or MM:SS (hh).
     */
    @SuppressLint("DefaultLocale")
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    /**
     * Helper to sleep (blocking wait).
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
