package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CurvePoint;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.MathFunctions;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Point;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class RobotMovement {

    private final CrawlerRobot robot;
    static double worldYPosition;
    static double worldXPosition;
    static double worldAngle_radians;
    public RobotMovement(CrawlerRobot robot) {
        this.robot = robot;
        worldYPosition = robot.devLocaliser.getPose().getY();
        worldXPosition = robot.devLocaliser.getPose().getX();
        worldAngle_radians = robot.devLocaliser.getPose().getHeading();
    }

    //TODO: Fix when end appeards, extend the last line by more than the follow distance. Get close to the target point, to last point of the list.
    public static void followCurve(ArrayList<CurvePoint> allPoints, double followAngle) {
        for (int i = 0; i < allPoints.size() - 1; i++) {
            /// Send information of line to debugger here (Lines)
        }

        CurvePoint followMe = getFollowPointPath(allPoints,
                new Point(worldXPosition, worldYPosition),
                allPoints.get(0).followDistance); // Todo: figure out the point on the path to set the best follow distance (Depending on speed and distance of the path...)
        /// Display more stuff here

        goToPosition(followMe.x, followMe.y, followMe.moveSpeed, followAngle, followMe.turnSpeed);
    }

    /**
     * This gets the best point from the path to follow/go to
     * @param pathPoints
     * @param robotLocation
     * @param followRadius
     * @return
     */
    public static CurvePoint getFollowPointPath(ArrayList<CurvePoint> pathPoints, Point robotLocation, double followRadius) {
        CurvePoint followMe = new CurvePoint(pathPoints.get(0));

        for(int i = 0; i < pathPoints.size() - 1; i++) {
            CurvePoint startLine = pathPoints.get(i);
            CurvePoint endLine = pathPoints.get(i +1);

            ArrayList<Point> intersections = MathFunctions.lineCircleIntersection(robotLocation, followRadius, startLine.toPoint(), endLine.toPoint());

            double closetsAngle = 10000000;

            for(Point thisIntersection: intersections) {
                double angel = Math.atan2(thisIntersection.y - worldYPosition, thisIntersection.x - worldXPosition);
                double deltaAngle = Math.abs(MathFunctions.AngleWrap(angel - worldAngle_radians));

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

        double relativeAngleToPoint = MathFunctions.AngleWrap(absAngleToTarget - (worldAngle_radians - Math.toRadians(0)));


        double relativeXToPoint = Math.cos(relativeAngleToPoint) * distanceToTarget;
        double relativeYToPoint = Math.sin(relativeAngleToPoint) * distanceToTarget;


        double movementXPower = relativeXToPoint / (Math.abs(relativeXToPoint) + Math.abs(relativeYToPoint));

        double movementYPower = relativeYToPoint / (Math.abs(relativeXToPoint) + Math.abs(relativeYToPoint));

        movementXPower *= movementSpeed;
        movementYPower *= movementSpeed;


        double relativeTurnAngle = relativeAngleToPoint - Math.toRadians(180) + preferredAngle;

        relativeTurnAngle /= MathFunctions.Clip(relativeTurnAngle/Math.toRadians(30), -1, 1) * turnSpeed;

        if(distanceToTarget < 10) { // If is is closer than 10 cm reduce the turning otherwise there is orbiting
            relativeTurnAngle *= 0;
        }
         //todo: get the data for the movement from here

    }
}
