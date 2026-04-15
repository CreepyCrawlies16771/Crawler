package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;


import static android.os.SystemClock.sleep;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.Vision.AprilTagWebcam;
import org.firstinspires.ftc.teamcode.Crawler.Vision.Rotation;
import org.firstinspires.ftc.teamcode.annotations.Experimental;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;

/**
 * This is the robot oriented movement engine
 * @deprecated Opmodeactive boolean not jet implemented
 */
public abstract class ROMovementEngine {

    public abstract void runPath() throws InterruptedException;

    private CrawlerRobot robot;

    public boolean isOpmodeRunning = false;

    private DcMotorEx rightOdo,leftOdo,centerOdo;

    private IMU imu;
    public void init() throws InterruptedException {

        imu = robot.imu;

        rightOdo = (DcMotorEx) robot.rightEncoder;
        leftOdo = (DcMotorEx) robot.leftEncoder;
        centerOdo = (DcMotorEx) robot.centerEncoder;
    }



    /**
     * Move the robot forward while using a pid/gyro correction
     * @param targetMeters distance to travel in meters
     * @param targetAngle angle to turn to in degrees
     */
    public void drivePID(double targetMeters, int targetAngle) {
        double targetTicks = targetMeters * RobotConfig.RobotBase.TICKS_PER_METER;

        // 1. SAFE START: Calculate start position instead of resetting hardware
        // resetting hardware encoders can be slow/laggy in loops
        double startPos = ((leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0);

        double error = targetTicks;
        double lastError = 0;
        double integral = 0;

        // 2. TIMEOUT: Prevent infinite loops if sensors fail
        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        while ((timer.seconds() < RobotConfig.RobotBase.timeoutSecs) && Math.abs(error) > 50) {

            // Calculate current distance traveled relative to start
            double rawCurrentPos = ((leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0) * -1;
            double currentPos = rawCurrentPos - startPos;

            // 3. DEBUGGING: If this number goes NEGATIVE when driving FORWARD,
            // you must reverse your encoder direction in the config or code.

            error = targetTicks - currentPos;

            double derivative = error - lastError;

            // Integral anti-windup (only accumulate when close to target)
            if (Math.abs(error) < (0.1 * RobotConfig.RobotBase.TICKS_PER_METER)) {
                integral += error;
            } else {
                integral = 0;
            }

            double power = (RobotConfig.RobotOriented.Kp * (error / RobotConfig.RobotBase.TICKS_PER_METER))
                    + (RobotConfig.RobotOriented.Ki * integral)
                    + (RobotConfig.RobotOriented.Kd * derivative);

            // Steering logic
            double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double steer = angleWrap(targetAngle - currentYaw) * RobotConfig.RobotOriented.STEER_P;

            // Clamp power
            power = Math.max(-0.7, Math.min(0.7, power));

            // Feedforward / Minimum power to overcome friction
            if (Math.abs(power) < RobotConfig.RobotOriented.MIN_POWER && Math.abs(error) > 50) {
                power = Math.signum(power) * RobotConfig.RobotOriented.MIN_POWER;
            }

            applyDrivePower(power, -steer);
            lastError = error;

        }
        robot.driveTrain.stop();
    }


    /**
     * Move the robot sideways while using a pid/gyro correction
     * @param targetMeters distance to travel in meters
     * @param targetAngle angle to turn to in degrees*/

    public void strafePID(double targetMeters, int targetAngle) {
        double targetTicks = targetMeters * RobotConfig.RobotBase.TICKS_PER_METER;
        double error = targetTicks;
        double lastError = 0;
        double integral = 0;

        final int maxError = 50;


        while (Math.abs(error) > maxError) {
            double currentPos =  centerOdo.getCurrentPosition();

            //currentPos = currentPos * -1; // if the odometry pods are mounted backwards

            error = targetTicks - currentPos;

            // PID Logic
            double derivative = error - lastError;
            integral += error;

            // Anti-windup cap
            if (Math.abs(error) < (0.1 * RobotConfig.RobotBase.TICKS_PER_METER)) { // Only use Integral when close
                integral = Math.max(-20, Math.min(20, integral));
            } else {
                integral = 0;
            }

            double power = (RobotConfig.RobotOriented.strafe_Kp * (error / RobotConfig.RobotBase.TICKS_PER_METER)) +
                    (RobotConfig.RobotOriented.strafe_Ki * integral) +
                    (RobotConfig.RobotOriented.strafe_Kd * derivative);

            // Steering with Angle Wrap
            double currentYaw = -imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double steer = angleWrap(currentYaw - targetAngle) * -RobotConfig.RobotOriented.STEER_P;

            power = Math.max(-0.7, Math.min(0.7, power));
            if (Math.abs(power) < RobotConfig.RobotOriented.MIN_POWER) power = Math.signum(power) * RobotConfig.RobotOriented.MIN_POWER;

            applyStrafePower(power, steer);
            lastError = error;


        }
        robot.driveTrain.stop();
    }

    /**
     * Turn the robot to desired angle
     * @param targetAngle angle to turn to in degrees*/

    public void turnPID(int targetAngle) {
        // 1. Calculate error
        double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double error = angleWrap(targetAngle - currentYaw);

        // 2. Loop until error is small (e.g., < 1 degree)
        while (Math.abs(error) > 1.0) {
            currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            error = angleWrap(targetAngle - currentYaw);

            // Simple P-Control for turning
            // We use a higher P value here because turning needs more punch than steering correction
            double turnPower = error * 0.03;

            // Clamp power to avoid moving too fast or stalling
            turnPower = Math.max(-0.6, Math.min(0.6, turnPower));
            if (Math.abs(turnPower) < 0.15) turnPower = Math.signum(turnPower) * 0.15;

            // Apply power (Turn Right = Left Forward, Right Back)
            robot.driveTrain.applyDriveTrainPowerSingle(
                    -turnPower, -turnPower, -turnPower, -turnPower
            );

        }
        robot.driveTrain.stop();
    }

    @Experimental("PID, accuracy not copley done, not as accurate as the rest")
    public void arc(double meters, double maxPower, AnimationBuilder animator) {
        double targetTicks = meters * RobotConfig.RobotBase.TICKS_PER_METER;
        if (Math.abs(targetTicks) < 10) return;

        double startHeading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        HeadingTimeline timeline = new HeadingTimeline();
        animator.build(timeline);

        resetOdometry();
        sleep(100);

        final double POSITION_DEADBAND_TICKS = 0.05 * RobotConfig.RobotBase.TICKS_PER_METER;
        long startTime = System.currentTimeMillis();

        while (isOpmodeRunning) {
            double currentPos = (leftOdo.getCurrentPosition() + rightOdo.getCurrentPosition()) / 2.0;
            double error = targetTicks - currentPos;

            // Exit if within deadband OR if we have physically passed the target distance
            if (Math.abs(error) < POSITION_DEADBAND_TICKS || Math.abs(currentPos) >= Math.abs(targetTicks)) {
                break;
            }

            // Safety timeout
            if (System.currentTimeMillis() - startTime > 8000) break;

            double progress = Math.max(0, Math.min(Math.abs(currentPos) / Math.abs(targetTicks), 1.0));
            double targetHeading = timeline.getTarget(progress, startHeading);
            double currentYaw = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double headingError = angleWrap(targetHeading - currentYaw);

            double power = (error / RobotConfig.RobotBase.TICKS_PER_METER) * RobotConfig.RobotOriented.Kp;
            power = Math.max(-maxPower, Math.min(maxPower, power));

            if (Math.abs(power) < RobotConfig.RobotOriented.MIN_POWER) {
                power = Math.signum(error) * RobotConfig.RobotOriented.MIN_POWER;
            }

            // Flip steer sign to fix "wrong way" arc and maintain turning authority near end
            double steer = -(headingError * RobotConfig.RobotOriented.STEER_P);
            steer = Math.max(-0.6, Math.min(0.6, steer));

            double steeringScale = Math.max(Math.abs(power), 0.3);
            steer *= steeringScale;

            applyDrivePower(power, steer);

        }

        robot.driveTrain.stop();
    }

    // --- HELPERS ---

    private void applyDrivePower(double p, double s) {
        // p = forward power, s = steer (turning)

        robot.driveTrain.applyDriveTrainPowerSingle(
                p+s,
                p-s,
                p+s,
                p-s
        );
    }

    public void applyStrafePower(double strafe, double steer) {
        // Mecanum Strafe Pattern:
        // FrontLeft and BackRight go one way
        // FrontRight and BackLeft go the other way
        double fl =  strafe + steer;
        double fr = -strafe - steer;
        double bl = -strafe + steer;
        double br =  strafe - steer;

        // Normalize power so no motor exceeds 1.0
        double max = Math.max(Math.abs(fl), Math.max(Math.abs(fr),
                Math.max(Math.abs(bl), Math.abs(br))));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        robot.driveTrain.applyDriveTrainPowerSingle(
                fl,
                fr,
                bl,
                br
        );
    }


    private void resetOdometry() {
        leftOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        centerOdo.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        centerOdo.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    private  double angleWrap(double degrees) {
        while (degrees > 180) degrees -= 360;
        while (degrees < -180) degrees += 360;
        return degrees;
    }

}