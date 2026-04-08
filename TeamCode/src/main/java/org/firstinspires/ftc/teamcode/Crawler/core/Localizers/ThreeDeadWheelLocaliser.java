package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.HolonomicOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;

/**
 * Three-dead-wheel holonomic odometry localizer for precise positioning.
 *
 * <p>Uses three perpendicular dead-wheel encoders (left, right, and center)
 * to track full 2D position and heading. This is the most accurate localizer
 * for field-relative movement.</p>
 */
public class ThreeDeadWheelLocaliser implements CrawlerLocaliser {

    private final HolonomicOdometry odometry;

    /**
     * Creates a three-dead-wheel localizer.
     *
     * @param leftEncoder the left parallel encoder motor
     * @param rightEncoder the right parallel encoder motor
     * @param centerEncoder the center perpendicular encoder motor
     * @param invertLeft whether to invert the left encoder
     * @param invertRight whether to invert the right encoder
     * @param invertCenter whether to invert the center encoder
     */
    public ThreeDeadWheelLocaliser(MotorEx leftEncoder, MotorEx rightEncoder, MotorEx centerEncoder,
                                   boolean invertLeft, boolean invertRight, boolean invertCenter) {

        // Calculate distance per pulse from wheel diameter and encoder resolution
        double distancePerPulse = Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER
                / RobotConfig.Odometry.TICKS_PER_REV;

        // Apply distance-per-pulse to all encoders
        leftEncoder.setDistancePerPulse(distancePerPulse);
        rightEncoder.setDistancePerPulse(distancePerPulse);
        centerEncoder.setDistancePerPulse(distancePerPulse);

        // Apply encoder inversions if specified
        if (invertLeft)   leftEncoder.setInverted(true);
        if (invertRight)  rightEncoder.setInverted(true);
        if (invertCenter) centerEncoder.setInverted(true);

        // Create holonomic odometry instance with three encoders
        odometry = new HolonomicOdometry(
                leftEncoder::getDistance,
                rightEncoder::getDistance,
                centerEncoder::getDistance,
                RobotConfig.Odometry.TRACK_WIDTH,
                RobotConfig.Odometry.CENTER_WHEEL_OFFSET
        );
    }

    /**
     * Updates the odometry state from encoder readings.
     */
    @Override
    public void update() {
        odometry.updatePose();
    }

    /**
     * Gets the current pose estimate.
     *
     * @return the current Pose2d (x, y, heading)
     */
    @Override
    public Pose2d getPose() {
        return odometry.getPose();
    }

    /**
     * Resets the odometry to a specified pose.
     *
     * @param pose the pose to reset to, or null for (0, 0, 0)
     */
    @Override
    public void resetPose(Pose2d pose) {
        if (pose != null) {
            odometry.updatePose(pose);
        } else {
            odometry.updatePose(new Pose2d());
        }
    }
}