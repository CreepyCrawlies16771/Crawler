package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import java.util.ArrayList;

/**
 * Crawler math utilities for geometry and angle calculations.
 *
 * <p>All angles are in radians internally. This class provides conversion utilities
 * and pure pursuit path computations.</p>
 */
public class CrawlerMath {

    /**
     * Constrains an angle (in radians) to the range [-π, π].
     *
     * <p>This ensures angles wrap correctly through 0, preventing jumps
     * from π to -π during heading calculations.</p>
     *
     * @param angle the angle in radians
     * @return the angle wrapped to [-π, π]
     */
    public static double wrapAngle(double angle) {
        while (angle < -Math.PI) angle += 2 * Math.PI;
        while (angle > Math.PI) angle -= 2 * Math.PI;
        return angle;
    }

    /**
     * Clamps a value to a specified range.
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
     * Finds the intersection points of a circle and a line segment.
     *
     * <p>This is the core algorithm for pure pursuit lookahead point selection.
     * Given a circle centered at {@code circleCenter} and a line segment from
     * {@code linePointA} to {@code linePointB}, this method returns the points
     * where they intersect.</p>
     *
     * <p>The method uses local copies of input coordinates to avoid mutating
     * the caller's data. If the line is nearly degenerate (horizontal or vertical),
     * it is slightly perturbed to ensure valid math.</p>
     *
     * @param circleCenter the center of the circle
     * @param radius the radius of the circle
     * @param linePointA the start point of the line segment
     * @param linePointB the end point of the line segment
     * @return a list of intersection points (0, 1, or 2 points)
     */
    public static ArrayList<Point> lineCircleIntersection(
            Point circleCenter,
            double radius,
            Point linePointA,
            Point linePointB
    ) {

        // Use local copies to avoid mutating caller's data
        double ax = linePointA.x;
        double ay = linePointA.y;
        double bx = linePointB.x;
        double by = linePointB.y;

        // Handle degenerate line segments (horizontal or vertical)
        if (Math.abs(ay - by) < 0.003) {
            ay = by + 0.003;  // Perturb slightly to avoid singular slope
        }

        if (Math.abs(ax - bx) < 0.003) {
            ax = bx + 0.003;  // Perturb slightly to avoid singular slope
        }

        double m1 = (by - ay) / (bx - ax);

        double quadraticA = 1.0 + Math.pow(m1, 2);

        double x1 = ax - circleCenter.x;
        double y1 = ay - circleCenter.y;

        double quadraticB = (2.0 * m1 * y1) - (2.0 * Math.pow(m1, 2) * x1);

        double quadraticC = (Math.pow(m1, 2) * Math.pow(x1, 2))
                - (2.0 * y1 * m1 * x1)
                - Math.pow(radius, 2);

        ArrayList<Point> allPoints = new ArrayList<>();

        try {
            double discriminant = Math.pow(quadraticB, 2) - (4.0 * quadraticA * quadraticC);

            // No real roots — no intersection
            if (discriminant < 0) return allPoints;

            double sqrt = Math.sqrt(discriminant);

            // First root
            double xRoot1 = (-quadraticB + sqrt) / (2.0 * quadraticA);
            double yRoot1 = m1 * (xRoot1 - x1) + y1;

            xRoot1 += circleCenter.x;
            yRoot1 += circleCenter.y;

            double minX = Math.min(ax, bx);
            double maxX = Math.max(ax, bx);

            // Verify first root is not NaN and within segment bounds
            if (!Double.isNaN(xRoot1) && !Double.isNaN(yRoot1) && xRoot1 > minX && xRoot1 < maxX) {
                allPoints.add(new Point(xRoot1, yRoot1));
            }

            // Second root
            double xRoot2 = (-quadraticB - sqrt) / (2.0 * quadraticA);
            double yRoot2 = m1 * (xRoot2 - x1) + y1;

            xRoot2 += circleCenter.x;
            yRoot2 += circleCenter.y;

            // Verify second root is not NaN and within segment bounds
            if (!Double.isNaN(xRoot2) && !Double.isNaN(yRoot2) && xRoot2 > minX && xRoot2 < maxX) {
                allPoints.add(new Point(xRoot2, yRoot2));
            }

        } catch (Exception e) {
            // Return empty list on math failure — caller handles gracefully
            // Do not swallow silently: NaN checks above prevent invalid points
        }

        return allPoints;
    }
}