package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Builds copy-paste text for the team robot's {@code builder()} method — the values
 * are printed as <b>builder-chain lines</b> (e.g. {@code .setTrackWidth(13.0)}), not
 * as field assignments, because the tuned config lives in the builder chain itself
 * (the shipped {@code MyRobot} example follows this convention).
 */
final class MyRobotSnippet {

    private MyRobotSnippet() {}

    static String format(CrawlerRobot.Config c) {
        return ""
                + "// Paste into MyRobot.builder(), replacing the tuned values below:\n"
                + ".setTrackWidth(" + fmt(c.trackWidthIn) + ")\n"
                + ".setCenterWheelOffset(" + fmt(c.centerWheelOffsetIn) + ")\n"
                + ".wheelDiameter(" + fmt(c.wheelDiameterIn) + ")\n"
                + ".ticksPerRev(" + (int) Math.round(c.ticksPerRev) + ")\n"
                + ".drivePid(" + fmt(c.driveKp) + ", " + fmt(c.driveKi) + ", " + fmt(c.driveKd) + ")\n"
                + ".strafePid(" + fmt(c.strafeKp) + ", " + fmt(c.strafeKi) + ", " + fmt(c.strafeKd) + ")\n"
                + ".steerPid(" + fmt(c.steerP) + ", " + fmt(c.steerI) + ", " + fmt(c.steerD) + ")\n"
                + ".minPower(" + fmt(c.minPower) + ")\n"
                + ".pathDefaults(" + fmt(c.defaultMoveSpeed) + ", " + fmt(c.defaultTurnSpeed)
                + ", " + fmt(c.followDistanceCm) + ")\n"
                + ".arrivalThresholdCm(" + fmt(c.arrivalThresholdCm) + ")\n"
                + ".orbitThresholdCm(" + fmt(c.orbitThresholdCm) + ")\n"
                + ".timeoutSecs(" + fmt(c.timeoutSecs) + ")\n"
                + ".maxDriveSpeed(" + fmt(c.maxDriveSpeed) + ")";
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.4f", v);
    }
}
