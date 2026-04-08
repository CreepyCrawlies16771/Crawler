package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;

/**
 * Two-wheel differential odometry localizer using front and back motor encoders.
 *
 * <p>This localizer assumes two wheels (left and right) with encoders,
 * and averages the left-side and right-side encoder distances for odometry.</p>
 */
public class MotorEncoderLocaliser implements CrawlerLocaliser {

    private final DifferentialOdometry odometry;

    /**
     * Creates a motor encoder localizer from four drive motors.
     *
     * <p>The left-side encoders (frontLeft and backLeft) are averaged for the
     * left wheel distance, and the right-side encoders (frontRight and backRight)
     * are averaged for the right wheel distance.</p>
     *
     * @param frontLeft the front-left drive motor
     * @param frontRight the front-right drive motor
     * @param backLeft the back-left drive motor
     * @param backRight the back-right drive motor
     */
    public MotorEncoderLocaliser(MotorEx frontLeft, MotorEx frontRight,
                                 MotorEx backLeft,  MotorEx backRight) {

        // Calculate distance per pulse from wheel diameter and encoder resolution
        double distancePerPulse = Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER
                / RobotConfig.Odometry.TICKS_PER_REV;

        // Apply distance-per-pulse to all encoders
        frontLeft.setDistancePerPulse(distancePerPulse);
        frontRight.setDistancePerPulse(distancePerPulse);
        backLeft.setDistancePerPulse(distancePerPulse);
        backRight.setDistancePerPulse(distancePerPulse);

        // Create differential odometry instance
        // Left encoder is average of front-left and back-left
        // Right encoder is average of front-right and back-right
        odometry = new DifferentialOdometry(
                () -> (frontLeft.getDistance() + backLeft.getDistance()) / 2.0,
                () -> (frontRight.getDistance() + backRight.getDistance()) / 2.0,
                RobotConfig.Odometry.TRACK_WIDTH
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