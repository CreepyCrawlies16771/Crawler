package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Streams the robot's pose to the FTC Dashboard <b>field view</b> during tuning
 * tests, so you can watch every test live in the browser while the telemetry
 * panel shows the numbers. Drawing is best-effort — safe to call every loop.
 */
final class TuningDashboard {

    private static boolean warnedOnce;

    private TuningDashboard() {}

    /** Draws the robot at its current odometry pose (x/y in cm, heading in radians). */
    static void drawRobot(CrawlerRobot robot) {
        drawRobot(robot.getPose().getX(), robot.getPose().getY(), robot.getPose().getHeading());
    }

    /** Draws the robot at an explicit pose. */
    static void drawRobot(double xCm, double yCm, double headingRad) {
        try {
            TelemetryPacket packet = new TelemetryPacket();
            DashboardFieldViewUtils.drawRobot(packet, xCm, yCm, headingRad,
                    DashboardFieldViewUtils.FieldColor.GREEN);
            FtcDashboard.getInstance().sendTelemetryPacket(packet);
        } catch (RuntimeException e) {
            // Dashboard not reachable — tuning still works without the field view.
            if (!warnedOnce) {
                warnedOnce = true;
                com.qualcomm.robotcore.util.RobotLog.ii("CrawlerTuner",
                        "FTC Dashboard field view unavailable: " + e);
            }
        }
    }
}
