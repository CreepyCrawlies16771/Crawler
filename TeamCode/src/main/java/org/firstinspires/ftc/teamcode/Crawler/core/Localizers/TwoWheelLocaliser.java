package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.arcrobotics.ftclib.kinematics.DifferentialOdometry;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

public class TwoWheelLocaliser implements CrawlerLocaliser {

    private final DifferentialOdometry odometry;

    public TwoWheelLocaliser(MotorEx left, MotorEx right,
                             CrawlerRobot.Config config) {

        // setDistancePerPulse expects DISTANCE PER TICK (cm per tick here), so the
        // reciprocal of ticksPerCm() is used; distances are in centimeters.
        double cmPerTick = 1.0 / config.ticksPerCm();
        left.setDistancePerPulse(cmPerTick);
        right.setDistancePerPulse(cmPerTick);

        // Encoder direction is owned by the MotorEx instances (builder flags applied in
        // CrawlerRobot) — never inverted here as well.
        odometry = new DifferentialOdometry(
                left::getDistance,
                right::getDistance,
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
