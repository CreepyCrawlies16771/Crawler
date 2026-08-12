package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Builds the tuning robot for {@link TuningSession}.
 *
 * <p>Implement this in the {@code CrawlerTuner} OpMode, typically backed by
 * {@link org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobotRegistry} so it
 * works with <b>any</b> {@link CrawlerRobot} subclass — whatever the team called it. The
 * tuner calls {@link #create()} once to read your builder's current values, then
 * {@link #create(CrawlerRobot.Config)} with the live tuning values whenever one changes,
 * so the odometry and PID constants are applied to a fresh robot.</p>
 */
public interface TuningRobotFactory {

    /**
     * Creates a reference robot built from the builder's own tuned values.
     *
     * <p>Used to seed {@link TuningConfig} so the tuner starts from your robot's current
     * config instead of hard-coded presets.</p>
     *
     * @return a ready-to-drive robot (drive motors, IMU, and localizer)
     */
    CrawlerRobot create();

    /**
     * Creates a {@link CrawlerRobot} built from the given tuning constants.
     *
     * @param config the current live tuning values (from {@link TuningConfig})
     * @return a ready-to-drive robot (drive motors, IMU, and localizer)
     */
    CrawlerRobot create(CrawlerRobot.Config config);
}
