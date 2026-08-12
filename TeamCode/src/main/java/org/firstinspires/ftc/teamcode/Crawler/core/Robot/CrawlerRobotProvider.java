package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Supplies the team's robot to Crawler tooling (Crawler Tuner, System Test, Smoke Test)
 * without the tooling hard-coding a specific robot class.
 *
 * <p>Implement this in your own robot class (or a small wrapper around it) and register
 * it once with {@link CrawlerRobotRegistry}. The tooling then builds <i>your</i> robot —
 * whatever you called it — instead of a shipped example.</p>
 */
public interface CrawlerRobotProvider {

    /**
     * Builds the robot using the builder's own tuned values.
     *
     * @param hwMap the OpMode's hardware map
     * @return a ready-to-drive instance of your {@link CrawlerRobot} subclass
     */
    CrawlerRobot create(HardwareMap hwMap);

    /**
     * Builds the robot applying the given tuning config on top of the builder.
     *
     * <p>Used by the Crawler Tuner, which rebuilds the robot whenever a live tuning
     * value changes. A typical implementation is
     * {@code return MyRobot.builder(hwMap).withConfig(config).build();}.</p>
     *
     * @param hwMap  the OpMode's hardware map
     * @param config the current live tuning values to apply
     * @return a ready-to-drive instance of your {@link CrawlerRobot} subclass
     */
    CrawlerRobot create(HardwareMap hwMap, CrawlerRobot.Config config);
}
