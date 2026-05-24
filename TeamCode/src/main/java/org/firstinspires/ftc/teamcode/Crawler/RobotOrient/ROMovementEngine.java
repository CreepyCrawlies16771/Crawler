package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Base class for short robot-relative moves ({@code drivePID}, {@code strafePID}, {@code turnPID}).
 *
 * <p>Uses your team's {@link CrawlerRobot} from {@link #buildRobot(HardwareMap)} — no hard-coded
 * motor names. PID values come from the robot builder {@code drivePid} / {@code strafePid} /
 * {@code steerPid} in {@code MyRobot.java}.</p>
 */
public abstract class ROMovementEngine extends LinearOpMode {

    protected CrawlerRobot robot;
    protected RobotOrientedDrive movement;

    /** Construct and return your configured robot (typically {@code new MyRobot(hwMap)}). */
    protected abstract CrawlerRobot buildRobot(HardwareMap hwMap);

  public abstract void runPath() throws InterruptedException;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = buildRobot(hardwareMap);
        movement = new RobotOrientedDrive(robot, this::opModeIsActive, telemetry);

        telemetry.addData("Status", "Ready");
        telemetry.update();
        waitForStart();

        if (opModeIsActive()) {
            robot.imu.resetYaw();
            robot.resetPose();
            runPath();
        }
        robot.stop();
    }

    public void drivePID(double targetMeters, int targetAngle) {
        movement.drivePID(targetMeters, targetAngle);
    }

    public void strafePID(double targetMeters, int targetAngle) {
        movement.strafePID(targetMeters, targetAngle);
    }

    public void turnPID(int targetAngle) {
        movement.turnPID(targetAngle);
    }
}
