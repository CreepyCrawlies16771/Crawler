package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Point;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Vector2d;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.ArrayList;
import java.util.List;

public class RobotMovement {

    private final CrawlerRobot robot;

    private double worldX;
    private double worldY;
    private double worldHeading;

    public RobotMovement(CrawlerRobot robot) {
        this.robot = robot;
        updatePose();
    }

    /***
     * The entry point for the Pure Persuit follower.
     * @param allPoints
     * @param followAngle
     */

    public void follow(List<Waypoint> allPoints, double followAngle) {
        updatePose();

        TelemetryPacket packet = new TelemetryPacket();

        for (int i = 0; i < allPoints.size() - 1; i++) {
            DashboardFieldViewUtils.drawLine(packet,
                    allPoints.get(i).x, allPoints.get(i).y,
                    allPoints.get(i + 1).x, allPoints.get(i + 1).y,
                    DashboardFieldViewUtils.FieldColor.BLUE);
        }
        List<Waypoint> extended = extendPath(allPoints);

        double dynamicFollowDistance = getDynamicFollowDistance(extended);

        Waypoint followMe = getFollowPointPath(extended,
                new Vector2d(worldX, worldY).toPoint(),
                dynamicFollowDistance);

        DashboardFieldViewUtils.drawRobot(packet,
                followMe.x, followMe.y, followAngle,
                DashboardFieldViewUtils.FieldColor.RED);

        goToPosition(followMe.x, followMe.y, followMe.moveSpeed, followAngle, followMe.turnSpeed);
    }

    /***
     * Path extension — fixes the "robot stops short" problem
     * Extends the final segment by followDistance beyond the last waypoint
     * @param original
     * @return
     */

    private List<Waypoint> extendPath(List<Waypoint> original) {
        if (original.size() < 2) return original;

        List<Waypoint> extended = new ArrayList<>(original);

        Waypoint last       = original.get(original.size() - 1);
        Waypoint secondLast = original.get(original.size() - 2);

        double dx = last.x - secondLast.x;
        double dy = last.y - secondLast.y;
        double len = Math.hypot(dx, dy);

        if (len < 1e-6) return extended;

        double extendBy = last.followDistance;
        double extraX = last.x + (dx / len) * extendBy;
        double extraY = last.y + (dy / len) * extendBy;

        extended.add(Waypoint.at(extraX, extraY)
                .speed(last.moveSpeed)
                .turnSpeed(last.turnSpeed)
                .followDistance(last.followDistance)
                .build());

        return extended;
    }

    /***
     * Dynamic lookahead — fixes jitter from always using first waypoint's radius
     * Finds the closest segment to the robot and uses its followDistance
     * @param points
     * @return
     */

    private double getDynamicFollowDistance(List<Waypoint> points) {
        double closestDist  = Double.MAX_VALUE;
        double followRadius = points.get(0).followDistance;

        for (int i = 0; i < points.size() - 1; i++) {
            Waypoint a = points.get(i);
            Waypoint b = points.get(i + 1);

            double midX = (a.x + b.x) / 2.0;
            double midY = (a.y + b.y) / 2.0;
            double dist = Math.hypot(midX - worldX, midY - worldY);

            if (dist < closestDist) {
                closestDist  = dist;
                followRadius = b.followDistance;
            }
        }

        return followRadius;
    }

    /***
     * Lookahead point — finds the best intersection on the path circle
     * @param pathPoints
     * @param robotLocation
     * @param followRadius
     * @return
     */

    public Waypoint getFollowPointPath(List<Waypoint> pathPoints,
                                       Point robotLocation,
                                       double followRadius) {
        Waypoint followMe = new Waypoint(pathPoints.get(0));

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Waypoint startLine = pathPoints.get(i);
            Waypoint endLine   = pathPoints.get(i + 1);

            List<Point> intersections = CrawlerMath.lineCircleIntersection(
                    robotLocation, followRadius,
                    startLine.toPoint(), endLine.toPoint()
            );

            double closestAngle = Double.MAX_VALUE;

            for (Point intersection : intersections) {
                double angle      = Math.atan2(intersection.y - worldY, intersection.x - worldX);
                double deltaAngle = Math.abs(CrawlerMath.wrapAngle(angle - worldHeading));

                if (deltaAngle < closestAngle) {
                    closestAngle = deltaAngle;
                    followMe = new Waypoint.Builder(intersection.x, intersection.y)
                            .speed(endLine.moveSpeed)
                            .turnSpeed(endLine.turnSpeed)
                            .followDistance(endLine.followDistance)
                            .build();
                }
            }
        }

        return followMe;
    }

    /***
     * goToPosition — computes and applies motor powers
     * @param x
     * @param y
     * @param moveSpeed
     * @param preferredAngle
     * @param turnSpeed
     */

    public void goToPosition(double x, double y,
                             double moveSpeed,
                             double preferredAngle,
                             double turnSpeed) {

        double distanceToTarget = Math.hypot(x - worldX, y - worldY);
        double absAngleToTarget = Math.atan2(y - worldY, x - worldX);
        double relativeAngle    = CrawlerMath.wrapAngle(absAngleToTarget - worldHeading);

        double relativeX = Math.cos(relativeAngle) * distanceToTarget;
        double relativeY = Math.sin(relativeAngle) * distanceToTarget;

        double scale          = Math.abs(relativeX) + Math.abs(relativeY);
        double movementXPower = (scale > 1e-6) ? (relativeX / scale) * moveSpeed : 0;
        double movementYPower = (scale > 1e-6) ? (relativeY / scale) * moveSpeed : 0;

        // FIX: turn angle was inverted — preferredAngle - relativeAngle
        // not relativeAngle - 180 + preferredAngle
        double relativeTurnAngle = CrawlerMath.wrapAngle(preferredAngle - relativeAngle);

        // FIX: orbit — scale turnPower by distance so it fades naturally
        // instead of cutting off abruptly at a hardcoded threshold
        double orbitScale = CrawlerMath.clamp(
                distanceToTarget / RobotConfig.FieldOriented.ORBIT_THRESHOLD, 0, 1
        );

        double turnPower = CrawlerMath.clamp(
                (relativeTurnAngle / Math.toRadians(30)) * turnSpeed, -1, 1
        ) * orbitScale;



    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void updatePose() {
        robot.localiser.update();
        worldX       = robot.localiser.getPose().getX();
        worldY       = robot.localiser.getPose().getY();
        worldHeading = robot.localiser.getPose().getHeading();
    }

    public double getWorldX()       { return worldX; }
    public double getWorldY()       { return worldY; }
    public double getWorldHeading() { return worldHeading; }
}