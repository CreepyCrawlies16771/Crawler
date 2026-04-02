package org.firstinspires.ftc.teamcode.Crawler.core.utils;

public class Point {
    public double x, y;
    public Point(double x, double y) { this.x = x; this.y = y; }

    public static Point fromCurvePoint(CurvePoint curvePoint) {
        return new Point(curvePoint.x, curvePoint.y);
    }
}
