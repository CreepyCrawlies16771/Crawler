package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.HolonomicOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

public class ThreeDeadWheelLocaliser implements CrawlerLocaliser {

    private final HolonomicOdometry odometry;

    public ThreeDeadWheelLocaliser(MotorEx leftEncoder, MotorEx rightEncoder, MotorEx centerEncoder,
                                   CrawlerRobot.Config config) {

        // setDistancePerPulse expects DISTANCE PER TICK, so take the reciprocal of
        // ticksPerCm(). Units are centimeters — the framework's field convention — so
        // the odometry pose comes out in cm like every other localizer.
        double cmPerTick = 1.0 / config.ticksPerCm();

        leftEncoder.setDistancePerPulse(cmPerTick);
        rightEncoder.setDistancePerPulse(cmPerTick);
        centerEncoder.setDistancePerPulse(cmPerTick);

        // Encoder direction is owned by the MotorEx instances: CrawlerRobot applies the
        // builder's invertLeftEncoder()/... flags once, before the localizer is built.
        // Inverting here as well would cancel it out.
        odometry = new HolonomicOdometry(
                leftEncoder::getDistance,
                rightEncoder::getDistance,
                centerEncoder::getDistance,
                UnitConverter.inToCm(config.trackWidthIn),
                UnitConverter.inToCm(config.centerWheelOffsetIn)
        );
    }

    @Override
    public void update() { odometry.updatePose(); }

    @Override
    public Pose2d getPose() { return odometry.getPose(); }

    @Override
    public void resetPose(Pose2d pose2d) { odometry.updatePose(pose2d); }
}
