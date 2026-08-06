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
 * <p>Because the fields are static, they survive between OpMode runs (until the app
 * restarts). When you are happy with a set of values, press <b>Square</b> in the tuner
 * to print the matching <b>builder lines</b> and paste them into your robot's
 * builder chain (the shipped {@code MyRobot.builder()} example follows this convention).</p>
 */
@Config("Crawler Tuner")
public final class TuningConfig {

    // --- Odometry -----------------------------------------------------------
    public static double trackWidthIn        = 13.0;
    public static double centerWheelOffsetIn = 3.5;
    public static double wheelDiameterIn     = 1.37795;
    public static double ticksPerRev         = 2000;

    // --- Robot-relative PID -------------------------------------------------
    public static double driveKp  = 0.05;
    public static double driveKi  = 0.0;
    public static double driveKd  = 0.0;

    public static double strafeKp = 0.05;
    public static double strafeKi = 0.0;
    public static double strafeKd = 0.0;

    public static double steerP   = 0.03;
    public static double steerI   = 0.0;
    public static double steerD   = 0.0;

    /** Smallest motor power that still moves the robot (friction deadband). */
    public static double minPower = 0.15;

    // --- Path following -----------------------------------------------------
    public static double moveSpeed          = 0.7;
    public static double turnSpeed          = 0.4;
    public static double followDistanceCm   = 25.4;
    public static double arrivalThresholdCm = 5.0;
    public static double orbitThresholdCm   = 25.4;
    public static double timeoutSecs        = 5.0;
    public static double maxDriveSpeed      = 1.0;

    private TuningConfig() {}

    /**
     * Builds a fresh {@link CrawlerRobot.Config} snapshot from the current static values.
     * Called every loop so Dashboard edits are picked up automatically.
     */
    static CrawlerRobot.Config toConfig() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.trackWidthIn        = trackWidthIn;
        c.centerWheelOffsetIn = centerWheelOffsetIn;
        c.wheelDiameterIn     = wheelDiameterIn;
        c.ticksPerRev         = ticksPerRev;

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
        return c;
    }
}
