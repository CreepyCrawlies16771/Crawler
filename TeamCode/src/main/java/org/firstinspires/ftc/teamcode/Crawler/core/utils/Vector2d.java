package org.firstinspires.ftc.teamcode.Crawler.core.utils;

public class Vector2d {

    public final double x;
    public final double y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Vector2d other) {
        return Math.hypot(other.x - this.x, other.y - this.y);
    }

    public double magnitude() {
        return Math.hypot(x, y);
    }

    public Vector2d normalized() {
        double mag = magnitude();
        if (mag < 1e-6) return new Vector2d(0, 0);
        return new Vector2d(x / mag, y / mag);
    }

    public Vector2d plus(Vector2d other) {
        return new Vector2d(this.x + other.x, this.y + other.y);
    }

    public Vector2d minus(Vector2d other) {
        return new Vector2d(this.x - other.x, this.y - other.y);
    }

    public Vector2d times(double scalar) {
        return new Vector2d(this.x * scalar, this.y * scalar);
    }

    public double dot(Vector2d other) {
        return this.x * other.x + this.y * other.y;
    }

    public double angleTo(Vector2d other) {
        return Math.atan2(other.y - this.y, other.x - this.x);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}