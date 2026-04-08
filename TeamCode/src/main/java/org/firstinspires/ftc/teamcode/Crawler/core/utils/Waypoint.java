package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a waypoint in a path for field-oriented robot movement.
 *
 * <p>Waypoints are immutable once created and contain position, speed, and
 * behavior parameters. They use a fluent builder pattern for construction.</p>
 */
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

    /**
     * Copy constructor for creating a waypoint from another waypoint.
     *
     * @param other the waypoint to copy
     */
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

    /**
     * Returns this waypoint as a Vector2d.
     *
     * @return a Vector2d containing the x and y position
     */
    public Vector2d toVector() {
        return new Vector2d(x, y);
    }

    /**
     * Returns this waypoint as a Point.
     *
     * @return a Point containing the x and y position
     */
    public Point toPoint() {
        return new Point(x, y);
    }

    /**
     * Factory entry point for creating a new Waypoint builder.
     *
     * @param x the x coordinate (in centimeters)
     * @param y the y coordinate (in centimeters)
     * @return a new Builder at the specified position
     */
    public static Builder at(double x, double y) {
        return new Builder(x, y);
    }

    /**
     * Fluent builder for constructing waypoints with fluent chaining.
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * List<Waypoint> path = Waypoint.at(0, 0)
     *     .at(24, 0).speed(0.8).onReach(() -> robot.openClaw())
     *     .at(24, 24).slow()
     *     .buildAll();
     * }</pre>
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

        // Instance field to accumulate waypoints — not static, prevents cross-OpMode pollution
        private static final ThreadLocal<List<Waypoint>> pathBuilder = new ThreadLocal<List<Waypoint>>() {
            @Override
            protected List<Waypoint> initialValue() {
                return new ArrayList<>();
            }
        };

        /**
         * Creates a new builder at the specified position.
         *
         * @param x the x coordinate in centimeters
         * @param y the y coordinate in centimeters
         */
        public Builder(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Sets the movement speed for this waypoint.
         *
         * @param speed the speed (0.0 to 1.0 normally)
         * @return this builder for chaining
         */
        public Builder speed(double speed) {
            this.moveSpeed = speed;
            return this;
        }

        /**
         * Sets the turning speed for this waypoint.
         *
         * @param turnSpeed the turn speed (0.0 to 1.0 normally)
         * @return this builder for chaining
         */
        public Builder turnSpeed(double turnSpeed) {
            this.turnSpeed = turnSpeed;
            return this;
        }

        /**
         * Sets the look-ahead distance for pure pursuit at this waypoint.
         *
         * @param followDistance the distance in centimeters
         * @return this builder for chaining
         */
        public Builder followDistance(double followDistance) {
            this.followDistance = followDistance;
            return this;
        }

        /**
         * Applies a custom slow-down function near sharp turns.
         *
         * @param radians the heading error threshold in radians
         * @param amount the fraction to reduce speed (0.0 to 1.0)
         * @return this builder for chaining
         */
        public Builder slowDown(double radians, double amount) {
            this.slowDownTurnRadians = radians;
            this.slowDownTurnAmount  = amount;
            return this;
        }

        /**
         * Preset: reduces speed and tightens the look-ahead for precise positioning.
         * Useful at path endpoints or sharp turns.
         *
         * @return this builder for chaining
         */
        public Builder slow() {
            this.moveSpeed      = RobotConfig.FieldOriented.SLOW_MOVE_SPEED;
            this.turnSpeed      = RobotConfig.FieldOriented.SLOW_TURN_SPEED;
            this.followDistance = RobotConfig.FieldOriented.SLOW_FOLLOW_DISTANCE;
            return this;
        }

        /**
         * Registers a callback to fire when the robot reaches this waypoint.
         *
         * @param action the runnable to execute
         * @return this builder for chaining
         */
        public Builder onReach(Runnable action) {
            this.onReach = action;
            return this;
        }

        /**
         * Chains to a new waypoint without finishing the path.
         *
         * <p>This builds the current waypoint and begins building the next,
         * preserving the path chain for {@code buildAll()}.</p>
         *
         * @param x the x coordinate of the next waypoint
         * @param y the y coordinate of the next waypoint
         * @return a new builder for the next waypoint
         */
        public Builder at(double x, double y) {
            // Add current waypoint to the accumulated list
            pathBuilder.get().add(new Waypoint(this));
            // Return a new builder for the next waypoint
            return new Builder(x, y);
        }

        /**
         * Builds a single waypoint without chaining.
         *
         * <p>Use this when you have only one waypoint or when you want to
         * build intermediate waypoints without accumulating them.</p>
         *
         * @return the completed Waypoint
         */
        public Waypoint build() {
            return new Waypoint(this);
        }

        /**
         * Builds the complete path as a list of waypoints.
         *
         * <p>This finalizes the path chain and returns all waypoints from
         * {@code at()} calls plus the current waypoint. Resets the internal
         * list for the next path.</p>
         *
         * @return a list of all waypoints in the path
         */
        public List<Waypoint> buildAll() {
            // Add the final waypoint
            pathBuilder.get().add(new Waypoint(this));
            // Copy the list and reset for next path
            List<Waypoint> result = new ArrayList<>(pathBuilder.get());
            pathBuilder.get().clear();
            return result;
        }
    }
}