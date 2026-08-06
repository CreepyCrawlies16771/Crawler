package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrors;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.Arrays;
import java.util.List;

/**
 * The guided tuning session behind {@code CrawlerTuner}.
 *
 * <p>The tuning robot is supplied by a {@link TuningRobotFactory} from your team code,
 * so the tuner always drives the same hardware names and localizer as your own
 * {@link CrawlerRobot} subclass (the shipped example is {@code MyRobot.builder}) — there
 * is no separate robot config to keep in sync.</p>
 */
public final class TuningSession {

    enum Step {
        MOTORS(1, "Spin each motor"),
        ENCODERS(2, "Encoder ticks / wheel size"),
        TRACK_WIDTH(3, "Track width"),
        CENTER_OFFSET(4, "Center pod offset"),
        PID(5, "Drive / strafe / turn"),
        AUTO_PATH(6, "Square path test"),
        FINISH(7, "Copy into your robot");

        final int number;
        final String title;

        Step(int number, String title) {
            this.number = number;
            this.title = title;
        }
    }

    /** One tunable test. Triangle cycles through these; {@code D-pad L/R} picks a term. */
    enum PidMode {
        DRIVE("Drive", "driveKp", "driveKi", "driveKd"),
        STRAFE("Strafe", "strafeKp", "strafeKi", "strafeKd"),
        TURN("Turn", "steerP", "steerI", "steerD"),
        MIN_POWER("Min power", "minPower", null, null);

        final String label;
        final String term0;
        final String term1;
        final String term2;

        PidMode(String label, String term0, String term1, String term2) {
            this.label = label;
            this.term0 = term0;
            this.term1 = term1;
            this.term2 = term2;
        }

        int termCount() {
            return term1 == null ? 1 : (term2 == null ? 2 : 3);
        }

        double get(int term) {
            switch (term) {
                case 0: return termValue(term0);
                case 1: return termValue(term1);
                default: return termValue(term2);
            }
        }

        void set(int term, double value) {
            switch (term) {
                case 0: setTerm(term0, value); break;
                case 1: setTerm(term1, value); break;
                default: setTerm(term2, value); break;
            }
        }

        private static double termValue(String name) {
            switch (name) {
                case "driveKp":  return TuningConfig.driveKp;
                case "driveKi":  return TuningConfig.driveKi;
                case "driveKd":  return TuningConfig.driveKd;
                case "strafeKp": return TuningConfig.strafeKp;
                case "strafeKi": return TuningConfig.strafeKi;
                case "strafeKd": return TuningConfig.strafeKd;
                case "steerP":   return TuningConfig.steerP;
                case "steerI":   return TuningConfig.steerI;
                case "steerD":   return TuningConfig.steerD;
                default:         return TuningConfig.minPower;
            }
        }

        private static void setTerm(String name, double value) {
            switch (name) {
                case "driveKp":  TuningConfig.driveKp = value; break;
                case "driveKi":  TuningConfig.driveKi = value; break;
                case "driveKd":  TuningConfig.driveKd = value; break;
                case "strafeKp": TuningConfig.strafeKp = value; break;
                case "strafeKi": TuningConfig.strafeKi = value; break;
                case "strafeKd": TuningConfig.strafeKd = value; break;
                case "steerP":   TuningConfig.steerP = value; break;
                case "steerI":   TuningConfig.steerI = value; break;
                case "steerD":   TuningConfig.steerD = value; break;
                default:         TuningConfig.minPower = value; break;
            }
        }

        /** Increment used by D-pad up/down for a given term index. */
        double stepFor(int term) {
            switch (term) {
                case 1: return isTurn() ? 0.0005 : 0.001;      // I terms
                case 2: return 0.01;                            // D terms
                default: return isTurn() ? 0.005 : 0.01;        // P terms + min power
            }
        }

        boolean isTurn() {
            return this == TURN;
        }
    }

    private static final double DIAMETER_STEP_IN = 0.01;
    private static final int    TICKS_STEP       = 50;
    private static final double SPIN_POWER       = 0.3;
    private static final double STRAFE_POWER     = 0.5;
    private static final double STRAFE_TARGET_CM = 100.0;
    private static final double SPIN_TARGET_DEG  = 3600.0;

