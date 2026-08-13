package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests for {@link CrawlerRobot.Config} — the pure tuning-constant container
 * shared by the builder, the tuner, and the pathing library.
 *
 * <p>The config has <b>no presets</b>: every value starts at 0 and must be set by the
 * robot's builder before {@code validate()} (and thus {@code build()}) will pass.</p>
 */
public class CrawlerRobotConfigTest {

    private static final double DELTA = 0.5;

    /** A fully-configured config that passes {@code validate()}. */
    private static CrawlerRobot.Config validConfig() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.trackWidth = 13.0;
        c.centerWheelOffset = 3.5;
        c.wheelDiameter = 1.37795;
        c.ticksPerRev = 2000;
        c.minPower = 0.15;
        c.defaultMoveSpeed = 0.7;
        c.defaultTurnSpeed = 0.4;
        c.followDistanceCm = 25.4;
        c.arrivalThresholdCm = 5.0;
        c.orbitThresholdCm = 25.4;
        c.timeoutSecs = 5.0;
        c.maxDriveSpeed = 1.0;
        c.turnReferenceRadians = Math.toRadians(30);
        return c;
    }

    @Test
    public void defaults_areUnset() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        assertEquals(0.0, c.trackWidth, 1e-9);
        assertEquals(0.0, c.centerWheelOffset, 1e-9);
        assertEquals(0.0, c.wheelDiameter, 1e-9);
        assertEquals(0.0, c.ticksPerRev, 1e-9);
        assertEquals(0.0, c.driveKp, 1e-9);
        assertEquals(0.0, c.minPower, 1e-9);
        assertEquals(0.0, c.defaultMoveSpeed, 1e-9);
        assertEquals(0.0, c.defaultTurnSpeed, 1e-9);
        assertEquals(0.0, c.followDistanceCm, 1e-9);
        assertEquals(0.0, c.arrivalThresholdCm, 1e-9);
        assertEquals(0.0, c.timeoutSecs, 1e-9);
        assertEquals(0.0, c.maxDriveSpeed, 1e-9);
        assertEquals(0.0, c.turnReferenceRadians, 1e-9);
    }

    @Test
    public void ticksPerMeter_computes() {
        CrawlerRobot.Config c = validConfig();
        // wheelDiameter 1.37795 in = 0.03499993 m/rev -> 2000 / (0.03499993 * PI)
        assertEquals(18189.1, c.ticksPerMeter(), DELTA);
    }

    @Test
    public void ticksPerMeter_unconfigured_isNaN() {
        // No presets -> nothing to compute from until the builder sets the values.
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        assertTrue(Double.isNaN(c.ticksPerMeter()));
    }

    @Test
    public void ticksPerCm_isMetersDividedBy100() {
        CrawlerRobot.Config c = validConfig();
        assertEquals(c.ticksPerMeter() / 100.0, c.ticksPerCm(), 1e-9);
    }

    @Test
    public void ticksPerMeter_customWheel() {
        CrawlerRobot.Config c = validConfig();
        c.wheelDiameter = 2.0;
        c.ticksPerRev = 1000;
        assertEquals(6266.0, c.ticksPerMeter(), DELTA);
    }

    @Test
    public void fluentSetters_roundTrip() {
        CrawlerRobot.Config c = new CrawlerRobot.Config()
                .setWheelDiameter(2.0)
                .setTicksPerRev(1000.0)
                .setTrackWidth(14.5)
                .setCenterWheelOffset(-7.0)
                .setTimeoutSecs(8.0)
                .setArrivalThresholdCm(3.0)
                .setOrbitThresholdCm(20.0)
                .setTurnReferenceRadians(0.6);
        assertEquals(2.0, c.wheelDiameter, 1e-9);
        assertEquals(1000.0, c.ticksPerRev, 1e-9);
        assertEquals(14.5, c.trackWidth, 1e-9);
        assertEquals(-7.0, c.centerWheelOffset, 1e-9);
        assertEquals(8.0, c.timeoutSecs, 1e-9);
        assertEquals(3.0, c.arrivalThresholdCm, 1e-9);
        assertEquals(20.0, c.orbitThresholdCm, 1e-9);
        assertEquals(0.6, c.turnReferenceRadians, 1e-9);
    }

    @Test
    public void validate_acceptsFullyConfigured() {
        validConfig().validate();   // must not throw
    }

    @Test
    public void validate_rejectsUnsetConfig() {
        // A fresh config has no presets, so validation must reject it.
        expectInvalidConfig(new CrawlerRobot.Config(), "trackWidth");
    }

    @Test
    public void validate_acceptsNegativeCenterOffset() {
        CrawlerRobot.Config c = validConfig();
        c.centerWheelOffset = -5.0;   // signed geometric offset is legal
        c.validate();
    }

    @Test
    public void validate_rejectsNonPositivePhysicalValues() {
        CrawlerRobot.Config c = validConfig();
        c.wheelDiameter = 0;
        expectInvalidConfig(c, "wheelDiameter");
    }

    @Test
    public void validate_rejectsNonFiniteValues() {
        CrawlerRobot.Config c = validConfig();
        c.trackWidth = Double.NaN;
        expectInvalidConfig(c, "trackWidth");

        CrawlerRobot.Config c2 = validConfig();
        c2.arrivalThresholdCm = Double.POSITIVE_INFINITY;
        expectInvalidConfig(c2, "arrivalThresholdCm");

        CrawlerRobot.Config c3 = validConfig();
        c3.centerWheelOffset = Double.NaN;
        expectInvalidConfig(c3, "centerWheelOffsetIn");
    }

    @Test
    public void validate_rejectsOutOfRangeSpeeds() {
        CrawlerRobot.Config c = validConfig();
        c.defaultMoveSpeed = 1.5;
        expectInvalidConfig(c, "defaultMoveSpeed");
    }

    @Test
    public void slowValues_areOptional() {
        // Slow-down values may be left at 0 (not used) without failing validation.
        validConfig().validate();
    }

    private static void expectInvalidConfig(CrawlerRobot.Config c, String fieldName) {
        try {
            c.validate();
            fail("expected IllegalArgumentException naming " + fieldName);
        } catch (IllegalArgumentException expected) {
            assertTrue("message should name the field: " + expected.getMessage(),
                    expected.getMessage().contains(fieldName));
        }
    }
}
