package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Live-tuning values for {@link CrawlerTuner}.
 *
 * <p>Every field is {@code public static} so the FTC Dashboard config panel
 * ({@code http://&lt;robot-ip&gt;:8080/dash}) shows a <b>Crawler Tuner</b> group where you
 * can type values directly in the browser — they take effect on the next loop cycle.
 * Gamepad adjustments in the tuner write back to the same fields, so both inputs stay
 * in sync.</p>
 *
 * <p>There are <b>no preset values</b>. {@link TuningSession} seeds these fields from
 * your robot's builder when the tuner starts ({@link #seed(CrawlerRobot.Config)}), so
 * you always start tuning from the values already in your robot class — never from
 * hard-coded numbers. When you are happy with a set of values, press <b>Square</b> in
 * the tuner to print the matching <b>builder lines</b> and paste them into your robot's
 * builder chain.</p>
 */
@Config("Crawler Tuner")
public final class TuningConfig {

    // --- Odometry -----------------------------------------------------------
    public static double trackWidthIn        = 0;
    public static double centerWheelOffsetIn = 0;
    public static double wheelDiameterIn     = 0;
    public static double ticksPerRev         = 0;

    // --- Robot-relative PID -------------------------------------------------
    public static double driveKp  = 0;
    public static double driveKi  = 0;
    public static double driveKd  = 0;

    public static double strafeKp = 0;
    public static double strafeKi = 0;
    public static double strafeKd = 0;

    public static double steerP   = 0;
    public static double steerI   = 0;
    public static double steerD   = 0;

    /** Smallest motor power that still moves the robot (friction deadband). */
    public static double minPower = 0;

    // --- Path following -----------------------------------------------------
    public static double moveSpeed            = 0;
    public static double turnSpeed            = 0;
    public static double followDistanceCm     = 0;
    public static double arrivalThresholdCm   = 0;
    public static double orbitThresholdCm     = 0;
    public static double timeoutSecs          = 0;
    public static double maxDriveSpeed        = 0;
    public static double turnReferenceRadians = 0;

    // --- Optional per-waypoint slow-down ------------------------------------
    public static double slowMoveSpeed        = 0;
    public static double slowTurnSpeed        = 0;
    public static double slowFollowDistanceCm = 0;
    public static double slowDownTurnRadians  = 0;
    public static double slowDownTurnAmount   = 0;

    private TuningConfig() {}

    /**
     * Copies every value from the robot's builder config into these static fields, so
     * the tuner starts from <i>your</i> robot instead of hard-coded presets. Called by
     * {@link TuningSession} before the first loop.
     */
    public static void seed(CrawlerRobot.Config c) {
        trackWidthIn        = c.trackWidth;
        centerWheelOffsetIn = c.centerWheelOffset;
        wheelDiameterIn     = c.wheelDiameter;
        ticksPerRev         = c.ticksPerRev;

        driveKp = c.driveKp;  driveKi = c.driveKi;  driveKd = c.driveKd;
        strafeKp = c.strafeKp; strafeKi = c.strafeKi; strafeKd = c.strafeKd;
        steerP = c.steerP;    steerI = c.steerI;    steerD = c.steerD;
        minPower = c.minPower;

        moveSpeed            = c.defaultMoveSpeed;
        turnSpeed            = c.defaultTurnSpeed;
        followDistanceCm     = c.followDistanceCm;
        arrivalThresholdCm   = c.arrivalThresholdCm;
        orbitThresholdCm     = c.orbitThresholdCm;
        timeoutSecs          = c.timeoutSecs;
        maxDriveSpeed        = c.maxDriveSpeed;
        turnReferenceRadians = c.turnReferenceRadians;

        slowMoveSpeed        = c.slowMoveSpeed;
        slowTurnSpeed        = c.slowTurnSpeed;
        slowFollowDistanceCm = c.slowFollowDistanceCm;
        slowDownTurnRadians  = c.slowDownTurnRadians;
        slowDownTurnAmount   = c.slowDownTurnAmount;
    }

    /**
     * Builds a fresh {@link CrawlerRobot.Config} snapshot from the current static values.
     * Called every loop so Dashboard edits are picked up automatically.
     */
    static CrawlerRobot.Config toConfig() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.trackWidth        = trackWidthIn;
        c.centerWheelOffset = centerWheelOffsetIn;
        c.wheelDiameter     = wheelDiameterIn;
        c.ticksPerRev       = ticksPerRev;

        c.driveKp = driveKp;  c.driveKi = driveKi;  c.driveKd = driveKd;
        c.strafeKp = strafeKp; c.strafeKi = strafeKi; c.strafeKd = strafeKd;
        c.steerP = steerP;    c.steerI = steerI;    c.steerD = steerD;
        c.minPower = minPower;

        c.defaultMoveSpeed   = moveSpeed;
        c.defaultTurnSpeed   = turnSpeed;
        c.followDistanceCm   = followDistanceCm;
        c.arrivalThresholdCm = arrivalThresholdCm;
        c.orbitThresholdCm   = orbitThresholdCm;
        c.timeoutSecs        = timeoutSecs;
        c.maxDriveSpeed      = maxDriveSpeed;
        c.turnReferenceRadians = turnReferenceRadians;

        c.slowMoveSpeed        = slowMoveSpeed;
        c.slowTurnSpeed        = slowTurnSpeed;
        c.slowFollowDistanceCm = slowFollowDistanceCm;
        c.slowDownTurnRadians  = slowDownTurnRadians;
        c.slowDownTurnAmount   = slowDownTurnAmount;
        return c;
    }
}
