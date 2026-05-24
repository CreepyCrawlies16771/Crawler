package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.HolonomicOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

public class ThreeDeadWheelLocaliser implements CrawlerLocaliser {

    private final HolonomicOdometry odometry;

    public ThreeDeadWheelLocaliser(MotorEx leftEncoder, MotorEx rightEncoder, MotorEx centerEncoder,
                                   boolean invertLeft, boolean invertRight, boolean invertCenter,
                                   CrawlerRobot.Config config) {

        double distancePerPulse = Math.PI * config.wheelDiameterIn / config.ticksPerRev;

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
                config.trackWidthIn,
                config.centerWheelOffsetIn
        );
    }

    @Override
    public void update() { odometry.updatePose(); }

    @Override
    public Pose2d getPose() { return odometry.getPose(); }

    @Override
    public void resetPose(Pose2d pose2d) { odometry.updatePose(pose2d); }
}