    private final TuningRobotFactory factory;
    private final Telemetry telemetry;
    private final TuningActiveCheck active;
    private final Gamepad gamepad;

    private CrawlerRobot robot;
    private TuningPidRunner pidRunner;

    private Step step = Step.MOTORS;
    private PidMode pidMode = PidMode.DRIVE;
    private int pidTerm;
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
    private final boolean[] triangleEdge = {false};

    /**
     * @param factory builds the tuning robot from live tuning values (see
     *                {@link TuningRobotFactory} — typically your robot's builder, e.g.
     *                {@code config -> MyRobot.buildTuned(hwMap, config)} in the shipped example)
     * @param driverTelemetry telemetry to mirror to the FTC Dashboard
     * @param gamepad the gamepad used to control the tuning steps
     * @param active lets the session know when the OpMode is stopping
     */
    public TuningSession(TuningRobotFactory factory, Telemetry driverTelemetry,
                         Gamepad gamepad, TuningActiveCheck active) {
        this.factory = factory;
        this.gamepad = gamepad;
        this.active = active;
        this.telemetry = new TuningTelemetry(driverTelemetry).get();

        rebuildRobot();
    }

    /** The current tuning robot (rebuilds when a tuning value changes). */
    public CrawlerRobot getRobot() {
        return robot;
    }

