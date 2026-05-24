package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

public class TwoWheelLocaliser implements CrawlerLocaliser {

    private final DifferentialOdometry odometry;

    public TwoWheelLocaliser(MotorEx left, MotorEx right,
                             boolean invertLeft, boolean invertRight,
                             CrawlerRobot.Config config) {

        left.setDistancePerPulse(config.ticksPerCm());
        right.setDistancePerPulse(config.ticksPerCm());

        if (invertLeft)  left.setInverted(true);
        if (invertRight) right.setInverted(true);

        odometry = new DifferentialOdometry(
                left::getDistance,
                right::getDistance,
                config.trackWidthIn
        );
    }

    @Override
    public void update() { odometry.updatePose(); }

    @Override
    public Pose2d getPose() { return odometry.getPose(); }

    @Override
    public void resetPose(Pose2d pose2d) { odometry.updatePose(pose2d); }
}
