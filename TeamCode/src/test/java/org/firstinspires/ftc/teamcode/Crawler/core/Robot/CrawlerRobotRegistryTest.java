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
 * build the team's robot without naming its class. The registered providers are returned
 * whatever they produce. No real robot or hardware is needed here: {@code HardwareMap}
 * can't be constructed in a JVM unit test (it needs an Android {@code Context}), so the
 * hardware map is passed as {@code null} and the recording providers return {@code null}
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
        CrawlerRobotRegistry.setProvider(new RecordingProvider(), new RecordingConfigProvider());
        assertTrue(CrawlerRobotRegistry.isRegistered());
    }

    @Test
    public void setProvider_rejectsNull() {
        try {
            CrawlerRobotRegistry.setProvider(null, new RecordingConfigProvider());
            fail("expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
        try {
            CrawlerRobotRegistry.setProvider(new RecordingProvider(), null);
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
        RecordingConfigProvider configProvider = new RecordingConfigProvider();
        CrawlerRobotRegistry.setProvider(provider, configProvider);

        CrawlerRobot robot = CrawlerRobotRegistry.create(null);

        assertTrue(provider.createCalled);         // the provider is actually consulted
        assertNull(robot);                         // passthrough of the provider's result
        assertFalse(configProvider.createCalled);  // the config overload is not consulted
    }

    @Test
    public void createWithConfig_delegatesConfigAndHardwareMap() {
        RecordingProvider provider = new RecordingProvider();
        RecordingConfigProvider configProvider = new RecordingConfigProvider();
        CrawlerRobotRegistry.setProvider(provider, configProvider);

        CrawlerRobotRegistry.create(null, config);

        assertTrue(configProvider.createCalled);       // the config builder is consulted
        assertSame(config, configProvider.lastConfig); // the exact config instance is forwarded
        assertFalse(provider.createCalled);            // the plain builder is not consulted
    }

    /** Records the arguments it was called with; returns null (no hardware in unit tests). */
    private static final class RecordingProvider implements CrawlerRobotProvider {

        boolean createCalled;

        @Override
        public CrawlerRobot create(HardwareMap hwMap) {
            this.createCalled = true;
            return null;
        }
    }

    /** Records the arguments it was called with; returns null (no hardware in unit tests). */
    private static final class RecordingConfigProvider implements CrawlerRobotConfigProvider {

        boolean createCalled;
        CrawlerRobot.Config lastConfig;

        @Override
        public CrawlerRobot create(HardwareMap hwMap, CrawlerRobot.Config config) {
            this.createCalled = true;
            this.lastConfig = config;
            return null;
        }
    }
}
