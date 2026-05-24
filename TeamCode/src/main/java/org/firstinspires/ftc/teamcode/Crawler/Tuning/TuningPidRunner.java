package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

final class TuningPidRunner {

    private static final double TARGET_DISTANCE_CM = 100.0;
    private static final double TARGET_TURN_DEG    = 90.0;

    private final CrawlerRobot robot;
    private final CrawlerRobot.Config config;
    private final IMU imu;
    private final Telemetry telemetry;
    private final TuningActiveCheck active;

    TuningPidRunner(CrawlerRobot robot, Telemetry telemetry, TuningActiveCheck active) {
        this.robot = robot;
        this.config = robot.config;
        this.imu = robot.imu;
        this.telemetry = telemetry;
        this.active = active;
    }

    void testDrive() throws InterruptedException {
        robot.update();
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            Pose2d pose = robot.getPose();
            double distance = Math.hypot(pose.getX() - start.getX(), pose.getY() - start.getY());
            double error = TARGET_DISTANCE_CM - distance;

            telemetry.addData("Distance (cm)", String.format("%.1f", distance));
            telemetry.addData("Error (cm)", String.format("%.1f", error));
            telemetry.addData("driveKp", config.driveKp);
            telemetry.update();

            if (distance >= TARGET_DISTANCE_CM) break;
            double power = clamp(config.driveKp * (error / TARGET_DISTANCE_CM));
            robot.drive(power, 0, headingHold(Math.toDegrees(start.getHeading())));
            Thread.yield();
        }
        robot.stop();
        sleepBrief();
    }

    void testStrafe() throws InterruptedException {
        robot.update();
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            Pose2d pose = robot.getPose();
            double distance = Math.abs(pose.getX() - start.getX());
            double error = TARGET_DISTANCE_CM - distance;

            telemetry.addData("Distance (cm)", String.format("%.1f", distance));
            telemetry.addData("strafeKp", config.strafeKp);
            telemetry.update();

            if (distance >= TARGET_DISTANCE_CM) break;
            double power = clamp(config.strafeKp * (error / TARGET_DISTANCE_CM));
            robot.drive(0, power, headingHold(Math.toDegrees(start.getHeading())));
            Thread.yield();
        }
        robot.stop();
        sleepBrief();
    }

    void testTurn() throws InterruptedException {
        double startYaw = TuningUtil.imuYawDeg(imu);
        double targetYaw = startYaw + TARGET_TURN_DEG;
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            double currentYaw = TuningUtil.imuYawDeg(imu);
            double error = TuningUtil.angleWrapDeg(targetYaw - currentYaw);

            telemetry.addData("Error (deg)", String.format("%.1f", error));
            telemetry.addData("steerP", config.steerP);
            telemetry.update();

            if (Math.abs(error) < 2.0) break;
            robot.drive(0, 0, clamp(error * config.steerP));
            Thread.yield();
        }
        robot.stop();
        sleepBrief();
    }

    private double headingHold(double targetHeadingDeg) {
        double err = TuningUtil.angleWrapDeg(targetHeadingDeg - TuningUtil.imuYawDeg(imu));
        return clamp(err * config.steerP);
    }

    private double clamp(double power) {
        power = Math.max(-0.7, Math.min(0.7, power));
        if (Math.abs(power) < config.minPower) {
            power = Math.signum(power == 0 ? 1 : power) * config.minPower;
        }
        return power;
    }

    private void sleepBrief() throws InterruptedException {
        ElapsedTime wait = new ElapsedTime();
        while (active.isActive() && wait.milliseconds() < 1500) Thread.yield();
    }
}
