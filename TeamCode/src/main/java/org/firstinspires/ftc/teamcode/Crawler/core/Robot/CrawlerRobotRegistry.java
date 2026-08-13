package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.Objects;

/**
 * Static hook that lets the Crawler tooling build <i>your</i> robot without knowing its
 * class name. The shipped OpModes (Crawler Tuner, System Test, Smoke Test) call
 * {@link #create(HardwareMap)} / {@link #create(HardwareMap, CrawlerRobot.Config)} and
 * never reference a concrete robot class.
 *
 * <p>Register your robot once, e.g. in a static block of your robot class:</p>
 * <pre>{@code
 * static {
 *     CrawlerRobotRegistry.setProvider(
 *         MyRobot::new,
 *         (hwMap, config) -> MyRobot.builder(hwMap).withConfig(config).build()
 *     );
 * }
 * }</pre>
 *
 * <p>The registration lives in the app process, so it survives between OpMode runs. Run
 * any OpMode that builds your robot once after deploying (your TeleOp does this), and
 * the tooling will find it. If nothing is registered, the tooling fails with a clear,
 * actionable message instead of crashing on a hard-coded example class.</p>
 */
public final class CrawlerRobotRegistry {

    private static CrawlerRobotProvider provider;
    private static CrawlerRobotConfigProvider configProvider;

    private CrawlerRobotRegistry() {}

    /**
     * Registers the team's robot builders. Call once, typically from a static block in
     * your robot class.
     *
     * @param provider       builds the robot with the builder's own tuned values (e.g. {@code MyRobot::new})
     * @param configProvider builds the robot applying live tuning values (e.g. {@code (hwMap, config) -> MyRobot.builder(hwMap).withConfig(config).build()})
     */
    public static void setProvider(CrawlerRobotProvider provider, CrawlerRobotConfigProvider configProvider) {
        CrawlerRobotRegistry.provider = Objects.requireNonNull(provider, "provider cannot be null");
        CrawlerRobotRegistry.configProvider = Objects.requireNonNull(configProvider, "configProvider cannot be null");
    }

    /** Whether a robot has been registered yet. */
    public static boolean isRegistered() {
        return provider != null && configProvider != null;
    }

    /** Resets the registered provider (used by unit tests). */
    static void clearForTesting() {
        provider = null;
        configProvider = null;
    }

    /** Builds the registered robot using its builder's own tuned values. */
    public static CrawlerRobot create(HardwareMap hwMap) {
        return requireProvider().create(hwMap);
    }

    /** Builds the registered robot with the given tuning config applied (used by the tuner). */
    public static CrawlerRobot create(HardwareMap hwMap, CrawlerRobot.Config config) {
        return requireConfigProvider().create(hwMap, config);
    }

    private static CrawlerRobotProvider requireProvider() {
        requireRegistered();
        return provider;
    }

    private static CrawlerRobotConfigProvider requireConfigProvider() {
        requireRegistered();
        return configProvider;
    }

    private static void requireRegistered() {
        if (provider == null || configProvider == null) {
            throw new IllegalStateException(
                    "No robot is registered with CrawlerRobotRegistry. Add a static block to your "
                            + "robot class (the one extending CrawlerRobot) that calls "
                            + "CrawlerRobotRegistry.setProvider(...) — see docs/setup.md — then run any "
                            + "of your robot's OpModes once so it gets registered, and run this test again.");
        }
    }
}
