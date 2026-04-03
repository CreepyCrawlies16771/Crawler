package org.firstinspires.ftc.teamcode.Crawler.Dashboard;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

public class DashboardFieldViewUtils {

    public static final double ROBOT_RADIUS = 9.0;

    /**
     * Standard web colors supported by FTC Dashboard.
     * Add more hex codes or names here as needed.
     */
    public enum FieldColor {
        RED("red"),
        BLUE("blue"),
        GREEN("green"),
        YELLOW("yellow"),
        ORANGE("orange"),
        PURPLE("purple"),
        CYAN("cyan"),
        MAGENTA("magenta"),
        BLACK("black"),
        WHITE("white");

        private final String code;

        FieldColor(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * Draws a simple line between two points on the field.
     */
    public static void drawLine(TelemetryPacket packet, double startX, double startY, double endX, double endY, FieldColor color) {
        Canvas canvas = packet.fieldOverlay();
        canvas.setStroke(color.getCode());
        canvas.setStrokeWidth(1);
        canvas.strokeLine(startX, startY, endX, endY);
    }

    /**
     * Draws a target point on the field (useful for waypoint debugging).
     */
    public static void drawPoint(TelemetryPacket packet, double x, double y, FieldColor color) {
        Canvas canvas = packet.fieldOverlay();
        canvas.setFill(color.getCode());
        canvas.fillCircle(x, y, 2.0); // 2-inch radius filled circle
    }

    /**
     * Draws the robot as a rotating square with a heading indicator line.
     * Takes standard X, Y, and Heading (in radians) from odometry outputs.
     */
    public static void drawRobot(TelemetryPacket packet, double x, double y, double headingRads, FieldColor color) {
        Canvas canvas = packet.fieldOverlay();
        canvas.setStroke(color.getCode());
        canvas.setStrokeWidth(1);

        double[] xPoints = new double[4];
        double[] yPoints = new double[4];

        // Corners of an 18x18 inch square relative to the center
        double[] xOffsets = {ROBOT_RADIUS, -ROBOT_RADIUS, -ROBOT_RADIUS, ROBOT_RADIUS};
        double[] yOffsets = {ROBOT_RADIUS, ROBOT_RADIUS, -ROBOT_RADIUS, -ROBOT_RADIUS};

        // Rotate the square to match the robot's current heading
        for (int i = 0; i < 4; i++) {
            double rotX = xOffsets[i] * Math.cos(headingRads) - yOffsets[i] * Math.sin(headingRads);
            double rotY = xOffsets[i] * Math.sin(headingRads) + yOffsets[i] * Math.cos(headingRads);
            xPoints[i] = x + rotX;
            yPoints[i] = y + rotY;
        }

        // Draw the chassis outline
        canvas.strokePolygon(xPoints, yPoints);

        // Draw the heading indicator (a line pointing forward from the center)
        double xDir = x + ROBOT_RADIUS * Math.cos(headingRads);
        double yDir = y + ROBOT_RADIUS * Math.sin(headingRads);
        canvas.strokeLine(x, y, xDir, yDir);
    }
}