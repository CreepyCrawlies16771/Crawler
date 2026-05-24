package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/** Builds copy-paste text for {@code TeamscodeNotLibrary/MyRobot.java}. */
final class MyRobotSnippet {

    private MyRobotSnippet() {}

    static String format(CrawlerRobot.Config c) {
        return ""
                + "// Paste into MyRobot constructor builder:\n"
                + ".setTrackWidth(" + fmt(c.trackWidthIn) + ")\n"
                + ".setCenterWheelOffset(" + fmt(c.centerWheelOffsetIn) + ")\n"
                + ".wheelDiameter(" + fmt(c.wheelDiameterIn) + ")\n"
                + ".ticksPerRev(" + (int) Math.round(c.ticksPerRev) + ")\n"
                + ".drivePid(" + fmt(c.driveKp) + ", " + fmt(c.driveKi) + ", " + fmt(c.driveKd) + ")\n"
                + ".strafePid(" + fmt(c.strafeKp) + ", " + fmt(c.strafeKi) + ", " + fmt(c.strafeKd) + ")\n"
                + ".steerPid(" + fmt(c.steerP) + ", " + fmt(c.steerI) + ", " + fmt(c.steerD) + ")\n"
                + ".pathDefaults(" + fmt(c.defaultMoveSpeed) + ", " + fmt(c.defaultTurnSpeed)
                + ", " + fmt(c.followDistanceCm) + ")\n"
                + ".arrivalThresholdCm(" + fmt(c.arrivalThresholdCm) + ")";
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.4f", v);
    }
}
