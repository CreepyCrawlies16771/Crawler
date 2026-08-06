package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Builds the tuning robot for {@link TuningSession}.
 *
 * <p>Implement this in your team code (see {@code TeamscodeNotLibrary/CrawlerTuner.java}),
 * typically as {@code config -> MyRobot.buildTuned(hardwareMap, config)} — but
 * <b>any</b> class that extends {@link CrawlerRobot} works, whatever you name it. The
 * tuner calls {@link #create} with the live tuning values whenever one changes, so the
 * odometry and PID constants are applied to a fresh robot.</p>
 */
public interface TuningRobotFactory {

    /**
     * Creates a {@link CrawlerRobot} built from the given tuning constants.
     *
     * @param config the current live tuning values (from {@link TuningConfig})
     * @return a ready-to-drive robot (drive motors, IMU, and localizer)
     */
    CrawlerRobot create(CrawlerRobot.Config config);
}
