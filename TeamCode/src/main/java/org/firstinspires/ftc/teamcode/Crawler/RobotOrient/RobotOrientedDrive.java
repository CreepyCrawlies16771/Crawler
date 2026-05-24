package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;

/**
 * Robot-relative PID moves using a {@link CrawlerRobot} and its {@code config}.
 * Use for small manual adjustments, alignment, and short autonomous segments.
 */
public class RobotOrientedDrive {

    public interface ActiveCheck {
        boolean isActive();
    }

    private final CrawlerRobot robot;
    private final CrawlerRobot.Config config;
    private final IMU imu;
    private final ActiveCheck active;
    private final Telemetry telemetry;

    public RobotOrientedDrive(CrawlerRobot robot, ActiveCheck active, Telemetry telemetry) {
        this.robot = robot;
        this.config = robot.config;
        this.imu = robot.imu;
        this.active = active;
        this.telemetry = telemetry;
    }

    /** Drive forward/backward in meters while holding heading (degrees). */
    public void drivePID(double targetMeters, int targetHeadingDeg) {
        double targetCm = Math.abs(targetMeters) * 100.0;
        double sign = Math.signum(targetMeters == 0 ? 1 : targetMeters);

        robot.update();
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            Pose2d pose = robot.getPose();
            double traveled = Math.hypot(pose.getX() - start.getX(), pose.getY() - start.getY());
            double errorCm = targetCm - traveled;

            if (traveled >= targetCm) break;

            double power = pidPower(errorCm / 100.0, config.driveKp, config.driveKi, config.driveKd, 0);
            power *= sign;
            double steer = headingHoldPower(targetHeadingDeg);
            robot.drive(power, 0, steer);
            telemetry.addData("drive err cm", errorCm);
            telemetry.update();
        }
        robot.stop();
    }

    /** Strafe left/right in meters (positive = right) while holding heading. */
    public void strafePID(double targetMeters, int targetHeadingDeg) {
        double targetCm = Math.abs(targetMeters) * 100.0;
        double sign = Math.signum(targetMeters == 0 ? 1 : targetMeters);

        robot.update();
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            double traveled = Math.abs(robot.getPose().getX() - start.getX());
            double errorCm = targetCm - traveled;

            if (traveled >= targetCm) break;

            double power = pidPower(errorCm / 100.0, config.strafeKp, config.strafeKi, config.strafeKd, 0);
            power *= sign;
            robot.drive(0, power, headingHoldPower(targetHeadingDeg));
            telemetry.addData("strafe err cm", errorCm);
            telemetry.update();
        }
        robot.stop();
    }

    /** Turn to absolute heading in degrees (field-relative gyro). */
    public void turnPID(int targetHeadingDeg) {
        ElapsedTime timer = new ElapsedTime();

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            double error = angleWrapDeg(targetHeadingDeg - imuYawDeg());
            if (Math.abs(error) < 1.0) break;

            double turn = clampPower(error * config.steerP);
            robot.drive(0, 0, turn);
            telemetry.addData("turn err deg", error);
            telemetry.update();
        }
        robot.stop();
    }

    private double headingHoldPower(int targetHeadingDeg) {
        double error = angleWrapDeg(targetHeadingDeg - imuYawDeg());
        return clampPower(error * config.steerP);
    }

    private double pidPower(double errorMeters, double kp, double ki, double kd, double integral) {
        return clampPower(kp * errorMeters + ki * integral + kd * 0);
    }

    private double clampPower(double power) {
        power = CrawlerMath.clamp(power, -0.7, 0.7);
        if (Math.abs(power) < config.minPower && Math.abs(power) > 1e-6) {
            power = Math.signum(power) * config.minPower;
        }
        return power;
    }

    private double imuYawDeg() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    private static double angleWrapDeg(double deg) {
        while (deg > 180) deg -= 360;
        while (deg < -180) deg += 360;
        return deg;
    }
}
