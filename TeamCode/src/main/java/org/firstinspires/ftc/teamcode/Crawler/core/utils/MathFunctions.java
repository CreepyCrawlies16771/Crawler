package org.firstinspires.ftc.teamcode.Crawler.core.utils;

public class MathFunctions {

    /**
     * Makes shore an angel is within the range -180 to 180 degrees
     * @param angle
     * @return
     */

    public static double AngleWrap(double angle) {

        while(Math.toRadians(angle) < -Math.PI) angle += 2 * Math.PI;

        while(Math.toRadians(angle) > Math.PI) angle -= 2 * Math.PI;

        return angle;
    }
}
