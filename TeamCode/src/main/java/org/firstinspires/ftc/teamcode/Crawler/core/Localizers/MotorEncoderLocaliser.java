package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

public class MotorEncoderLocaliser implements CrawlerLocaliser {

    private final DifferentialOdometry odometry;

    public MotorEncoderLocaliser(MotorEx frontLeft, MotorEx frontRight,
                                 MotorEx backLeft, MotorEx backRight,
                                 CrawlerRobot.Config config) {

        // setDistancePerPulse expects DISTANCE PER TICK (cm per tick here), so the
        // reciprocal of ticksPerCm() is used; distances are in centimeters.
        double cmPerTick = 1.0 / config.ticksPerCm();
        frontLeft.setDistancePerPulse(cmPerTick);
        frontRight.setDistancePerPulse(cmPerTick);
        backLeft.setDistancePerPulse(cmPerTick);
        backRight.setDistancePerPulse(cmPerTick);

        // Drive-motor direction is owned by the MotorEx instances (builder inversion
        // flags applied in CrawlerRobot) — never inverted here as well.
        odometry = new DifferentialOdometry(
                () -> (frontLeft.getDistance() + backLeft.getDistance()) / 2.0,
                () -> (frontRight.getDistance() + backRight.getDistance()) / 2.0,
                UnitConverter.inToCm(config.trackWidthIn)
        );
    }

    @Override
    public void update() { odometry.updatePose(); }

    @Override
    public Pose2d getPose() { return odometry.getPose(); }

    @Override
    public void resetPose(Pose2d pose2d) { odometry.updatePose(pose2d); }
}
