package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CrawlerRobotRegistry} — the static hook the Crawler OpModes use to
 * build the team's robot without naming its class. The registered provider is returned
 * whatever it produces. No real robot or hardware is needed here: {@code HardwareMap}
 * can't be constructed in a JVM unit test (it needs an Android {@code Context}), so the
 * hardware map is passed as {@code null} and the recording provider returns {@code null}
 * — the tests assert that the registry delegates the exact arguments it was given.
 */
public class CrawlerRobotRegistryTest {

    private final CrawlerRobot.Config config = new CrawlerRobot.Config();

    @Before
    public void resetRegistry() {
        CrawlerRobotRegistry.clearForTesting();
    }

    @After
    public void resetRegistryAfter() {
        CrawlerRobotRegistry.clearForTesting();
    }

    @Test
    public void isRegistered_isFalseBeforeRegistration() {
        assertFalse(CrawlerRobotRegistry.isRegistered());
    }

    @Test
    public void isRegistered_isTrueAfterRegistration() {
        CrawlerRobotRegistry.setProvider(new RecordingProvider());
        assertTrue(CrawlerRobotRegistry.isRegistered());
    }

    @Test
    public void setProvider_rejectsNull() {
        try {
            CrawlerRobotRegistry.setProvider(null);
            fail("expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test
    public void create_withoutProvider_throwsActionableError() {
        try {
            CrawlerRobotRegistry.create(null);
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("CrawlerRobotRegistry"));
            assertTrue(expected.getMessage().contains("setProvider"));
        }
    }

    @Test
    public void createWithConfig_withoutProvider_throwsActionableError() {
        try {
            CrawlerRobotRegistry.create(null, config);
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("CrawlerRobotRegistry"));
            assertTrue(expected.getMessage().contains("setProvider"));
        }
    }

    @Test
    public void create_delegatesToProvider() {
        RecordingProvider provider = new RecordingProvider();
        CrawlerRobotRegistry.setProvider(provider);

        CrawlerRobot robot = CrawlerRobotRegistry.create(null);

        assertTrue(provider.createCalled);        // the provider is actually consulted
        assertNull(robot);                        // passthrough of the provider's result
        assertNull(provider.lastConfig);          // the single-arg overload forwards no config
    }

    @Test
    public void createWithConfig_delegatesConfigAndHardwareMap() {
        RecordingProvider provider = new RecordingProvider();
        CrawlerRobotRegistry.setProvider(provider);

        CrawlerRobotRegistry.create(null, config);

        assertTrue(provider.createCalled);
        assertTrue(provider.configCreateCalled);
        assertSame(config, provider.lastConfig);  // the exact config instance is forwarded
    }

    /** Records the arguments it was called with; returns null (no hardware in unit tests). */
    private static final class RecordingProvider implements CrawlerRobotProvider {

        boolean createCalled;
        boolean configCreateCalled;
        CrawlerRobot.Config lastConfig;

        @Override
        public CrawlerRobot create(HardwareMap hwMap) {
            this.createCalled = true;
            return null;
        }

        @Override
        public CrawlerRobot create(HardwareMap hwMap, CrawlerRobot.Config config) {
            this.createCalled = true;
            this.configCreateCalled = true;
            this.lastConfig = config;
            return null;
        }
    }
}
