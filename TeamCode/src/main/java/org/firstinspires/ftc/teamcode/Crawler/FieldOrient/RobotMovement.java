package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.MathFunctions;

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



    public static void goToPosition(double x, double y, double movementSpeed) {

        double distanceToTarget = Math.hypot(x-worldXPosition, y-worldYPosition);

       double absAngleToTarget = Math.atan2(y - worldYPosition, x - worldXPosition);

       double relativeAngleToPoint = MathFunctions.AngleWrap(absAngleToTarget - (worldAngle_radians - Math.toRadians(0)));


       double relativeXToPoint = Math.cos(relativeAngleToPoint) * distanceToTarget;
       double relativeYToPoint = Math.sin(relativeAngleToPoint) * distanceToTarget;


       double movementXPower = relativeXToPoint / (Math.abs(relativeXToPoint) + Math.abs(relativeYToPoint));

       double movementYPower = relativeYToPoint / (Math.abs(relativeXToPoint) + Math.abs(relativeYToPoint));

        //todo: get the data for the movement from here
    }
}
