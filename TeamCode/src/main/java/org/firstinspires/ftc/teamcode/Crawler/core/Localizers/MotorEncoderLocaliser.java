package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;

public class MotorEncoderLocaliser implements CrawlerLocaliser {

    private final DifferentialOdometry odometry;

    public MotorEncoderLocaliser(MotorEx frontLeft, MotorEx frontRight,
                                 MotorEx backLeft,  MotorEx backRight) {

        frontLeft.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
        frontRight.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
        backLeft.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
        backRight.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);

        odometry = new DifferentialOdometry(
                () -> (frontLeft.getDistance() + backLeft.getDistance()) / 2.0,
                () -> (frontRight.getDistance() + backRight.getDistance()) / 2.0,
                RobotConfig.Odometry.TRACK_WIDTH
        );
    }

    @Override
    public void update() {
        odometry.updatePose();
    }

    @Override
    public Pose2d getPose() {
        return odometry.getPose();
    }

    @Override
    public void resetPose(Pose2d pose2d) {
        odometry.updatePose(pose2d);
    }

}