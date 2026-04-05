package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.HolonomicOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;

public abstract class ThreeDeadWheelLocaliser implements CrawlerLocaliser {

    private final HolonomicOdometry odometry;

    public ThreeDeadWheelLocaliser(MotorEx leftEncoder, MotorEx rightEncoder, MotorEx centerEncoder,
                                   boolean invertLeft, boolean invertRight, boolean invertCenter) {

        double distancePerPulse = Math.PI * RobotConfig.Odometry.WHEEL_DIAMETER
                / RobotConfig.Odometry.TICKS_PER_REV;

        leftEncoder.setDistancePerPulse(distancePerPulse);
        rightEncoder.setDistancePerPulse(distancePerPulse);
        centerEncoder.setDistancePerPulse(distancePerPulse);

        if (invertLeft)   leftEncoder.setInverted(true);
        if (invertRight)  rightEncoder.setInverted(true);
        if (invertCenter) centerEncoder.setInverted(true);

        odometry = new HolonomicOdometry(
                leftEncoder::getDistance,
                rightEncoder::getDistance,
                centerEncoder::getDistance,
                RobotConfig.Odometry.TRACK_WIDTH,
                RobotConfig.Odometry.CENTER_WHEEL_OFFSET
        );
    }

    @Override
    public void update() { odometry.updatePose(); }

    @Override
    public Pose2d getPose() { return odometry.getPose(); }

    @Override
    public void resetPose(Pose2d pose2d) { odometry.updatePose(pose2d); }
}