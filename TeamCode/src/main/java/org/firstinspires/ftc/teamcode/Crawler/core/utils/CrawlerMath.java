package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import java.util.ArrayList;

/**
 * Utility math functions for Crawler robot control.
 *
 * <p>Provides angle wrapping, clamping, and path geometry functions for pure pursuit
 * and other control algorithms.</p>
 */
public class CrawlerMath {

    /**
     * Wraps an angle in degrees to the range [-180, 180].
     *
     * <p>Used for heading error calculations where we want the shortest rotation path.</p>
     *
     * @param degrees the angle in degrees
     * @return the angle wrapped to [-180, 180] degrees
     */
    public static double wrapAngle(double degrees) {
        while (degrees < -180) degrees += 360;
        while (degrees > 180) degrees -= 360;
        return degrees;
    }

    /**
     * Wraps an angle in radians to the range [-π, π].
     *
     * <p>This is the radian equivalent of {@link #wrapAngle(double)}. Used when
     * working with heading angles in radians (e.g., from localiser.getPose().getHeading()).</p>
     *
     * @param radians the angle in radians
     * @return the angle wrapped to [-π, π] radians
     */
    public static double wrapRadians(double radians) {
        while (radians < -Math.PI) radians += 2 * Math.PI;
        while (radians > Math.PI) radians -= 2 * Math.PI;
        return radians;
    }

    /**
     * Clamps a value to a range.
     *
     * @param value the value to clamp
     * @param min the minimum allowed value
     * @param max the maximum allowed value
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Finds the intersection points between a circle and a line segment.
     *
     * <p>Used by pure pursuit pathfinding to calculate lookahead points. Returns
     * all intersection points within the line segment bounds.</p>
     *
     * @param circleCenter the center of the circle
     * @param radius the radius of the circle
     * @param linePointA the first endpoint of the line segment
     * @param linePointB the second endpoint of the line segment
     * @return a list of intersection points (may be empty if no intersection exists)
     */
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