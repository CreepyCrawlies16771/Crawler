package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
