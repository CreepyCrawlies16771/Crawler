package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import org.firstinspires.ftc.teamcode.Crawler.RobotConfig;

public class Waypoint {

    public final double x;
    public final double y;
    public final double moveSpeed;
    public final double turnSpeed;
    public final double followDistance;
    public final double slowDownTurnRadians;
    public final double slowDownTurnAmount;
    public final Runnable onReach;

    // internal — mutable only by Field Follower
    double pointLength;

    private Waypoint(Builder builder) {
        this.x                  = builder.x;
        this.y                  = builder.y;
        this.moveSpeed          = builder.moveSpeed;
        this.turnSpeed          = builder.turnSpeed;
        this.followDistance     = builder.followDistance;
        this.slowDownTurnRadians = builder.slowDownTurnRadians;
        this.slowDownTurnAmount  = builder.slowDownTurnAmount;
        this.onReach            = builder.onReach;
    }

    public Waypoint(Waypoint other) {
        this.x                  = other.x;
        this.y                  = other.y;
        this.moveSpeed          = other.moveSpeed;
        this.turnSpeed          = other.turnSpeed;
        this.followDistance     = other.followDistance;
        this.slowDownTurnRadians = other.slowDownTurnRadians;
        this.slowDownTurnAmount  = other.slowDownTurnAmount;
        this.onReach            = other.onReach;
        this.pointLength        = other.pointLength;
    }

    public Vector2d toVector() {
        return new Vector2d(x, y);
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    /***
     * Factory Entry Point
     * @param x
     * @param y
     * @return
     */

    public static Builder at(double x, double y) {
        return new Builder(x, y);
    }

    /**
     *The Waypoint Builder
     */

    public static class Builder {
        private final double x;
        private final double y;
        private double moveSpeed          = RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED;
        private double turnSpeed          = RobotConfig.FieldOriented.DEFAULT_TURN_SPEED;
        private double followDistance     = RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE;
        private double slowDownTurnRadians = RobotConfig.FieldOriented.DEFAULT_SLOW_DOWN_TURN_RADIANS;
        private double slowDownTurnAmount  = RobotConfig.FieldOriented.DEFAULT_SLOW_DOWN_TURN_AMOUNT;
        private Runnable onReach          = null;

        public Builder(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Builder speed(double speed) {
            this.moveSpeed = speed;
            return this;
        }

        public Builder turnSpeed(double turnSpeed) {
            this.turnSpeed = turnSpeed;
            return this;
        }

        public Builder followDistance(double followDistance) {
            this.followDistance = followDistance;
            return this;
        }

        public Builder slowDown(double radians, double amount) {
            this.slowDownTurnRadians = radians;
            this.slowDownTurnAmount  = amount;
            return this;
        }

        /** Preset — slow speed, tight follow distance for sharp turns or end points */
        public Builder slow() {
            this.moveSpeed      = RobotConfig.FieldOriented.SLOW_MOVE_SPEED;
            this.turnSpeed      = RobotConfig.FieldOriented.SLOW_TURN_SPEED;
            this.followDistance = RobotConfig.FieldOriented.SLOW_FOLLOW_DISTANCE;
            return this;
        }

        /** Fires a Runnable when the robot reaches this waypoint */
        public Builder onReach(Runnable action) {
            this.onReach = action;
            return this;
        }

        public Waypoint build() {
            return new Waypoint(this);
        }
    }
}