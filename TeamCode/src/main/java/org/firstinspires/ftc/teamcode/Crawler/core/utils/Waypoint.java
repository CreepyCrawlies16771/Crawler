package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Represents a waypoint in a path for field-oriented movement.
 */
public class Waypoint {

    public final double x;
    public final double y;
    public final double heading;
    public final double moveSpeed;
    public final double turnSpeed;
    public final double followDistance;
    public final double slowDownTurnRadians;
    public final double slowDownTurnAmount;
    public final Runnable onReach;

    double pointLength;

    private Waypoint(Builder builder) {
        this.x                   = builder.x;
        this.y                   = builder.y;
        this.heading             = builder.heading;
        this.moveSpeed           = builder.moveSpeed;
        this.turnSpeed           = builder.turnSpeed;
        this.followDistance      = builder.followDistance;
        this.slowDownTurnRadians = builder.slowDownTurnRadians;
        this.slowDownTurnAmount  = builder.slowDownTurnAmount;
        this.onReach             = builder.onReach;
    }

    public Waypoint(Waypoint other) {
        this.x                   = other.x;
        this.y                   = other.y;
        this.heading             = other.heading;
        this.moveSpeed           = other.moveSpeed;
        this.turnSpeed           = other.turnSpeed;
        this.followDistance      = other.followDistance;
        this.slowDownTurnRadians = other.slowDownTurnRadians;
        this.slowDownTurnAmount  = other.slowDownTurnAmount;
        this.onReach             = other.onReach;
        this.pointLength         = other.pointLength;
    }

    public Vector2d toVector() {
        return new Vector2d(x, y);
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    /**
     * Prefer {@link #at(double, double, CrawlerRobot.Config)} with {@code robot.config}.
     */
    public static Builder at(double x, double y) {
        return at(x, y, new CrawlerRobot.Config());
    }

    /** Creates a waypoint using your robot's tuned path defaults. */
    public static Builder at(double x, double y, CrawlerRobot.Config config) {
        if (config == null) {
            throw new IllegalArgumentException("config must be your robot.config, not null");
        }
        return new Builder(x, y, config);
    }

    public static class Builder {
        private final double x;
        private final double y;
        private double heading;
        private double moveSpeed;
        private double turnSpeed;
        private double followDistance;
        private double slowDownTurnRadians;
        private double slowDownTurnAmount;
        private Runnable onReach;

        public Builder(double x, double y, CrawlerRobot.Config config) {
            this.x = x;
            this.y = y;
            this.heading             = 0.0;
            this.moveSpeed           = config.defaultMoveSpeed;
            this.turnSpeed           = config.defaultTurnSpeed;
            this.followDistance      = config.followDistanceCm;
            this.slowDownTurnRadians = config.slowDownTurnRadians;
            this.slowDownTurnAmount  = config.slowDownTurnAmount;
        }

        public Builder heading(double heading) {
            this.heading = heading;
            return this;
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

        public Builder slow(CrawlerRobot.Config config) {
            this.moveSpeed      = config.slowMoveSpeed;
            this.turnSpeed      = config.slowTurnSpeed;
            this.followDistance = config.slowFollowDistanceCm;
            return this;
        }

        public Builder onReach(Runnable action) {
            this.onReach = action;
            return this;
        }

        public Waypoint build() {
            return new Waypoint(this);
        }
    }
}
