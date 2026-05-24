package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Fast automated smoke test (~2 min). Run after tuning or before every practice.
 *
 * <p>Checks: robot builds, pose updates, drive moves the robot, config is wired.</p>
 */
@Autonomous(name = "Crawler Smoke Test", group = "Crawler Tests")
public class CrawlerSmokeTest extends LinearOpMode {

    private static final double MIN_POSE_DELTA_CM = 5.0;
    private static final double DRIVE_POWER = 0.35;
    private static final double DRIVE_TIME_SEC = 2.0;

    @Override
    public void runOpMode() throws InterruptedException {
        boolean pass = true;
        String failReason = "";

        telemetry.addLine("Crawler Smoke Test");
        telemetry.addLine("Builds MyRobot and runs short drive check");
        telemetry.update();

        MyRobot robot;
        try {
            robot = new MyRobot(hardwareMap);
        } catch (Exception e) {
            telemetry.addLine("FAIL: Could not build MyRobot");
            telemetry.addLine(e.getMessage());
            telemetry.update();
            return;
        }

        waitForStart();
        robot.resetPose();
        sleep(200);

        double x0 = robot.getPose().getX();
        double y0 = robot.getPose().getY();

        ElapsedTime driveTimer = new ElapsedTime();
        while (opModeIsActive() && driveTimer.seconds() < DRIVE_TIME_SEC) {
            robot.update();
            robot.drive(DRIVE_POWER, 0, 0);
            telemetry.addData("Pose X", robot.getPose().getX());
            telemetry.addData("Pose Y", robot.getPose().getY());
            telemetry.update();
        }
        robot.stop();
        robot.update();

        double delta = Math.hypot(robot.getPose().getX() - x0, robot.getPose().getY() - y0);
        if (delta < MIN_POSE_DELTA_CM) {
            pass = false;
            failReason = "Odometry moved " + String.format("%.1f", delta)
                    + " cm (need " + MIN_POSE_DELTA_CM + "+). Check encoders / tuning.";
        }

        if (robot.config.trackWidthIn <= 0 || robot.config.ticksPerRev <= 0) {
            pass = false;
            failReason = "Invalid config on robot — re-run Crawler Tuner and update MyRobot.";
        }

        telemetry.addLine("---");
        telemetry.addData("Distance reported (cm)", String.format("%.2f", delta));
        telemetry.addData("trackWidth in", robot.config.trackWidthIn);
        telemetry.addData("ticksPerRev", robot.config.ticksPerRev);
        if (pass) {
            telemetry.addLine("RESULT: PASS");
            telemetry.addLine("Next: run Crawler System Test");
        } else {
            telemetry.addLine("RESULT: FAIL");
            telemetry.addLine(failReason);
        }
        telemetry.update();
    }
}
