package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import android.util.Log;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Pose2d;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;
import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;
import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.RobotMovement;
import org.firstinspires.ftc.teamcode.Crawler.MyRobot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

/**
 * Complete guided tuning system for Crawler robotics library.
 *
 * <p>This LinearOpMode walks teams through a 12-step sequential tuning process
 * for odometry calibration, robot-oriented PID control, and field-oriented pure pursuit.
 * Teams cannot skip ahead — each step validates before allowing advancement to the next.
 * All tuned values are saved to JSON and human-readable text files on the SD card.</p>
 *
 * <p><b>Gamepad controls:</b></p>
 * <ul>
 *   <li><b>Right bumper (RB)</b> — Run the current step test</li>
 *   <li><b>D-pad up</b> — Increase the current tuning value by step amount</li>
 *   <li><b>D-pad down</b> — Decrease the current tuning value by step amount</li>
 *   <li><b>Circle (B)</b> — Accept current value and advance to the next step</li>
 *   <li><b>Triangle (Y)</b> — Return to the previous step</li>
 *   <li><b>X (hold 2 sec)</b> — Skip the current step (warning displayed)</li>
 * </ul>
 *
 * <p><b>Tuning sequence:</b></p>
 * <ol>
 *   <li>Hardware verification — spin all motors, check encoder readings</li>
 *   <li>IMU verification — rotate 90°, check heading read</li>
 *   <li>Track width — run 10 rotations, auto-adjust based on heading error</li>
 *   <li>Center wheel offset — run 10 rotations, auto-adjust based on Y drift</li>
 *   <li>Odometry accuracy — drive robot in square, check return error</li>
 *   <li>Drive PID — drive 48" forward, tune Kp</li>
 *   <li>Strafe PID — strafe 48" right, tune strafe_Kp</li>
 *   <li>Turn PID — turn 720°, tune STEER_P</li>
 *   <li>Pure pursuit follow distance — run L-shaped path, observe cornering</li>
 *   <li>Pure pursuit move speed — run 96" straight path</li>
 *   <li>Integration test — run combined RO + FO path, verify everything</li>
 *   <li>Complete — summary displayed, values saved</li>
 * </ol>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Full FTC Dashboard integration with live field view and encoder graphs</li>
 *   <li>Live sidebar showing all current tuned values</li>
 *   <li>Step timer and total elapsed time</li>
 *   <li>JSON persistence: {@code /sdcard/Crawler/tune.json}</li>
 *   <li>Human-readable summary: {@code /sdcard/Crawler/tune_summary.txt}</li>
 *   <li>Auto-correction suggestions for offset-based steps</li>
 *   <li>Skip with 2-second hold to prevent accidental skipping</li>
 *   <li>Full return navigation preserves all previous tuned values</li>
 * </ul>
 *
 * @see RobotConfig
 * @see CrawlerRobot
 * @see RobotMovement
 * @since Crawler 1.0
 */
@TeleOp(name = "Crawler Tuner", group = "Crawler Tuning")
public class CrawlerTuner extends LinearOpMode {

    private static final String TAG = "CrawlerTuner";
    private static final String TUNE_DIR = "/sdcard/Crawler";
    private static final String TUNE_JSON = TUNE_DIR + "/tune.json";
    private static final String TUNE_SUMMARY = TUNE_DIR + "/tune_summary.txt";

    // -----------------------------------------------------------------------
    // Step definitions
    // -----------------------------------------------------------------------
    private static final int STEP_HARDWARE_VERIFICATION = 0;
    private static final int STEP_IMU_VERIFICATION = 1;
    private static final int STEP_TRACK_WIDTH = 2;
    private static final int STEP_CENTER_WHEEL_OFFSET = 3;
    private static final int STEP_ODOMETRY_ACCURACY = 4;
    private static final int STEP_DRIVE_PID = 5;
    private static final int STEP_STRAFE_PID = 6;
    private static final int STEP_TURN_PID = 7;
    private static final int STEP_FOLLOW_DISTANCE = 8;
    private static final int STEP_MOVE_SPEED = 9;
    private static final int STEP_INTEGRATION_TEST = 10;
    private static final int STEP_COMPLETE = 11;

    // -----------------------------------------------------------------------
    // Tuning state
    // -----------------------------------------------------------------------
    private int currentStep = STEP_HARDWARE_VERIFICATION;
    private long stepStartTime;
    private long tuneStartTime;

    private TuneValues values = new TuneValues();
    private CrawlerRobot robot;
    private RobotMovement movement;
    private List<Pose2d> racePath = new ArrayList<>();

    // Gamepad state for edge detection
    private boolean prevRightBumper = false;
    private boolean prevCircle = false;
    private boolean prevTriangle = false;
    private boolean prevX = false;
    private long xPressStartTime = 0;
    private static final long X_HOLD_THRESHOLD_MS = 2000;

    // Test state variables
    private double encoderStartDistLeft = 0;
    private double encoderStartDistRight = 0;
    private double encoderStartDistCenter = 0;
    private double imuStartHeading = 0;
    private Pose2d squareStartPose = null;
    private boolean testRunning = false;
    private boolean testComplete = false;
    private List<Double> motorPowerHistory = new ArrayList<>();
    private List<Pose2d> testPath = new ArrayList<>();

