package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests for {@link CrawlerRobot.Config} — the pure tuning-constant container
 * shared by the builder, the tuner, and the pathing library.
 */
public class CrawlerRobotConfigTest {

    private static final double DELTA = 0.5;

    @Test
    public void ticksPerMeter_defaults() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        // wheelDiameter 1.37795 in = 0.03499993 m/rev -> 2000 / (0.03499993 * PI)
        assertEquals(18189.1, c.ticksPerMeter(), DELTA);
    }

    @Test
    public void ticksPerCm_isMetersDividedBy100() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        assertEquals(c.ticksPerMeter() / 100.0, c.ticksPerCm(), 1e-9);
    }

    @Test
    public void ticksPerMeter_customWheel() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.wheelDiameterIn = 2.0;
        c.ticksPerRev = 1000;
        assertEquals(6266.0, c.ticksPerMeter(), DELTA);
    }

    @Test
    public void ticksPerMeter_guardAgainstBadGeometry() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.wheelDiameterIn = 0;
        assertEquals(2000.0, c.ticksPerMeter(), 1e-9); // fallback value
    }

    @Test
    public void defaultConstants_areSane() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        assertTrue(c.trackWidthIn > 0);
        assertTrue(c.wheelDiameterIn > 0);
        assertTrue(c.ticksPerRev > 0);
        assertEquals(0.15, c.minPower, 1e-9);
        assertEquals(0.7, c.defaultMoveSpeed, 1e-9);
        assertEquals(25.4, c.followDistanceCm, 1e-9);
        assertEquals(5.0, c.timeoutSecs, 1e-9);
        assertEquals(1.0, c.maxDriveSpeed, 1e-9);
    }

    @Test
    public void fluentSetters_roundTrip() {
        CrawlerRobot.Config c = new CrawlerRobot.Config()
                .setWheelDiameterIn(2.0)
                .setTicksPerRev(1000.0)
                .setTrackWidthIn(14.5)
                .setCenterWheelOffsetIn(-7.0)
                .setTimeoutSecs(8.0)
                .setArrivalThresholdCm(3.0)
                .setOrbitThresholdCm(20.0)
                .setTurnReferenceRadians(0.6);
        assertEquals(2.0, c.wheelDiameterIn, 1e-9);
        assertEquals(1000.0, c.ticksPerRev, 1e-9);
        assertEquals(14.5, c.trackWidthIn, 1e-9);
        assertEquals(-7.0, c.centerWheelOffsetIn, 1e-9);
        assertEquals(8.0, c.timeoutSecs, 1e-9);
        assertEquals(3.0, c.arrivalThresholdCm, 1e-9);
        assertEquals(20.0, c.orbitThresholdCm, 1e-9);
        assertEquals(0.6, c.turnReferenceRadians, 1e-9);
    }

    @Test
    public void validate_acceptsDefaults() {
        new CrawlerRobot.Config().validate();   // must not throw
    }

    @Test
    public void validate_acceptsNegativeCenterOffset() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.centerWheelOffsetIn = -5.0;   // signed geometric offset is legal
        c.validate();
    }

    @Test
    public void validate_rejectsNonPositivePhysicalValues() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.wheelDiameterIn = 0;
        expectInvalidConfig(c, "wheelDiameterIn");
    }

    @Test
    public void validate_rejectsNonFiniteValues() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.trackWidthIn = Double.NaN;
        expectInvalidConfig(c, "trackWidthIn");

        CrawlerRobot.Config c2 = new CrawlerRobot.Config();
        c2.arrivalThresholdCm = Double.POSITIVE_INFINITY;
        expectInvalidConfig(c2, "arrivalThresholdCm");

        CrawlerRobot.Config c3 = new CrawlerRobot.Config();
        c3.centerWheelOffsetIn = Double.NaN;
        expectInvalidConfig(c3, "centerWheelOffsetIn");
    }

    @Test
    public void validate_rejectsOutOfRangeSpeeds() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.defaultMoveSpeed = 1.5;
        expectInvalidConfig(c, "defaultMoveSpeed");
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
