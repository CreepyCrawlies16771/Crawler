package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * Builds the team's robot using its builder's own tuned values, so the Crawler tooling
 * (Crawler Tuner, System Test, Smoke Test) never hard-codes a robot class name.
 *
 * <p>This is a functional interface. Register your robot with
 * {@link CrawlerRobotRegistry#setProvider(CrawlerRobotProvider, CrawlerRobotConfigProvider)}
 * using a constructor or method reference, e.g. {@code MyRobot::new} — no anonymous
 * class needed.</p>
 *
 * @see CrawlerRobotConfigProvider for the variant that applies live tuning values
 */
@FunctionalInterface
public interface CrawlerRobotProvider {

    /**
     * Builds the robot using the builder's own tuned values.
     *
     * @param hwMap the OpMode's hardware map
     * @return a ready-to-drive instance of your {@link CrawlerRobot} subclass
     */
    CrawlerRobot create(HardwareMap hwMap);
}