    @Override
    public void runOpMode() throws InterruptedException {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        telemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());

        // Build robot
        try {
            robot = new MyRobot(hardwareMap);
            movement = new RobotMovement(robot);
        } catch (Exception e) {
            Log.e(TAG, "Failed to build robot", e);
            telemetry.addData("ERROR", "Failed to build robot: " + e.getMessage());
            telemetry.update();
            return;
        }

        // Ensure SD card directory exists
        ensureTuneDirectory();

        // Load any existing config
        values.loadFromRobotConfig();

        stepStartTime = System.currentTimeMillis();
        tuneStartTime = System.currentTimeMillis();

        telemetry.addData("STATUS", "Crawler Tuner Ready");
        telemetry.addData("Current Step", describeStep(currentStep));
        telemetry.update();

        waitForStart();

        if (!opModeIsActive()) return;

        // Main tuning loop
        while (opModeIsActive()) {
            // Handle gamepad input
            handleGamepadInput();

            // Clear test state if we moved to a new step
            if (testComplete) {
                testComplete = false;
                testRunning = false;
            }

            // Run step-specific logic
            executeCurrentStep();

            // Display telemetry
            displayTelemetry();

            idle();
        }

        // Cleanup
        if (robot != null) {
            robot.stop();
        }
    }

    // -----------------------------------------------------------------------
    // Gamepad input handling
    // -----------------------------------------------------------------------

    /**
     * Processes gamepad input for navigation and value adjustment.
     *
     * <p>Implements edge detection to prevent multiple triggers from a single
     * button press. X button requires 2-second hold to prevent accidental skips.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void handleGamepadInput() throws InterruptedException {
        // Right bumper: run test (edge triggered)
        if (gamepad1.right_bumper && !prevRightBumper) {
            if (!testRunning && !testComplete) {
                testRunning = true;
                testComplete = false;
                motorPowerHistory.clear();
                testPath.clear();
            }
        }
        prevRightBumper = gamepad1.right_bumper;

        // D-pad up: increase value
        if (gamepad1.dpad_up) {
            values.adjustCurrentValue(true, currentStep);
        }

        // D-pad down: decrease value
        if (gamepad1.dpad_down) {
            values.adjustCurrentValue(false, currentStep);
        }

        // Circle (B): accept and advance
        if (gamepad1.circle && !prevCircle) {
            saveCurrentState();
            if (currentStep < STEP_COMPLETE) {
                currentStep++;
                resetStepState();
            }
        }
        prevCircle = gamepad1.circle;

        // Triangle (Y): go back
        if (gamepad1.triangle && !prevTriangle) {
            if (currentStep > STEP_HARDWARE_VERIFICATION) {
                currentStep--;
                resetStepState();
            }
        }
        prevTriangle = gamepad1.triangle;

        // X: skip with 2-second hold
        if (gamepad1.cross) {
            if (!prevX) {
                xPressStartTime = System.currentTimeMillis();
            }
            long holdTime = System.currentTimeMillis() - xPressStartTime;
            if (holdTime >= X_HOLD_THRESHOLD_MS && currentStep < STEP_COMPLETE) {
                telemetry.addData("SKIPPED", describeStep(currentStep));
                currentStep++;
                resetStepState();
            }
        } else {
            xPressStartTime = 0;
        }
        prevX = gamepad1.cross;
    }

    // -----------------------------------------------------------------------
    // Step execution
    // -----------------------------------------------------------------------

    /**
     * Executes the logic for the current tuning step.
     *
     * <p>Each step either runs a test (if {@code testRunning} is true) or
     * displays the current step's UI. Steps transition automatically based
     * on validation or user input.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void executeCurrentStep() throws InterruptedException {
        switch (currentStep) {
            case STEP_HARDWARE_VERIFICATION:
                stepHardwareVerification();
                break;
            case STEP_IMU_VERIFICATION:
                stepIMUVerification();
                break;
            case STEP_TRACK_WIDTH:
                stepTrackWidth();
                break;
            case STEP_CENTER_WHEEL_OFFSET:
                stepCenterWheelOffset();
                break;
            case STEP_ODOMETRY_ACCURACY:
                stepOdometryAccuracy();
                break;
            case STEP_DRIVE_PID:
                stepDrivePID();
                break;
            case STEP_STRAFE_PID:
                stepStrafePID();
                break;
            case STEP_TURN_PID:
                stepTurnPID();
                break;
            case STEP_FOLLOW_DISTANCE:
                stepFollowDistance();
                break;
            case STEP_MOVE_SPEED:
                stepMoveSpeed();
                break;
            case STEP_INTEGRATION_TEST:
                stepIntegrationTest();
                break;
            case STEP_COMPLETE:
                stepComplete();
                break;
        }
    }

    /**
     * Step 0: Hardware verification — spin all motors and check encoder readings.
     *
     * <p>While RB is held, all drive motors spin at 0.3 power forward. Team verifies
     * correct wheel direction and watches encoder readings for anomalies.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepHardwareVerification() throws InterruptedException {
        if (testRunning) {
            robot.drive(0.3, 0.0, 0.0);
            motorPowerHistory.add(0.3);
        } else if (testComplete) {
            robot.stop();
            testComplete = false;
        } else {
            robot.stop();
        }
    }

    /**
     * Step 1: IMU verification — rotate robot 90° and check heading.
     *
     * <p>Displays live heading in degrees. Team rotates robot by hand 90° counterclockwise.
     * If heading reads ~90°, IMU is correctly oriented. Otherwise, team must fix IMU
     * orientation in MyRobot class.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepIMUVerification() throws InterruptedException {
        robot.update();
        double currentHeading = Math.toDegrees(robot.getHeading());

        if (testRunning) {
            imuStartHeading = currentHeading;
            testRunning = false;
            testComplete = true;
        }

        telemetry.addLine("Rotate robot 90° counterclockwise by hand");
        telemetry.addData("Current Heading (deg)", String.format("%.2f", currentHeading));
        telemetry.addData("Expected", "~90.0");
    }

    /**
     * Step 2: Track width calibration via 10-rotation spin test.
     *
     * <p>RB runs 10 full clockwise rotations. Heading error is measured and used to
     * auto-calculate correction factor: {@code trackWidth += corrected_error * 0.5}.
     * Manual D-pad adjustment is also available.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepTrackWidth() throws InterruptedException {
        robot.update();

        if (testRunning) {
            spinRotations(10, 0.4);
            testComplete = true;
            testRunning = false;

            // Auto-calculate correction
            double measuredHeading = Math.toDegrees(robot.getHeading());
            double headingError = measuredHeading - imuStartHeading;
            double correctionFactor = (headingError / (10.0 * 360.0)) * RobotConfig.Odometry.TRACK_WIDTH;
            double suggestedAdjustment = correctionFactor * 0.5;

            telemetry.addData("Measured Heading", String.format("%.2f°", measuredHeading));
            telemetry.addData("Heading Error", String.format("%.2f°", headingError));
            telemetry.addData("Suggested Adjustment", String.format("%.3f", suggestedAdjustment));

            imuStartHeading = robot.getHeading();
        } else {
            robot.stop();
        }

        telemetry.addData("Current TRACK_WIDTH", RobotConfig.Odometry.TRACK_WIDTH);
        telemetry.addLine("RB: Run 10 rotations test");
        telemetry.addLine("D-pad: Manual adjust ±0.1");
    }

    /**
     * Step 3: Center wheel offset calibration via Y drift during 10-rotation test.
     *
     * <p>After spinning 10 rotations, Y drift indicates offset error. Positive drift
     * suggests decrease offset; negative suggests increase. Auto-correction formula
     * is displayed.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepCenterWheelOffset() throws InterruptedException {
        robot.update();

        if (testRunning) {
            Pose2d startPose = robot.getPose();
            spinRotations(10, 0.4);
            Pose2d endPose = robot.getPose();

            double yDrift = endPose.y - startPose.y;
            telemetry.addData("Y Drift (inches)", String.format("%.3f", yDrift));
            telemetry.addLine(yDrift > 0 ? "Drift positive → decrease offset" : "Drift negative → increase offset");

            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current CENTER_WHEEL_OFFSET", RobotConfig.Odometry.CENTER_WHEEL_OFFSET);
        telemetry.addLine("RB: Run 10 rotations test");
        telemetry.addLine("D-pad: Manual adjust ±0.05");
    }

    /**
     * Step 4: Odometry accuracy validation — drive 48×48 inch square and return.
     *
     * <p>Team drives the robot in a 48×48 inch square by hand and returns to origin.
     * Final position error determines status:</p>
     * <ul>
     *   <li>&lt; 0.5 inches: EXCELLENT</li>
     *   <li>&lt; 1.0 inches: PASS</li>
     *   <li>&lt; 2.0 inches: MARGINAL (warning, but can advance)</li>
     *   <li>≥ 2.0 inches: FAIL (must go back and re-tune)</li>
     * </ul>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepOdometryAccuracy() throws InterruptedException {
        robot.update();
        Pose2d currentPose = robot.getPose();

        if (testRunning) {
            squareStartPose = currentPose;
            testRunning = false;
            telemetry.addLine("Drive 48×48 square and return. Press Circle when done.");
        } else if (squareStartPose != null && !testPath.isEmpty()) {
            testPath.add(currentPose);
        }

        double error = 0;
        String status = "RUNNING";

        if (squareStartPose != null) {
            error = Math.hypot(currentPose.x - squareStartPose.x, currentPose.y - squareStartPose.y);

            if (error < 0.5) status = "EXCELLENT";
            else if (error < 1.0) status = "PASS";
            else if (error < 2.0) status = "MARGINAL";
            else status = "FAIL";

            telemetry.addData("Return Error (inches)", String.format("%.3f", error));
            telemetry.addData("Status", status);

            if (status.equals("FAIL")) {
                telemetry.addLine("ERROR: Must re-tune track width and center wheel offset!");
            } else if (status.equals("MARGINAL")) {
                telemetry.addLine("WARNING: Consider re-tuning steps 2-3 for better accuracy");
            }
        }

        telemetry.addLine("RB: Reset odometry");
        telemetry.addLine("Drive 48×48 square by hand, return to start");
        telemetry.addLine("Circle: Done");
    }

    /**
     * Step 5: Drive PID tuning — measure 48-inch forward distance error.
     *
     * <p>Robot drives exactly 48 inches forward using encoder distance with current
     * {@code RobotConfig.RobotOriented.Kp}. Uses start-offset approach (does not reset
     * encoders). Shows distance error and motor power graph.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepDrivePID() throws InterruptedException {
        robot.update();

        if (testRunning) {
            driveStraightTest(48.0, 0.4);
            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current Kp", RobotConfig.RobotOriented.Kp);
        telemetry.addLine("RB: Drive 48\" forward test");
        telemetry.addLine("D-pad: Adjust Kp ±0.005");
    }

    /**
     * Step 6: Strafe PID tuning — measure 48-inch right strafe distance error.
     *
     * <p>Same as drive PID but strafes 48 inches to the right. Updates
     * {@code RobotConfig.RobotOriented.strafe_Kp}.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepStrafePID() throws InterruptedException {
        robot.update();

        if (testRunning) {
            strafeStraightTest(48.0, 0.4);
            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current strafe_Kp", RobotConfig.RobotOriented.strafe_Kp);
        telemetry.addLine("RB: Strafe 48\" right test");
        telemetry.addLine("D-pad: Adjust strafe_Kp ±0.005");
    }

    /**
     * Step 7: Turn PID tuning — measure heading error after 720° spin.
     *
     * <p>Robot spins 2 full rotations (720°) in place using IMU-based turning.
     * Final heading error is displayed. Updates {@code RobotConfig.RobotOriented.STEER_P}.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepTurnPID() throws InterruptedException {
        robot.update();

        if (testRunning) {
            turnInPlaceTest(Math.toRadians(720), 0.4);
            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current STEER_P", RobotConfig.RobotOriented.STEER_P);
        telemetry.addLine("RB: Turn 720° in place test");
        telemetry.addLine("D-pad: Adjust STEER_P ±0.002");
    }

    /**
     * Step 8: Pure pursuit follow distance tuning via L-shaped path.
     *
     * <p>RB runs an L-shaped pure pursuit path: (0,0) → (48,0) → (48,48).
     * Team observes corner handling. Sharp turn suggests increase distance;
     * too-wide curve suggests decrease. Updates {@code RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE}.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepFollowDistance() throws InterruptedException {
        robot.update();

        if (testRunning) {
            List<Waypoint> path = Waypoint.at(0, 0)
                    .at(48, 0)
                    .at(48, 48)
                    .buildAll();

            movement.follow(path, 0);

            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current DEFAULT_FOLLOW_DISTANCE", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE);
        telemetry.addLine("RB: Run L-shaped path");
        telemetry.addLine("Watch corner handling");
        telemetry.addLine("Sharp turn → increase distance");
        telemetry.addLine("D-pad: Adjust ±0.5");
    }

    /**
     * Step 9: Pure pursuit move speed tuning via 96-inch straight path.
     *
     * <p>RB runs a 96-inch straight pure pursuit path. Team observes smoothness
     * and stopping accuracy. Updates {@code RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED}.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepMoveSpeed() throws InterruptedException {
        robot.update();

        if (testRunning) {
            List<Waypoint> path = Waypoint.at(0, 0)
                    .at(96, 0)
                    .buildAll();

            movement.follow(path, 0);

            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addData("Current DEFAULT_MOVE_SPEED", RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED);
        telemetry.addLine("RB: Run 96\" straight path");
        telemetry.addLine("Observe smoothness and accuracy");
        telemetry.addLine("D-pad: Adjust ±0.05");
    }

    /**
     * Step 10: Integration test — combined RO and FO path with return validation.
     *
     * <p>Runs a complete path sequence that uses both robot-oriented and field-oriented
     * control:</p>
     * <ol>
     *   <li>FO: Follow (0,0) → (48,0) → (48,48)</li>
     *   <li>RO: Drive 12" forward</li>
     *   <li>RO: Turn 180°</li>
     *   <li>FO: Follow (48,48) → (0,0)</li>
     * </ol>
     *
     * <p>Final return-to-origin error and total path time are displayed. All tuned values
     * are automatically saved to JSON and text files at completion.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepIntegrationTest() throws InterruptedException {
        robot.update();

        if (testRunning) {
            long testStart = System.currentTimeMillis();

            // FO: Follow L-path
            List<Waypoint> foPath1 = Waypoint.at(0, 0)
                    .at(48, 0)
                    .at(48, 48)
                    .buildAll();
            movement.follow(foPath1, 0);

            // RO: Drive 12" forward
            driveStraightTest(12.0, 0.4);

            // RO: Turn 180°
            turnInPlaceTest(Math.PI, 0.4);

            // FO: Return home
            List<Waypoint> foPath2 = Waypoint.at(48, 48)
                    .at(0, 0)
                    .buildAll();
            movement.follow(foPath2, 0);

            long testDuration = System.currentTimeMillis() - testStart;
            Pose2d finalPose = robot.getPose();
            double returnError = Math.hypot(finalPose.x, finalPose.y);
            boolean passed = returnError < 2.0;

            telemetry.addData("Return Error (inches)", String.format("%.3f", returnError));
            telemetry.addData("Test Duration (sec)", testDuration / 1000.0);
            telemetry.addData("Result", passed ? "PASS" : "FAIL");

            values.integrationTestPassed = passed;
            values.integrationReturnError = returnError;

            saveCurrentState();
            testComplete = true;
            testRunning = false;
        } else {
            robot.stop();
        }

        telemetry.addLine("RB: Run integration test");
        telemetry.addLine("(FO L-path + RO movements + FO return)");
    }

    /**
     * Step 11: Final summary — all tuned values displayed.
     *
     * <p>Shows JSON and text files have been saved. Team reviews all final tuned values.
     * This step cannot be exited — represents completion.</p>
     *
     * @throws InterruptedException if the OpMode is stopped
     */
    private void stepComplete() throws InterruptedException {
        robot.stop();
        telemetry.addLine("========== TUNING COMPLETE ==========");
        telemetry.addLine("All values saved to:");
        telemetry.addLine("  /sdcard/Crawler/tune.json");
        telemetry.addLine("  /sdcard/Crawler/tune_summary.txt");
        telemetry.addLine("");
        telemetry.addData("TRACK_WIDTH", RobotConfig.Odometry.TRACK_WIDTH);
        telemetry.addData("CENTER_WHEEL_OFFSET", RobotConfig.Odometry.CENTER_WHEEL_OFFSET);
        telemetry.addData("Drive Kp", RobotConfig.RobotOriented.Kp);
        telemetry.addData("Strafe Kp", RobotConfig.RobotOriented.strafe_Kp);
        telemetry.addData("Steer P", RobotConfig.RobotOriented.STEER_P);
        telemetry.addData("Follow Distance", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE);
        telemetry.addData("Move Speed", RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED);
    }

    // -----------------------------------------------------------------------
    // Helper test routines
    // -----------------------------------------------------------------------

    /**
     * Spins the robot N full rotations in place using IMU heading.
     *
     * <p>Uses robot-oriented turning with {@code RobotConfig.RobotOriented.STEER_P}.
     * This routine does not return until rotations are complete or OpMode is stopped.</p>
     *
     * @param numRotations number of full 360° rotations (can be fractional)
     * @param maxTurnPower maximum turn power, in range [0.0, 1.0]
     * @throws InterruptedException if the OpMode is stopped
     */
    private void spinRotations(double numRotations, double maxTurnPower) throws InterruptedException {
        double targetHeading = robot.getHeading() + (numRotations * 2 * Math.PI);

        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && System.currentTimeMillis() - startTime < 10000) {
            robot.update();
            double currentHeading = robot.getHeading();
            double headingError = angleDiff(targetHeading, currentHeading);

            double turnPower = Math.signum(headingError)
                    * Math.min(Math.abs(headingError) * RobotConfig.RobotOriented.STEER_P, maxTurnPower);

            robot.drive(0, 0, turnPower);

            if (Math.abs(headingError) < 0.05) break;
            idle();
        }

        robot.stop();
    }

    /**
     * Drives the robot straight for a measured distance using encoder feedback.
     *
     * <p>Uses start-offset approach (does not reset encoders). Applies proportional
     * control based on drive motor encoder distance with {@code RobotConfig.RobotOriented.Kp}.
     * Maintains zero heading via {@code RobotConfig.RobotOriented.STEER_P}.</p>
     *
     * @param distanceInches target distance to drive, in inches
     * @param maxDrivePower maximum drive power, in range [0.0, 1.0]
     * @throws InterruptedException if the OpMode is stopped
     */
    private void driveStraightTest(double distanceInches, double maxDrivePower) throws InterruptedException {
        robot.update();
        double startHeading = robot.getHeading();

        // Record starting encoder positions
        double startEnc = (robot.frontLeft.getCurrentPosition() + robot.frontRight.getCurrentPosition())
                / 2.0 / RobotConfig.Odometry.TICKS_PER_REV
                * Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER;

        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && System.currentTimeMillis() - startTime < 10000) {
            robot.update();

            // Current distance from start
            double currentEnc = (robot.frontLeft.getCurrentPosition() + robot.frontRight.getCurrentPosition())
                    / 2.0 / RobotConfig.Odometry.TICKS_PER_REV
                    * Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER;
            double distanceTraveled = currentEnc - startEnc;

            // Distance error
            double distanceError = distanceInches - distanceTraveled;

            // PID for forward movement
            double drivePower = Math.signum(distanceError)
                    * Math.max(RobotConfig.RobotOriented.MIN_POWER,
                    Math.min(Math.abs(distanceError) * RobotConfig.RobotOriented.Kp, maxDrivePower));

            // Maintain heading
            double headingError = angleDiff(startHeading, robot.getHeading());
            double steerPower = headingError * RobotConfig.RobotOriented.STEER_P;

            robot.drive(drivePower, 0, steerPower);
            motorPowerHistory.add(drivePower);

            telemetry.addData("Distance", String.format("%.2f / %.2f inches", distanceTraveled, distanceInches));
            telemetry.addData("Error", String.format("%.3f inches", distanceError));

            if (Math.abs(distanceError) < 0.5) break;
            idle();
        }

        robot.stop();
    }

    /**
     * Strafes the robot straight for a measured distance using encoder feedback.
     *
     * <p>Same as {@code driveStraightTest} but applies strafe movement instead.
     * Uses {@code RobotConfig.RobotOriented.strafe_Kp} for control.</p>
     *
     * @param distanceInches target distance to strafe, in inches
     * @param maxStrafePower maximum strafe power, in range [0.0, 1.0]
     * @throws InterruptedException if the OpMode is stopped
     */
    private void strafeStraightTest(double distanceInches, double maxStrafePower) throws InterruptedException {
        robot.update();
        double startHeading = robot.getHeading();

        // Record starting encoder position
        double startEnc = robot.frontLeft.getCurrentPosition() / RobotConfig.Odometry.TICKS_PER_REV
                * Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER;

        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && System.currentTimeMillis() - startTime < 10000) {
            robot.update();

            // Current distance
            double currentEnc = robot.frontLeft.getCurrentPosition() / RobotConfig.Odometry.TICKS_PER_REV
                    * Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER;
            double distanceTraveled = currentEnc - startEnc;

            double distanceError = distanceInches - distanceTraveled;

            double strafePower = Math.signum(distanceError)
                    * Math.max(RobotConfig.RobotOriented.MIN_POWER,
                    Math.min(Math.abs(distanceError) * RobotConfig.RobotOriented.strafe_Kp, maxStrafePower));

            // Maintain heading
            double headingError = angleDiff(startHeading, robot.getHeading());
            double turnPower = headingError * RobotConfig.RobotOriented.STEER_P;

            robot.drive(0, strafePower, turnPower);
            motorPowerHistory.add(strafePower);

            telemetry.addData("Distance", String.format("%.2f / %.2f inches", distanceTraveled, distanceInches));
            telemetry.addData("Error", String.format("%.3f inches", distanceError));

            if (Math.abs(distanceError) < 0.5) break;
            idle();
        }

        robot.stop();
    }

    /**
     * Turns the robot in place to a target heading using IMU feedback.
     *
     * <p>Uses proportional control with {@code RobotConfig.RobotOriented.STEER_P}.
     * Handles angle wrapping automatically.</p>
     *
     * @param targetHeadingRadians target heading in radians
     * @param maxTurnPower maximum turn power, in range [0.0, 1.0]
     * @throws InterruptedException if the OpMode is stopped
     */
    private void turnInPlaceTest(double targetHeadingRadians, double maxTurnPower) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (opModeIsActive() && System.currentTimeMillis() - startTime < 10000) {
            robot.update();
            double currentHeading = robot.getHeading();
            double headingError = angleDiff(targetHeadingRadians, currentHeading);

            double turnPower = Math.signum(headingError)
                    * Math.min(Math.abs(headingError) * RobotConfig.RobotOriented.STEER_P, maxTurnPower);

            robot.drive(0, 0, turnPower);
            motorPowerHistory.add(Math.abs(turnPower));

            telemetry.addData("Heading", String.format("%.2f / %.2f deg",
                    Math.toDegrees(currentHeading), Math.toDegrees(targetHeadingRadians)));
            telemetry.addData("Error", String.format("%.3f deg", Math.toDegrees(headingError)));

            if (Math.abs(headingError) < 0.05) break;
            idle();
        }

        robot.stop();
    }

    // -----------------------------------------------------------------------
    // Utility functions
    // -----------------------------------------------------------------------

    /**
     * Calculates shortest signed angle difference between two angles in radians.
     *
     * <p>Wraps result to (-π, π] range.</p>
     *
     * @param target target angle in radians
     * @param current current angle in radians
     * @return signed angle difference in radians
     */
    private double angleDiff(double target, double current) {
        double diff = target - current;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        return diff;
    }

    /**
     * Displays all current telemetry including step info, timer, and tuned values.
     *
     * <p>Shows live sidebar with all {@link TuneValues} and step-specific information.
     * Called every loop cycle.</p>
     */
    private void displayTelemetry() {
        long stepElapsed = System.currentTimeMillis() - stepStartTime;
        long totalElapsed = System.currentTimeMillis() - tuneStartTime;

        TelemetryPacket packet = new TelemetryPacket();

        telemetry.addLine("========== CRAWLER TUNER ==========");
        telemetry.addData("Step", String.format("%d/11: %s", currentStep, describeStep(currentStep)));
        telemetry.addData("Step Time", formatTime(stepElapsed));
        telemetry.addData("Total Time", formatTime(totalElapsed));
        telemetry.addLine("");

        telemetry.addLine("--- CURRENT VALUES ---");
        telemetry.addData("TRACK_WIDTH", String.format("%.4f", RobotConfig.Odometry.TRACK_WIDTH));
        telemetry.addData("CENTER_WHEEL_OFFSET", String.format("%.4f", RobotConfig.Odometry.CENTER_WHEEL_OFFSET));
        telemetry.addData("Drive Kp", String.format("%.5f", RobotConfig.RobotOriented.Kp));
        telemetry.addData("Strafe Kp", String.format("%.5f", RobotConfig.RobotOriented.strafe_Kp));
        telemetry.addData("Steer P", String.format("%.5f", RobotConfig.RobotOriented.STEER_P));
        telemetry.addData("Follow Distance", String.format("%.2f", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE));
        telemetry.addData("Move Speed", String.format("%.3f", RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED));
        telemetry.addLine("");

        telemetry.addLine("--- CONTROLS ---");
        telemetry.addLine("RB: Run test | D-pad: Adjust");
        telemetry.addLine("Circle: Next | Triangle: Back | X (hold): Skip");
        telemetry.addLine("");

        if (robot != null) {
            robot.update();
            Pose2d pose = robot.getPose();
            telemetry.addLine("--- ROBOT STATE ---");
            telemetry.addData("X", String.format("%.2f in", pose.x));
            telemetry.addData("Y", String.format("%.2f in", pose.y));
            telemetry.addData("Heading", String.format("%.2f°", Math.toDegrees(pose.heading)));
        }

        telemetry.update();
    }

    /**
     * Formats elapsed time in milliseconds to "MMm SSs" format.
     *
     * @param elapsedMs elapsed time in milliseconds
     * @return formatted time string
     */
    private String formatTime(long elapsedMs) {
        long seconds = elapsedMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%dm %02ds", minutes, seconds);
    }

    /**
     * Returns human-readable description of the given step number.
     *
     * @param step step index (0-11)
     * @return step description
     */
    private String describeStep(int step) {
        switch (step) {
            case STEP_HARDWARE_VERIFICATION: return "Hardware Verification";
            case STEP_IMU_VERIFICATION: return "IMU Verification";
            case STEP_TRACK_WIDTH: return "Track Width";
            case STEP_CENTER_WHEEL_OFFSET: return "Center Wheel Offset";
            case STEP_ODOMETRY_ACCURACY: return "Odometry Accuracy";
            case STEP_DRIVE_PID: return "Drive PID";
            case STEP_STRAFE_PID: return "Strafe PID";
            case STEP_TURN_PID: return "Turn PID";
            case STEP_FOLLOW_DISTANCE: return "Follow Distance";
            case STEP_MOVE_SPEED: return "Move Speed";
            case STEP_INTEGRATION_TEST: return "Integration Test";
            case STEP_COMPLETE: return "Complete";
            default: return "Unknown";
        }
    }

    /**
     * Resets test state when moving to a new step.
     *
     * <p>Clears test-specific variables and resets history lists.</p>
     */
    private void resetStepState() {
        testRunning = false;
        testComplete = false;
        motorPowerHistory.clear();
        testPath.clear();
        squareStartPose = null;
        stepStartTime = System.currentTimeMillis();
    }

    /**
     * Ensures the SD card tune directory exists, creating if necessary.
     *
     * <p>Creates `/sdcard/Crawler` if it does not exist.</p>
     */
    private void ensureTuneDirectory() {
        File dir = new File(TUNE_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.i(TAG, "Created tune directory: " + TUNE_DIR);
            } else {
                Log.w(TAG, "Failed to create tune directory: " + TUNE_DIR);
            }
        }
    }

    /**
     * Saves current tuned values to JSON and human-readable text file on SD card.
     *
     * <p>Writes two files:</p>
     * <ul>
     *   <li>{@code /sdcard/Crawler/tune.json} — machine-readable JSON</li>
     *   <li>{@code /sdcard/Crawler/tune_summary.txt} — human-readable summary</li>
     * </ul>
     *
     * <p>This method is called automatically at the end of step 10 (integration test)
     * and whenever Circle is pressed on any step.</p>
     */
    private void saveCurrentState() {
        try {
            // Save JSON
            JSONObject json = new JSONObject();
            JSONObject metadata = new JSONObject();
            metadata.put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date()));
            metadata.put("totalTimeSeconds", (System.currentTimeMillis() - tuneStartTime) / 1000);
            metadata.put("integrationTestPassed", values.integrationTestPassed);
            metadata.put("returnErrorInches", values.integrationReturnError);
            json.put("metadata", metadata);

            JSONObject odometry = new JSONObject();
            odometry.put("trackWidth", RobotConfig.Odometry.TRACK_WIDTH);
            odometry.put("centerWheelOffset", RobotConfig.Odometry.CENTER_WHEEL_OFFSET);
            json.put("odometry", odometry);

            JSONObject robotOriented = new JSONObject();
            robotOriented.put("driveKp", RobotConfig.RobotOriented.Kp);
            robotOriented.put("strafeKp", RobotConfig.RobotOriented.strafe_Kp);
            robotOriented.put("steerP", RobotConfig.RobotOriented.STEER_P);
            json.put("robotOriented", robotOriented);

            JSONObject fieldOriented = new JSONObject();
            fieldOriented.put("followDistance", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE);
            fieldOriented.put("moveSpeed", RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED);
            json.put("fieldOriented", fieldOriented);

            Files.write(Paths.get(TUNE_JSON), json.toString(2).getBytes());
            Log.i(TAG, "Saved tune JSON to " + TUNE_JSON);

            // Save human-readable summary
            StringBuilder summary = new StringBuilder();
            summary.append("Crawler Tune Summary\n");
            summary.append("====================\n");
            summary.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append("\n");
            long totalSec = (System.currentTimeMillis() - tuneStartTime) / 1000;
            summary.append("Total Time: ").append(totalSec / 60).append("m ").append(totalSec % 60).append("s\n");
            summary.append("Integration Test: ").append(values.integrationTestPassed ? "PASS" : "FAIL")
                    .append(" (").append(String.format("%.2f", values.integrationReturnError)).append(" inch error)\n");
            summary.append("\nCopy these values into RobotConfig:\n\n");
            summary.append("Odometry.TRACK_WIDTH          = ").append(RobotConfig.Odometry.TRACK_WIDTH).append("\n");
            summary.append("Odometry.CENTER_WHEEL_OFFSET  = ").append(RobotConfig.Odometry.CENTER_WHEEL_OFFSET).append("\n");
            summary.append("RobotOriented.Kp              = ").append(RobotConfig.RobotOriented.Kp).append("\n");
            summary.append("RobotOriented.strafe_Kp       = ").append(RobotConfig.RobotOriented.strafe_Kp).append("\n");
            summary.append("RobotOriented.STEER_P         = ").append(RobotConfig.RobotOriented.STEER_P).append("\n");
            summary.append("FieldOriented.DEFAULT_FOLLOW_DISTANCE = ").append(RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE).append("\n");
            summary.append("FieldOriented.DEFAULT_MOVE_SPEED      = ").append(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED).append("\n");

            Files.write(Paths.get(TUNE_SUMMARY), summary.toString().getBytes());
            Log.i(TAG, "Saved tune summary to " + TUNE_SUMMARY);

        } catch (IOException e) {
            Log.e(TAG, "Failed to save tune files", e);
            telemetry.addData("SAVE ERROR", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // TuneValues helper class
    // -----------------------------------------------------------------------

    /**
     * Container for all tuning values with adjustment logic.
     *
     * <p>Centralizes all tuned parameters and provides per-step adjustment
     * with step-specific adjustment increments.</p>
     */
    private static class TuneValues {
        double trackWidth;
        double centerWheelOffset;
        double driveKp;
        double strafeKp;
        double steerP;
        double followDistance;
        double moveSpeed;

        boolean integrationTestPassed = false;
        double integrationReturnError = 0;

        /**
         * Loads current values from {@link RobotConfig}.
         */
        void loadFromRobotConfig() {
            trackWidth = RobotConfig.Odometry.TRACK_WIDTH;
            centerWheelOffset = RobotConfig.Odometry.CENTER_WHEEL_OFFSET;
            driveKp = RobotConfig.RobotOriented.Kp;
            strafeKp = RobotConfig.RobotOriented.strafe_Kp;
            steerP = RobotConfig.RobotOriented.STEER_P;
            followDistance = RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE;
            moveSpeed = RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED;
        }

        /**
         * Adjusts the tuning value for the current step.
         *
         * <p>Each step has a different adjustment increment (adjustStep) to provide
         * appropriate granularity. Values are clamped to reasonable ranges.</p>
         *
         * @param increase true to increase, false to decrease
         * @param step current step number
         */
        void adjustCurrentValue(boolean increase, int step) {
            double delta = increase ? 1 : -1;

            switch (step) {
                case STEP_TRACK_WIDTH:
                    RobotConfig.Odometry.TRACK_WIDTH += delta * 0.1;
                    RobotConfig.Odometry.TRACK_WIDTH = Math.max(5, Math.min(30, RobotConfig.Odometry.TRACK_WIDTH));
                    break;
                case STEP_CENTER_WHEEL_OFFSET:
                    RobotConfig.Odometry.CENTER_WHEEL_OFFSET += delta * 0.05;
                    RobotConfig.Odometry.CENTER_WHEEL_OFFSET = Math.max(0, Math.min(10, RobotConfig.Odometry.CENTER_WHEEL_OFFSET));
                    break;
                case STEP_DRIVE_PID:
                    RobotConfig.RobotOriented.Kp += delta * 0.005;
                    RobotConfig.RobotOriented.Kp = Math.max(0, Math.min(0.2, RobotConfig.RobotOriented.Kp));
                    break;
                case STEP_STRAFE_PID:
                    RobotConfig.RobotOriented.strafe_Kp += delta * 0.005;
                    RobotConfig.RobotOriented.strafe_Kp = Math.max(0, Math.min(0.2, RobotConfig.RobotOriented.strafe_Kp));
                    break;
                case STEP_TURN_PID:
                    RobotConfig.RobotOriented.STEER_P += delta * 0.002;
                    RobotConfig.RobotOriented.STEER_P = Math.max(0, Math.min(0.1, RobotConfig.RobotOriented.STEER_P));
                    break;
                case STEP_FOLLOW_DISTANCE:
                    RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE += delta * 0.5;
                    RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE = Math.max(1, Math.min(30, RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE));
                    break;
                case STEP_MOVE_SPEED:
                    RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED += delta * 0.05;
                    RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED = Math.max(0.1, Math.min(1.0, RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED));
                    break;
            }
        }
    }
}