    /** Runs one tuning cycle — call every OpMode loop while active. */
    public void loop() throws InterruptedException {
        if (!active.isActive()) {
            robot.stop();
            return;
        }
        if (testRunning) return;

        // Rebuild the robot whenever a value changed (gamepad or FTC Dashboard).
        rebuildIfChanged();
        handleGlobalInput();

        telemetry.clear();
        telemetry.addLine("Crawler Tuner | Step " + step.number + "/7: " + step.title);
        telemetry.addLine("Circle: next  X: back  Square: copy values");
        telemetry.addLine("Edit values live in FTC Dashboard -> Crawler Tuner");
        telemetry.addLine("Stop OpMode, paste values into your robot, then run again");

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

    // -----------------------------------------------------------------------
    // Robot construction
    // -----------------------------------------------------------------------

    private void rebuildIfChanged() {
        CrawlerRobot.Config fresh = TuningConfig.toConfig();
        if (robot == null || !configEquals(robot.config, fresh)) {
            rebuildRobot(fresh);
        }
    }

    private void rebuildRobot() {
        rebuildRobot(TuningConfig.toConfig());
    }

    private void rebuildRobot(CrawlerRobot.Config c) {
        robot = factory.create(c);
        pidRunner = new TuningPidRunner(robot, telemetry, active);
    }

    private static boolean configEquals(CrawlerRobot.Config a, CrawlerRobot.Config b) {
        return a.trackWidthIn == b.trackWidthIn
                && a.centerWheelOffsetIn == b.centerWheelOffsetIn
                && a.wheelDiameterIn == b.wheelDiameterIn
                && a.ticksPerRev == b.ticksPerRev
                && a.driveKp == b.driveKp && a.driveKi == b.driveKi && a.driveKd == b.driveKd
                && a.strafeKp == b.strafeKp && a.strafeKi == b.strafeKi && a.strafeKd == b.strafeKd
                && a.steerP == b.steerP && a.steerI == b.steerI && a.steerD == b.steerD
                && a.minPower == b.minPower
                && a.defaultMoveSpeed == b.defaultMoveSpeed
                && a.defaultTurnSpeed == b.defaultTurnSpeed
                && a.followDistanceCm == b.followDistanceCm
                && a.arrivalThresholdCm == b.arrivalThresholdCm
                && a.orbitThresholdCm == b.orbitThresholdCm
                && a.timeoutSecs == b.timeoutSecs
                && a.maxDriveSpeed == b.maxDriveSpeed;
    }

    // -----------------------------------------------------------------------
    // Global input / snippet
    // -----------------------------------------------------------------------

    private void handleGlobalInput() {
        if (TuningUtil.square(gamepad, squareEdge)) {
            showSnippet = !showSnippet;
            statusMessage = showSnippet ? "Copy values into your robot class" : "";
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

    // -----------------------------------------------------------------------
    // Step 1 — motors
    // -----------------------------------------------------------------------

    private void loopMotors() {
        telemetry.addLine("RB=FL  LB=FR  RT=BL  LT=BR");
        robot.stop();
        if (gamepad.right_bumper) robot.frontLeft.set(0.5);
        if (gamepad.left_bumper)  robot.frontRight.set(0.5);
        if (gamepad.right_trigger > 0.1) robot.backLeft.set(gamepad.right_trigger);
        if (gamepad.left_trigger > 0.1)  robot.backRight.set(gamepad.left_trigger);
    }

    // -----------------------------------------------------------------------
    // Step 2 — encoders / wheel diameter
    // -----------------------------------------------------------------------

    private void loopEncoders() {
        telemetry.addLine("D-pad U/D: wheel diameter  L/R: ticks/rev  RB: spin");
        MotorEx left = robot.getLeftEncoder();
        MotorEx right = robot.getRightEncoder();
        MotorEx center = robot.getCenterEncoder();
        if (left != null)   telemetry.addData("Left ticks", left.getCurrentPosition());
        if (right != null)  telemetry.addData("Right ticks", right.getCurrentPosition());
        if (center != null) telemetry.addData("Center ticks", center.getCurrentPosition());
        telemetry.addData("ticksPerRev", (int) TuningConfig.ticksPerRev);
        telemetry.addData("wheelDiameter in", TuningConfig.wheelDiameterIn);

        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            TuningConfig.wheelDiameterIn += DIAMETER_STEP_IN;
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            TuningConfig.wheelDiameterIn = Math.max(0.5, TuningConfig.wheelDiameterIn - DIAMETER_STEP_IN);
        }
        if (TuningUtil.pressed(gamepad.dpad_left, dpadLeftEdge)) {
            TuningConfig.ticksPerRev = Math.max(1, TuningConfig.ticksPerRev - TICKS_STEP);
        }
        if (TuningUtil.pressed(gamepad.dpad_right, dpadRightEdge)) {
            TuningConfig.ticksPerRev += TICKS_STEP;
        }
        if (gamepad.right_bumper) {
            robot.frontLeft.set(0.2);
            robot.frontRight.set(0.2);
        }
    }

    // -----------------------------------------------------------------------
    // Step 3 — track width
    // -----------------------------------------------------------------------

    private void loopTrackWidth() throws InterruptedException {
        telemetry.addLine("D-pad U/D: track width  RB: spin test  (rebuilds robot)");
        telemetry.addData("trackWidth in", TuningConfig.trackWidthIn);
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            TuningConfig.trackWidthIn += 0.1;
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            TuningConfig.trackWidthIn = Math.max(1.0, TuningConfig.trackWidthIn - 0.1);
        }
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
            TuningDashboard.drawRobot(robot);   // live on the Dashboard field view

            double imuDelta = Math.abs(TuningUtil.imuYawDeg(robot.imu) - startImu);
            if (imuDelta >= SPIN_TARGET_DEG) break;
            Thread.yield();
        }

        robot.stop();
        robot.update();
        double odomSigned = Math.toDegrees(
                robot.getPose().getHeading() - startPose.getHeading());
        double imuSigned = TuningUtil.imuYawDeg(robot.imu) - startImu;
        double odomFinal = Math.abs(odomSigned);
        double imuFinal = Math.abs(imuSigned);
        double diff = Math.abs(odomFinal - imuFinal);

        if (Math.signum(odomSigned) != Math.signum(imuSigned)) {
            CrawlerErrors.postToTelemetry(telemetry, CrawlerError.ODO_REVERSED_DIRECTION);
        }
        if (diff >= 5) {
            CrawlerErrors.postToTelemetry(telemetry, CrawlerError.ODO_DRIFT_DETECTED, diff);
        }

        statusMessage = diff < 5
                ? "Track width OK — copy into your robot"
                : "Tweak track width, rebuild (drift " + String.format("%.1f deg", diff) + ")";
    }

