package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * Example TeleOp OpMode for the Crawler library.
 *
 * <p>This OpMode demonstrates field-oriented driving with {@code CrawlerRobot} and
 * season-specific hardware from {@code MyRobot}. Driver 1 controls movement and claw,
 * while Driver 2 controls the lift motor.</p>
 *
 * <p><b>Driver 1 Controls:</b></p>
 * <ul>
 *   <li>Left stick: Forward/backward and strafe (field-relative)</li>
 *   <li>Right stick X: Rotate (independent)</li>
 *   <li>A button: Open claw</li>
 *   <li>B button: Close claw</li>
 * </ul>
 *
 * <p><b>Driver 2 Controls:</b></p>
 * <ul>
 *   <li>Left trigger: Lower lift (target 0 ticks)</li>
 *   <li>Right trigger: Raise lift (target 800 ticks)</li>
 * </ul>
 *
 * <p>The robot's position and heading are displayed on telemetry every loop cycle.</p>
 */
@TeleOp(name = "Example TeleOp", group = "Crawler Examples")
public class ExampleTeleOp extends LinearOpMode {

    /**
     * Main OpMode entry point.
     *
     * @throws InterruptedException if the OpMode is stopped or interrupted
     */
    @Override
    public void runOpMode() throws InterruptedException {
        // Build the robot using the MyRobot builder
        MyRobot robot = new MyRobot(hardwareMap);

        // Wait for start button
        telemetry.addData("Status", "Ready");
        telemetry.update();
        waitForStart();

        if (!opModeIsActive()) {
            return;
        }

        // Main loop
        while (opModeIsActive()) {
            // Update localiser to get current pose
            robot.update();

            // ===== Driver 1: Main Movement Control =====
            double forward = -gamepad1.left_stick_y;   // Forward/backward
            double strafe = gamepad1.left_stick_x;     // Strafe left/right
            double rotate = gamepad1.right_stick_x;    // Rotate

            // Apply field-relative driving
            robot.driveFieldRelative(forward, strafe, rotate);

            // Claw control
            if (gamepad1.a) {
                robot.openClaw();
            } else if (gamepad1.b) {
                robot.closeClaw();
            }

            // ===== Driver 2: Lift Control =====
            if (gamepad2.left_trigger > 0.1) {
                // Lower lift to 0
                robot.setLift(0);
            } else if (gamepad2.right_trigger > 0.1) {
                // Raise lift to 800 ticks
                robot.setLift(800);
            } else {
                // Stop lift when no input
                robot.stopLift();
            }

            // ===== Telemetry: Display Robot Pose =====
            com.arcrobotics.ftclib.geometry.Pose2d pose = robot.getPose();
            double headingDegrees = Math.toDegrees(robot.getHeading());

            telemetry.addData("X (cm)", String.format("%.2f", pose.getX()));
            telemetry.addData("Y (cm)", String.format("%.2f", pose.getY()));
            telemetry.addData("Heading (°)", String.format("%.2f", headingDegrees));
            telemetry.addLine();
            telemetry.addData("Forward", String.format("%.2f", forward));
            telemetry.addData("Strafe", String.format("%.2f", strafe));
            telemetry.addData("Rotate", String.format("%.2f", rotate));
            telemetry.update();

            idle();
        }

        // Stop robot when OpMode ends
        robot.stop();
    }
}
