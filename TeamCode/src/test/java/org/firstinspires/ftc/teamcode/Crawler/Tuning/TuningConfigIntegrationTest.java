package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.junit.Test;

/**
 * Integration test for the tuner's config lifecycle: a robot's builder config must
 * round-trip through {@link TuningConfig#seed} → {@link TuningConfig#toConfig} without
 * losing a single value, and {@link TuningSnippet} must print it back as builder lines.
 */
public class TuningConfigIntegrationTest {

    /** A fully-tuned config (exactly what a tuned robot's builder would produce). */
    private static CrawlerRobot.Config tunedConfig() {
        CrawlerRobot.Config c = new CrawlerRobot.Config();
        c.trackWidth = 13.0;
        c.centerWheelOffset = 3.5;
        c.wheelDiameter = 1.37795;
        c.ticksPerRev = 2000;
        c.driveKp = 0.05;  c.driveKi = 0.001; c.driveKd = 0.002;
        c.strafeKp = 0.06; c.strafeKi = 0.0;  c.strafeKd = 0.0;
        c.steerP = 0.03;   c.steerI = 0.0;    c.steerD = 0.0;
        c.minPower = 0.15;
        c.defaultMoveSpeed = 0.7;
        c.defaultTurnSpeed = 0.4;
        c.followDistanceCm = 25.4;
        c.arrivalThresholdCm = 5.0;
        c.orbitThresholdCm = 25.4;
        c.timeoutSecs = 5.0;
        c.maxDriveSpeed = 1.0;
        c.turnReferenceRadians = Math.toRadians(30);
        c.slowMoveSpeed = 0.3;
        c.slowTurnSpeed = 0.2;
        c.slowFollowDistanceCm = 12.7;
        c.slowDownTurnRadians = 0.5;
        c.slowDownTurnAmount = 0.5;
        return c;
    }

    @Test
    public void seed_then_toConfig_roundTripsEveryValue() {
        CrawlerRobot.Config original = tunedConfig();
        TuningConfig.seed(original);

        CrawlerRobot.Config rt = TuningConfig.toConfig();
        assertEquals(original.trackWidth, rt.trackWidth, 1e-9);
        assertEquals(original.centerWheelOffset, rt.centerWheelOffset, 1e-9);
        assertEquals(original.wheelDiameter, rt.wheelDiameter, 1e-9);
        assertEquals(original.ticksPerRev, rt.ticksPerRev, 1e-9);
        assertEquals(original.driveKp, rt.driveKp, 1e-9);
        assertEquals(original.driveKi, rt.driveKi, 1e-9);
        assertEquals(original.driveKd, rt.driveKd, 1e-9);
        assertEquals(original.strafeKp, rt.strafeKp, 1e-9);
        assertEquals(original.strafeKi, rt.strafeKi, 1e-9);
        assertEquals(original.strafeKd, rt.strafeKd, 1e-9);
        assertEquals(original.steerP, rt.steerP, 1e-9);
        assertEquals(original.steerI, rt.steerI, 1e-9);
        assertEquals(original.steerD, rt.steerD, 1e-9);
        assertEquals(original.minPower, rt.minPower, 1e-9);
        assertEquals(original.defaultMoveSpeed, rt.defaultMoveSpeed, 1e-9);
        assertEquals(original.defaultTurnSpeed, rt.defaultTurnSpeed, 1e-9);
        assertEquals(original.followDistanceCm, rt.followDistanceCm, 1e-9);
        assertEquals(original.arrivalThresholdCm, rt.arrivalThresholdCm, 1e-9);
        assertEquals(original.orbitThresholdCm, rt.orbitThresholdCm, 1e-9);
        assertEquals(original.timeoutSecs, rt.timeoutSecs, 1e-9);
        assertEquals(original.maxDriveSpeed, rt.maxDriveSpeed, 1e-9);
        assertEquals(original.turnReferenceRadians, rt.turnReferenceRadians, 1e-9);
        assertEquals(original.slowMoveSpeed, rt.slowMoveSpeed, 1e-9);
        assertEquals(original.slowTurnSpeed, rt.slowTurnSpeed, 1e-9);
        assertEquals(original.slowFollowDistanceCm, rt.slowFollowDistanceCm, 1e-9);
        assertEquals(original.slowDownTurnRadians, rt.slowDownTurnRadians, 1e-9);
        assertEquals(original.slowDownTurnAmount, rt.slowDownTurnAmount, 1e-9);
    }

    @Test
    public void snippet_printsTheTunedValuesAsBuilderLines() {
        String snippet = TuningSnippet.format(tunedConfig());

        assertTrue(snippet.contains("Paste into your robot's builder()"));
        assertTrue(snippet.contains(".setTrackWidth(13.0000)"));
        assertTrue(snippet.contains(".wheelDiameter(1.3780)"));
        assertTrue(snippet.contains(".ticksPerRev(2000)"));
        assertTrue(snippet.contains(".turnReferenceRadians("));
        assertTrue(snippet.contains(".slowSpeeds(0.3000, 0.2000, 12.7000)"));
        assertTrue(snippet.contains(".slowDownTurn(0.5000, 0.5000)"));
    }

    @Test
    public void snippet_omitsUnusedSlowLines() {
        String snippet = TuningSnippet.format(new CrawlerRobot.Config());   // nothing set
        assertFalse(snippet.contains(".slowSpeeds("));
        assertFalse(snippet.contains(".slowDownTurn("));
    }
}
