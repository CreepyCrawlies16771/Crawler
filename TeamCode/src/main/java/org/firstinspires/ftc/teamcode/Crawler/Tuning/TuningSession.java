package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.Arrays;
import java.util.List;

final class TuningSession {

    enum Step {
        MOTORS(1, "Spin each motor"),
        ENCODERS(2, "Encoder ticks / wheel size"),
        TRACK_WIDTH(3, "Track width"),
        CENTER_OFFSET(4, "Center pod offset"),
        PID(5, "Drive / strafe / turn"),
        AUTO_PATH(6, "Square path test"),
        FINISH(7, "Copy into MyRobot.java");

        final int number;
        final String title;

        Step(int number, String title) {
            this.number = number;
            this.title = title;
        }
    }

    enum PidMode { DRIVE, STRAFE, TURN }

    private static final double STEP_IN          = 0.1;
    private static final double DIAMETER_STEP_IN = 0.01;
    private static final int    TICKS_STEP       = 50;
    private static final double SPIN_POWER       = 0.3;
    private static final double STRAFE_POWER     = 0.5;
    private static final double STRAFE_TARGET_CM = 100.0;
    private static final double SPIN_TARGET_DEG  = 3600.0;

    private final HardwareMap hardwareMap;
    private final Telemetry telemetry;
    private final TuningActiveCheck active;
    private final Gamepad gamepad;
    private final MotorEx leftEncoder;
    private final MotorEx rightEncoder;
    private final MotorEx centerEncoder;

    private CrawlerRobot robot;
    private TuningPidRunner pidRunner;
    private final CrawlerRobot.Config trial = new CrawlerRobot.Config();

    private Step step = Step.MOTORS;
    private PidMode pidMode = PidMode.DRIVE;
    private boolean testRunning;
    private boolean showSnippet;
    private String statusMessage = "";

    private final boolean[] dpadUpEdge = {false};
    private final boolean[] dpadDownEdge = {false};
    private final boolean[] dpadLeftEdge = {false};
    private final boolean[] dpadRightEdge = {false};
    private final boolean[] circleEdge = {false};
    private final boolean[] squareEdge = {false};
    private final boolean[] xEdge = {false};
    private final boolean[] rbEdge = {false};
    private final boolean[] xModeEdge = {false};

    TuningSession(HardwareMap hardwareMap, Telemetry driverTelemetry,
                  Gamepad gamepad, TuningActiveCheck active) {
        this.hardwareMap = hardwareMap;
        this.gamepad = gamepad;
        this.active = active;
        this.telemetry = new TuningTelemetry(driverTelemetry).get();

        leftEncoder = new MotorEx(hardwareMap, TuningRobotConfig.ENC_LEFT);
        rightEncoder = new MotorEx(hardwareMap, TuningRobotConfig.ENC_RIGHT);
        centerEncoder = new MotorEx(hardwareMap, TuningRobotConfig.ENC_CENTER);

        rebuildRobot();
    }

    CrawlerRobot getRobot() {
        return robot;
    }

    void loop() throws InterruptedException {
        if (!active.isActive()) {
            robot.stop();
            return;
        }
        if (testRunning) return;

        handleGlobalInput();

        telemetry.clear();
        telemetry.addLine("Crawler Tuner | Step " + step.number + "/7: " + step.title);
        telemetry.addLine("Circle: next  X: back  Square: MyRobot builder code");
        telemetry.addLine("Stop OpMode, paste into MyRobot.java, then run again to test");

        if (showSnippet) {
            printSnippet();
        } else {
            switch (step) {
                case MOTORS:        loopMotors(); break;
                case ENCODERS:      loopEncoders(); break;
                case TRACK_WIDTH:   loopTrackWidth(); break;
                case CENTER_OFFSET: loopCenterOffset(); break;
                case PID:           loopPid(); break;
                case AUTO_PATH:     loopAutoPath(); break;
                case FINISH:        loopFinish(); break;
            }
        }

        if (!statusMessage.isEmpty()) telemetry.addLine(statusMessage);
        telemetry.update();
    }

