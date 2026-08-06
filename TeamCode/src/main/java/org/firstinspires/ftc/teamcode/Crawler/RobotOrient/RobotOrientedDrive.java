package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerPreflight;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;

/**
 * Robot-relative PID moves using a {@link CrawlerRobot} and its {@code config}.
 * Use for small manual adjustments, alignment, and short autonomous segments.
 */
public class RobotOrientedDrive {

    public interface ActiveCheck {
        boolean isActive();
    }

    /**
     * Optional per-loop observer (used by the Crawler Tuner).
     * Receives the current error, commanded power and the individual P/I/D terms
     * on every control cycle so tuning can report exactly what the engine is doing.
     */
    public interface DebugSink {
        void onLoop(double error, double power, double p, double i, double d);
    }

    private final CrawlerRobot robot;
    private final CrawlerRobot.Config config;
    private final IMU imu;
    private final ActiveCheck active;
    private final Telemetry telemetry;
    private DebugSink debugSink;

    public RobotOrientedDrive(CrawlerRobot robot, ActiveCheck active, Telemetry telemetry) {
        this.robot = robot;
        this.config = robot.config;
        this.imu = robot.imu;
        this.active = active;
        this.telemetry = telemetry;

        // Early error detection: invalid tuned values (CRWL-107) and a dead IMU
        // (CRWL-201) surface here, at INIT, instead of mid-match.
        CrawlerPreflight.checkEngine(robot, telemetry);
    }

    /** Attach a per-loop observer (optional). */
    public void setDebugSink(DebugSink debugSink) {
        this.debugSink = debugSink;
    }

    private static final double INTEGRAL_LIMIT = 0.3;

    /** Drive forward/backward in meters while holding heading (degrees). */
    public void drivePID(double targetMeters, int targetHeadingDeg) {
        double targetCm = Math.abs(targetMeters) * 100.0;
        double sign = Math.signum(targetMeters == 0 ? 1 : targetMeters);

        robot.update();
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();
        Pid pid = new Pid(config.driveKp, config.driveKi, config.driveKd);

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            Pose2d pose = robot.getPose();
            double traveled = Math.hypot(pose.getX() - start.getX(), pose.getY() - start.getY());
            double errorCm = targetCm - traveled;

            if (traveled >= targetCm) break;

            // Deadband only overcomes static friction at start; raw PID once moving.
            double power = clampPower(pid.update(errorCm / 100.0, timer.seconds()),
                    traveled < 3.0) * sign;
            double steer = headingHoldPower(targetHeadingDeg);
            robot.drive(power, 0, steer);
            if (debugSink != null) {
                debugSink.onLoop(errorCm, power, pid.lastP, pid.lastI, pid.lastD);
            }
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
        Pid pid = new Pid(config.strafeKp, config.strafeKi, config.strafeKd);

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            robot.update();
            double traveled = Math.abs(robot.getPose().getX() - start.getX());
            double errorCm = targetCm - traveled;

            if (traveled >= targetCm) break;

            double power = clampPower(pid.update(errorCm / 100.0, timer.seconds()),
                    traveled < 3.0) * sign;
            robot.drive(0, power, headingHoldPower(targetHeadingDeg));
            if (debugSink != null) {
                debugSink.onLoop(errorCm, power, pid.lastP, pid.lastI, pid.lastD);
            }
            telemetry.addData("strafe err cm", errorCm);
            telemetry.update();
        }
        robot.stop();
    }

    /** Turn to absolute heading in degrees (field-relative gyro). */
    public void turnPID(int targetHeadingDeg) {
        ElapsedTime timer = new ElapsedTime();
        Pid pid = new Pid(config.steerP, config.steerI, config.steerD);

        while (active.isActive() && timer.seconds() < config.timeoutSecs) {
            double error = angleWrapDeg(targetHeadingDeg - imuYawDeg());
            if (Math.abs(error) < 1.0) break;

            double turn = clampPower(pid.update(error, timer.seconds()), Math.abs(error) > 8.0);
            robot.drive(0, 0, turn);
            if (debugSink != null) {
                debugSink.onLoop(error, turn, pid.lastP, pid.lastI, pid.lastD);
            }
            telemetry.addData("turn err deg", error);
            telemetry.update();
        }
        robot.stop();
    }

    private double headingHoldPower(int targetHeadingDeg) {
        double error = angleWrapDeg(targetHeadingDeg - imuYawDeg());
        return clampPower(error * config.steerP, true);   // active correction keeps its deadband
    }

    /** Clamped PID loop with derivative on error and sign-change anti-windup. */
    private static final class Pid {
        private final double kp;
        private final double ki;
        private final double kd;
        private double integral;
        private double prevError;
        private double prevTime = -1;
        private double lastP;
        private double lastI;
        private double lastD;

        Pid(double kp, double ki, double kd) {
            this.kp = kp;
            this.ki = ki;
            this.kd = kd;
        }

        double update(double error, double now) {
            double dt = prevTime < 0 ? 0.016 : Math.max(1e-3, now - prevTime);
            if (error == 0 || (prevTime >= 0 && Math.signum(error) != Math.signum(prevError))) {
                integral = 0;
            } else {
                integral = Math.max(-INTEGRAL_LIMIT, Math.min(INTEGRAL_LIMIT,
                        integral + error * dt));
            }
            double deriv = prevTime < 0 ? 0 : (error - prevError) / dt;
            lastP = kp * error;
            lastI = ki * integral;
            lastD = kd * deriv;
            prevError = error;
            prevTime = now;
            return lastP + lastI + lastD;
        }
    }

    private double clampPower(double power, boolean applyDeadband) {
        power = CrawlerMath.clamp(power, -0.7, 0.7);
        if (applyDeadband && Math.abs(power) < config.minPower && Math.abs(power) > 1e-6) {
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
