package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import java.util.ArrayList;

public class CrawlerMath {

    /**
     * Makes sure an angle is within the range -180 to 180 degrees
     */
    public static double wrapAngle(double angle) {

        while (angle < -180) angle += 360;
        while (angle > 180) angle -= 360;

        return angle;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static ArrayList<Point> lineCircleIntersection(
            Point circleCenter,
            double radius,
            Point linePointA,
            Point linePointB
    ) {

        if (Math.abs(linePointA.y - linePointB.y) < 0.003) {
            linePointA.y = linePointB.y + 0.003;
        }

        if (Math.abs(linePointA.x - linePointB.x) < 0.003) {
            linePointA.x = linePointB.x + 0.003;
        }

        double m1 = (linePointB.y - linePointA.y) / (linePointB.x - linePointA.x);

        double quadraticA = 1.0 + Math.pow(m1, 2);

        double x1 = linePointA.x - circleCenter.x;
        double y1 = linePointA.y - circleCenter.y;

        double quadraticB = (2.0 * m1 * y1) - (2.0 * Math.pow(m1, 2) * x1);

        double quadraticC = (Math.pow(m1, 2) * Math.pow(x1, 2))
                - (2.0 * y1 * m1 * x1)
                - Math.pow(radius, 2);

        ArrayList<Point> allPoints = new ArrayList<>();

        try {
            double discriminant = Math.pow(quadraticB, 2) - (4.0 * quadraticA * quadraticC);

            if (discriminant < 0) return allPoints; // no intersection

            double sqrt = Math.sqrt(discriminant);

            double xRoot1 = (-quadraticB + sqrt) / (2.0 * quadraticA);
            double yRoot1 = m1 * (xRoot1 - x1) + y1;

            xRoot1 += circleCenter.x;
            yRoot1 += circleCenter.y;

            double minX = Math.min(linePointA.x, linePointB.x);
            double maxX = Math.max(linePointA.x, linePointB.x);

            if (xRoot1 > minX && xRoot1 < maxX) {
                allPoints.add(new Point(xRoot1, yRoot1));
            }

            double xRoot2 = (-quadraticB - sqrt) / (2.0 * quadraticA);
            double yRoot2 = m1 * (xRoot2 - x1) + y1;

            xRoot2 += circleCenter.x;
            yRoot2 += circleCenter.y;

            if (xRoot2 > minX && xRoot2 < maxX) {
                allPoints.add(new Point(xRoot2, yRoot2));
            }

        } catch (Exception e) {
            // silently ignore (could log if needed)
        }

        return allPoints;
    }
}