    private void rebuildRobot() {
        robot = new CrawlerRobot.Builder(hardwareMap)
                .frontLeft(TuningRobotConfig.FRONT_LEFT)
                .frontRight(TuningRobotConfig.FRONT_RIGHT)
                .backLeft(TuningRobotConfig.BACK_LEFT)
                .backRight(TuningRobotConfig.BACK_RIGHT)
                .imu(TuningRobotConfig.IMU)
                .imuOrientation(
                        TuningRobotConfig.IMU_LOGO_FACING,
                        TuningRobotConfig.IMU_USB_FACING)
                .motors()
                .withThreeDeadWheels(
                        TuningRobotConfig.ENC_LEFT,
                        TuningRobotConfig.ENC_RIGHT,
                        TuningRobotConfig.ENC_CENTER)
                .setTrackWidth(trial.trackWidthIn)
                .setCenterWheelOffset(trial.centerWheelOffsetIn)
                .wheelDiameter(trial.wheelDiameterIn)
                .ticksPerRev(trial.ticksPerRev)
                .drivePid(trial.driveKp, trial.driveKi, trial.driveKd)
                .strafePid(trial.strafeKp, trial.strafeKi, trial.strafeKd)
                .steerPid(trial.steerP, trial.steerI, trial.steerD)
                .pathDefaults(trial.defaultMoveSpeed, trial.defaultTurnSpeed, trial.followDistanceCm)
                .arrivalThresholdCm(trial.arrivalThresholdCm)
                .build();
        pidRunner = new TuningPidRunner(robot, telemetry, active);
    }

    private void handleGlobalInput() {
        if (TuningUtil.square(gamepad, squareEdge)) {
            showSnippet = !showSnippet;
            statusMessage = showSnippet ? "Copy lines into MyRobot.java" : "";
        }
        if (TuningUtil.circle(gamepad, circleEdge) && step != Step.FINISH) {
            step = Step.values()[step.ordinal() + 1];
            showSnippet = false;
            statusMessage = "Step " + step.number + ": " + step.title;
        }
        if (TuningUtil.xButton(gamepad, xEdge) && step != Step.MOTORS) {
            step = Step.values()[step.ordinal() - 1];
            showSnippet = false;
        }
    }

    private void printSnippet() {
        for (String line : MyRobotSnippet.format(robot.config).split("\n")) {
            telemetry.addLine(line);
        }
    }

    private void loopMotors() {
        telemetry.addLine("RB=FL  LB=FR  RT=BL  LT=BR");
        robot.stop();
        if (gamepad.right_bumper) robot.frontLeft.set(0.5);
        if (gamepad.left_bumper)  robot.frontRight.set(0.5);
        if (gamepad.right_trigger > 0.1) robot.backLeft.set(gamepad.right_trigger);
        if (gamepad.left_trigger > 0.1)  robot.backRight.set(gamepad.left_trigger);
    }

