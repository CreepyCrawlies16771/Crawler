package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Builds the team's robot applying a given tuning config on top of the builder.
 *
 * <p>Used by the Crawler Tuner, which rebuilds the robot whenever a live tuning value
 * changes. A typical implementation is
 * {@code (hwMap, config) -> MyRobot.builder(hwMap).withConfig(config).build()}.</p>
 *
 * <p>This is a functional interface. Pass a lambda to
 * {@link CrawlerRobotRegistry#setProvider(CrawlerRobotProvider, CrawlerRobotConfigProvider)}
 * — no anonymous class needed.</p>
 *
 * @see CrawlerRobotProvider for the variant that uses the builder's own tuned values
 */
@FunctionalInterface
public interface CrawlerRobotConfigProvider {

    /**
     * Builds the robot applying the given tuning config on top of the builder.
     *
     * @param hwMap  the OpMode's hardware map
     * @param config the current live tuning values to apply
     * @return a ready-to-drive instance of your {@link CrawlerRobot} subclass
     */
    CrawlerRobot create(HardwareMap hwMap, CrawlerRobot.Config config);
}
