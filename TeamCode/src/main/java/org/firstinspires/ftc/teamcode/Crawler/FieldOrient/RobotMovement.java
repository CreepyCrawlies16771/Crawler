package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;
import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Point;

import java.util.ArrayList;

public class RobotMovement {

    private final CrawlerRobot robot;
    static double worldYPosition;
    static double worldXPosition;
    static double worldAngle_radians;

    static TelemetryPacket packet = new TelemetryPacket();
    public RobotMovement(CrawlerRobot robot) {
        this.robot = robot;
        worldYPosition = robot.devLocaliser.getPose().getY();
        worldXPosition = robot.devLocaliser.getPose().getX();
        worldAngle_radians = robot.devLocaliser.getPose().getHeading();
    }

    //TODO: Fix when end appeards, extend the last line by more than the follow distance. Get close to the target point, to last point of the list.
    public static void followCurve(ArrayList<Waypoint> allPoints, double followAngle) {
        for (int i = 0; i < allPoints.size() - 1; i++) {
            DashboardFieldViewUtils.drawLine(packet, allPoints.get(i).x, allPoints.get(i).y, allPoints.get(i + 1).x, allPoints.get(i+ 1).y, DashboardFieldViewUtils.FieldColor.BLUE);
            //Displays the paths that are going to be followed
        }

        Waypoint followMe = getFollowPointPath(allPoints,
                new Point(worldXPosition, worldYPosition),
                allPoints.get(0).followDistance); // Todo: figure out the point on the path to set the best follow distance (Depending on speed and distance of the path...)

        DashboardFieldViewUtils.drawRobot(packet, followMe.x, followMe.y, followAngle, DashboardFieldViewUtils.FieldColor.RED); //Displays Robot on the field

        goToPosition(followMe.x, followMe.y, followMe.moveSpeed, followAngle, followMe.turnSpeed);
    }

    /**
     * This gets the best point from the path to follow/go to
     * @param pathPoints
     * @param robotLocation
     * @param followRadius
     * @return
     */
    public static Waypoint getFollowPointPath(ArrayList<Waypoint> pathPoints, Point robotLocation, double followRadius) {
        Waypoint followMe = new Waypoint(pathPoints.get(0));

        for(int i = 0; i < pathPoints.size() - 1; i++) {
            Waypoint startLine = pathPoints.get(i);
            Waypoint endLine = pathPoints.get(i +1);

            ArrayList<Point> intersections = CrawlerMath.lineCircleIntersection(robotLocation, followRadius, startLine.toPoint(), endLine.toPoint());

            double closetsAngle = 10000000;

            for(Point thisIntersection: intersections) {
                double angel = Math.atan2(thisIntersection.y - worldYPosition, thisIntersection.x - worldXPosition);
                double deltaAngle = Math.abs(CrawlerMath.wrapAngle(angel - worldAngle_radians));

                if(deltaAngle < closetsAngle) {
                    closetsAngle = deltaAngle;
                    followMe.setPoint(thisIntersection);
                }

            }
        }

        return followMe;
    }

    /**
     * This makes the robot go to a point on the field
     * @param x
     * @param y
     * @param movementSpeed
     * @param preferredAngle
     * @param turnSpeed
     */

    public static void goToPosition(double x, double y, double movementSpeed, double preferredAngle, double turnSpeed) {

        double distanceToTarget = Math.hypot(x-worldXPosition, y-worldYPosition);

        double absAngleToTarget = Math.atan2(y - worldYPosition, x - worldXPosition);

        double relativeAngleToPoint = CrawlerMath.wrapAngle(absAngleToTarget - (worldAngle_radians - Math.toRadians(0)));


        double relativeXToPoint = Math.cos(relativeAngleToPoint) * distanceToTarget;
        double relativeYToPoint = Math.sin(relativeAngleToPoint) * distanceToTarget;


        double v = Math.abs(relativeXToPoint) + Math.abs(relativeYToPoint);
        double movementXPower = relativeXToPoint / v;

        double movementYPower = relativeYToPoint / v;

        movementXPower *= movementSpeed;
        movementYPower *= movementSpeed;


        double relativeTurnAngle = relativeAngleToPoint - Math.toRadians(180) + preferredAngle;

        relativeTurnAngle /= CrawlerMath.clamp(relativeTurnAngle/Math.toRadians(30), -1, 1) * turnSpeed; //TODO: check this agiain

        if(distanceToTarget < 10) { // If is is closer than 10 cm reduce the turning otherwise there is orbiting
            relativeTurnAngle *= 0;
        }
         //todo: get the data for the movement from here

    }
}