    private void loopEncoders() {
        telemetry.addLine("D-pad U/D: wheel diameter  L/R: ticks/rev  RB: spin");
        telemetry.addData("Left ticks", leftEncoder.getCurrentPosition());
        telemetry.addData("Right ticks", rightEncoder.getCurrentPosition());
        telemetry.addData("Center ticks", centerEncoder.getCurrentPosition());
        telemetry.addData("ticksPerRev", (int) trial.ticksPerRev);
        telemetry.addData("wheelDiameter in", trial.wheelDiameterIn);

        boolean encChanged = false;
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            trial.wheelDiameterIn += DIAMETER_STEP_IN; encChanged = true;
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            trial.wheelDiameterIn = Math.max(0.5, trial.wheelDiameterIn - DIAMETER_STEP_IN);
            encChanged = true;
        }
        if (TuningUtil.pressed(gamepad.dpad_left, dpadLeftEdge)) {
            trial.ticksPerRev = Math.max(1, trial.ticksPerRev - TICKS_STEP); encChanged = true;
        }
        if (TuningUtil.pressed(gamepad.dpad_right, dpadRightEdge)) {
            trial.ticksPerRev += TICKS_STEP; encChanged = true;
        }
        if (encChanged) rebuildRobot();
        if (gamepad.right_bumper) {
            robot.frontLeft.set(0.2);
            robot.frontRight.set(0.2);
        }
    }

    private void loopTrackWidth() throws InterruptedException {
        telemetry.addLine("D-pad U/D: track width  RB: spin test  (rebuilds robot)");
        telemetry.addData("trackWidth in", trial.trackWidthIn);
        boolean changed = false;
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) { trial.trackWidthIn += STEP_IN; changed = true; }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            trial.trackWidthIn = Math.max(1.0, trial.trackWidthIn - STEP_IN);
            changed = true;
        }
        if (changed) rebuildRobot();
        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            runSpinTest();
            testRunning = false;
        }
    }

    private void runSpinTest() throws InterruptedException {
        robot.imu.resetYaw();
        robot.update();
        Pose2d startPose = robot.getPose();
        double startImu = TuningUtil.imuYawDeg(robot.imu);

        while (active.isActive()) {
            robot.frontLeft.set(SPIN_POWER);
            robot.backLeft.set(SPIN_POWER);
            robot.frontRight.set(-SPIN_POWER);
            robot.backRight.set(-SPIN_POWER);
            robot.update();

            double imuDelta = Math.abs(TuningUtil.imuYawDeg(robot.imu) - startImu);
            if (imuDelta >= SPIN_TARGET_DEG) break;
            Thread.yield();
        }

        robot.stop();
        robot.update();
        double odomFinal = Math.abs(Math.toDegrees(
                robot.getPose().getHeading() - startPose.getHeading()));
        double imuFinal = Math.abs(TuningUtil.imuYawDeg(robot.imu) - startImu);
        double diff = Math.abs(odomFinal - imuFinal);
        statusMessage = diff < 5 ? "Track width OK — paste into MyRobot" : "Tweak track width, rebuild";
    }

    private void loopCenterOffset() throws InterruptedException {
        telemetry.addLine("D-pad U/D: center offset  RB: strafe 1 m");
        telemetry.addData("centerOffset in", trial.centerWheelOffsetIn);
        boolean changed = false;
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) { trial.centerWheelOffsetIn += STEP_IN; changed = true; }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            trial.centerWheelOffsetIn = Math.max(0, trial.centerWheelOffsetIn - STEP_IN);
            changed = true;
        }
        if (changed) rebuildRobot();
        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            runStrafeTest();
            testRunning = false;
        }
    }

    private void runStrafeTest() throws InterruptedException {
        robot.update();
        Pose2d start = robot.getPose();
        double startHeading = Math.toDegrees(start.getHeading());

        while (active.isActive()) {
            robot.update();
            double dist = Math.hypot(
                    robot.getPose().getX() - start.getX(),
                    robot.getPose().getY() - start.getY());
            if (dist >= STRAFE_TARGET_CM) break;
            robot.drive(0, STRAFE_POWER, 0);
            Thread.yield();
        }

        robot.stop();
        robot.update();
        double drift = Math.abs(Math.toDegrees(robot.getPose().getHeading()) - startHeading);
        statusMessage = drift < 2 ? "Center offset OK" : "Tweak offset, paste into MyRobot";
    }

    private void loopPid() throws InterruptedException {
        telemetry.addLine("D-pad U/D: adjust Kp for current test  RB: run");
        telemetry.addLine("X: switch drive / strafe / turn");
        if (TuningUtil.xButton(gamepad, xModeEdge)) {
            pidMode = PidMode.values()[(pidMode.ordinal() + 1) % 3];
        }
        telemetry.addData("Test", pidMode);
        boolean pidChanged = false;
        switch (pidMode) {
            case DRIVE:
                telemetry.addData("driveKp", trial.driveKp);
                if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) { trial.driveKp += 0.01; pidChanged = true; }
                if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
                    trial.driveKp = Math.max(0, trial.driveKp - 0.01); pidChanged = true;
                }
                break;
            case STRAFE:
                telemetry.addData("strafeKp", trial.strafeKp);
                if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) { trial.strafeKp += 0.01; pidChanged = true; }
                if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
                    trial.strafeKp = Math.max(0, trial.strafeKp - 0.01); pidChanged = true;
                }
                break;
            case TURN:
                telemetry.addData("steerP", trial.steerP);
                if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) { trial.steerP += 0.005; pidChanged = true; }
                if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
                    trial.steerP = Math.max(0, trial.steerP - 0.005); pidChanged = true;
                }
                break;
        }
        if (pidChanged) rebuildRobot();
        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            switch (pidMode) {
                case DRIVE:  pidRunner.testDrive(); break;
                case STRAFE: pidRunner.testStrafe(); break;
                case TURN:   pidRunner.testTurn(); break;
            }
            testRunning = false;
        }
    }

    private void loopAutoPath() throws InterruptedException {
        telemetry.addData("moveSpeed", trial.defaultMoveSpeed);
        telemetry.addLine("D-pad U/D: move speed  RB: 1 m square");
        boolean speedChanged = false;
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            trial.defaultMoveSpeed = Math.min(1, trial.defaultMoveSpeed + 0.05); speedChanged = true;
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            trial.defaultMoveSpeed = Math.max(0.1, trial.defaultMoveSpeed - 0.05); speedChanged = true;
        }
        if (speedChanged) rebuildRobot();

        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            FOFollower follower = new FOFollower(robot, telemetry, active::isActive);
            CrawlerRobot.Config c = robot.config;
            follower.follow(Arrays.asList(
                    Waypoint.at(0, 0, c).build(),
                    Waypoint.at(100, 0, c).build(),
                    Waypoint.at(100, 100, c).build(),
                    Waypoint.at(0, 100, c).build(),
                    Waypoint.at(0, 0, c).build()
            ));
            testRunning = false;
            statusMessage = "Path done — paste pathDefaults into MyRobot";
        }
    }

    private void loopFinish() {
        showSnippet = true;
        printSnippet();
    }
}