    // -----------------------------------------------------------------------
    // Step 4 — center wheel offset
    // -----------------------------------------------------------------------

    private void loopCenterOffset() throws InterruptedException {
        telemetry.addLine("D-pad U/D: center offset  RB: strafe 1 m");
        telemetry.addData("centerOffset in", TuningConfig.centerWheelOffsetIn);
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            TuningConfig.centerWheelOffsetIn += 0.1;
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            TuningConfig.centerWheelOffsetIn = Math.max(0, TuningConfig.centerWheelOffsetIn - 0.1);
        }
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
            TuningDashboard.drawRobot(robot);   // live on the Dashboard field view
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
        statusMessage = drift < 2
                ? "Center offset OK"
                : "Tweak offset, copy into your robot (drift " + String.format("%.1f deg", drift) + ")";
    }

    // -----------------------------------------------------------------------
    // Step 5 — PID (drive / strafe / turn / min power)
    // -----------------------------------------------------------------------

    private void loopPid() throws InterruptedException {
        telemetry.addLine("Triangle: switch test  D-pad L/R: term  U/D: adjust  RB: run");
        if (TuningUtil.triangle(gamepad, triangleEdge)) {
            pidMode = PidMode.values()[(pidMode.ordinal() + 1) % PidMode.values().length];
            pidTerm = 0;
        }

        int count = pidMode.termCount();
        if (TuningUtil.pressed(gamepad.dpad_left, dpadLeftEdge)) {
            pidTerm = (pidTerm - 1 + count) % count;
        }
        if (TuningUtil.pressed(gamepad.dpad_right, dpadRightEdge)) {
            pidTerm = (pidTerm + 1) % count;
        }

        telemetry.addData("Test", pidMode.label);
        telemetry.addData("Term", termLabel(pidMode, pidTerm));
        telemetry.addData("Value", String.format("%.4f", pidMode.get(pidTerm)));

        double stepSize = pidMode.stepFor(pidTerm);
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            pidMode.set(pidTerm, pidMode.get(pidTerm) + stepSize);
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            pidMode.set(pidTerm, Math.max(0, pidMode.get(pidTerm) - stepSize));
        }

        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            switch (pidMode) {
                case DRIVE:     pidRunner.testDrive(); break;
                case STRAFE:    pidRunner.testStrafe(); break;
                case TURN:      pidRunner.testTurn(); break;
                case MIN_POWER: pidRunner.testMinPower(); break;
            }
            testRunning = false;
        }
    }

    private static String termLabel(PidMode mode, int term) {
        switch (term) {
            case 1: return mode.isTurn() ? "steerI"
                    : (mode == PidMode.DRIVE ? "driveKi" : "strafeKi");
            case 2: return mode.isTurn() ? "steerD"
                    : (mode == PidMode.DRIVE ? "driveKd" : "strafeKd");
            default: return mode.term0;
        }
    }

    // -----------------------------------------------------------------------
    // Step 6 — square path test
    // -----------------------------------------------------------------------

    private void loopAutoPath() throws InterruptedException {
        telemetry.addData("moveSpeed", TuningConfig.moveSpeed);
        telemetry.addLine("D-pad U/D: move speed  RB: 1 m square");
        if (TuningUtil.dpadUp(gamepad, dpadUpEdge)) {
            TuningConfig.moveSpeed = Math.min(1, TuningConfig.moveSpeed + 0.05);
        }
        if (TuningUtil.dpadDown(gamepad, dpadDownEdge)) {
            TuningConfig.moveSpeed = Math.max(0.1, TuningConfig.moveSpeed - 0.05);
        }

        if (TuningUtil.rightBumper(gamepad, rbEdge)) {
            testRunning = true;
            // Preflight inside follow() throws CRWL-101 unless a start pose is set.
            robot.resetPose();
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
            statusMessage = "Path done — copy pathDefaults into your robot";
        }
    }

    private void loopFinish() {
        showSnippet = true;
        printSnippet();
    }
}
