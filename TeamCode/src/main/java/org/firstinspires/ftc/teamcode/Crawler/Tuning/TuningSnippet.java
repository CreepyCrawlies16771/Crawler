package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Builds copy-paste text for the team robot's {@code builder()} method — the values
 * are printed as <b>builder-chain lines</b> (e.g. {@code .setTrackWidth(13.0)}), not
 * as field assignments, because the tuned config lives in the builder chain itself.
 */
final class TuningSnippet {

    private TuningSnippet() {}

    static String format(CrawlerRobot.Config c) {
        String slowSpeeds = (c.slowMoveSpeed > 0 || c.slowTurnSpeed > 0 || c.slowFollowDistanceCm > 0)
                ? ".slowSpeeds(" + fmt(c.slowMoveSpeed) + ", " + fmt(c.slowTurnSpeed)
                + ", " + fmt(c.slowFollowDistanceCm) + ")"
                : "";
        String slowDown = (c.slowDownTurnRadians > 0 || c.slowDownTurnAmount > 0)
                ? ".slowDownTurn(" + fmt(c.slowDownTurnRadians) + ", " + fmt(c.slowDownTurnAmount) + ")"
                : "";
        String body = ""
                + "// Paste into your robot's builder(), replacing the tuned values below:\n"
                + ".setTrackWidth(" + fmt(c.trackWidth) + ")\n"
                + ".setCenterWheelOffset(" + fmt(c.centerWheelOffset) + ")\n"
                + ".wheelDiameter(" + fmt(c.wheelDiameter) + ")\n"
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
                + ".turnReferenceRadians(" + fmt(c.turnReferenceRadians) + ")\n"
                + ".maxDriveSpeed(" + fmt(c.maxDriveSpeed) + ")";
        if (!slowSpeeds.isEmpty()) body += "\n" + slowSpeeds;
        if (!slowDown.isEmpty())  body += "\n" + slowDown;
        return body;
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.4f", v);
    }
}
