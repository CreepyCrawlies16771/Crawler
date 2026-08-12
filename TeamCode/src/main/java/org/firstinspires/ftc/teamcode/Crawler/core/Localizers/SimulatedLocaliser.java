package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

/**
 * Simulated mecanum odometry for testing path code on the PC — no hardware required.
 *
 * <p>Integrates the four drive-motor positions into a field pose using standard mecanum
 * kinematics, so a path followed in a JVM test behaves like it will on the real robot.
 * Choose it with {@code .withSimulatedLocaliser()} and drive it with simulated motors
 * (see the {@code Crawler.sim} test package for fakes). Not for use on a real robot —
 * swap to a real localizer before testing on the floor.</p>
 */
public class SimulatedLocaliser implements CrawlerLocaliser {

    private final MotorEx frontLeft;
    private final MotorEx frontRight;
    private final MotorEx backLeft;
    private final MotorEx backRight;
    private final double cmPerTick;
    private final double halfTrackCm;

    private Pose2d pose = new Pose2d();
    private double lastFl;
    private double lastFr;
    private double lastBl;
    private double lastBr;
    private boolean haveLast;

    public SimulatedLocaliser(MotorEx frontLeft, MotorEx frontRight,
                              MotorEx backLeft, MotorEx backRight,
                              CrawlerRobot.Config config) {
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;
        this.cmPerTick = 1.0 / config.ticksPerCm();
        this.halfTrackCm = UnitConverter.inToCm(config.trackWidth) / 2.0;
    }

    @Override
    public void update() {
        double fl = frontLeft.getCurrentPosition() * cmPerTick;
        double fr = frontRight.getCurrentPosition() * cmPerTick;
        double bl = backLeft.getCurrentPosition() * cmPerTick;
        double br = backRight.getCurrentPosition() * cmPerTick;

        if (!haveLast) {
            lastFl = fl;
            lastFr = fr;
            lastBl = bl;
            lastBr = br;
            haveLast = true;
        }

        // Mecanum inverse kinematics (robot frame, cm traveled since the last update).
        // Signs match CrawlerRobot.drive(): fl = f+s+r, fr = f-s-r, bl = f-s+r, br = f+s-r.
        double dFl = fl - lastFl;
        double dFr = fr - lastFr;
        double dBl = bl - lastBl;
        double dBr = br - lastBr;

        double fwd = (dFl + dFr + dBl + dBr) / 4.0;
        double str = (dFl - dFr - dBl + dBr) / 4.0;
        double rot = (dFl - dFr + dBl - dBr) / 4.0;

        lastFl = fl;
        lastFr = fr;
        lastBl = bl;
        lastBr = br;

        // Rotate into the field frame, then advance heading by the rotation distance.
        double heading = pose.getHeading();
        double dx = fwd * Math.cos(heading) - str * Math.sin(heading);
        double dy = fwd * Math.sin(heading) + str * Math.cos(heading);
        double dHeading = rot / halfTrackCm;

        pose = new Pose2d(pose.getX() + dx, pose.getY() + dy,
                new Rotation2d(heading + dHeading));
    }

    @Override
    public Pose2d getPose() {
        return pose;
    }

    @Override
    public void resetPose(Pose2d pose) {
        this.pose = pose;
        haveLast = false;   // next update() becomes the new baseline (no position jump)
    }
}
