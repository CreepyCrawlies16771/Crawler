package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

/**
 * Example Autonomous OpMode for the Crawler library.
 *
 * <p>This OpMode demonstrates field-oriented autonomous movement using {@code FOFollower}
 * and {@code Waypoint} objects. The robot starts at (0, 0), opens the claw, drives to
 * several waypoints, performs actions at each waypoint, and returns home.</p>
 *
 * <p><b>Path:</b></p>
 * <ol>
 *   <li>Start at (0, 0)</li>
 *   <li>Drive to (60, 0) at 0.8 speed → open claw</li>
 *   <li>Drive to (60, 60) slowly → score high basket</li>
 *   <li>Return to (0, 0)</li>
 * </ol>
 *
 * <p>All waypoints use field-oriented pure pursuit control for smooth, curved paths.
 * Actions are triggered via {@code onReach()} callbacks when the robot arrives at
 * each waypoint.</p>
 */
@Autonomous(name = "Example Auto", group = "Crawler Examples")
public class ExampleAuto extends LinearOpMode {

    /**
     * Main OpMode entry point.
     *
     * @throws InterruptedException if the OpMode is stopped or interrupted
     */
    @Override
    public void runOpMode() throws InterruptedException {
        // Build the robot using the MyRobot builder
        MyRobot robot = new MyRobot(hardwareMap);

        // Create the FOFollower instance
        // Pass a lambda that returns opModeIsActive() so the follower can check if the OpMode is still running
        FOFollower follower = new FOFollower(
                robot,
                telemetry,
                this::opModeIsActive
        );

        // Wait for start button
        telemetry.addData("Status", "Ready");
        telemetry.addLine("Press PLAY to begin autonomous path");
        telemetry.update();
        waitForStart();

        try {
            // Create the autonomous path with waypoints and callbacks
            follower.follow(
                    Waypoint.at(0, 0, robot.config)
                            .speed(0.8)
                            .build(),

                    Waypoint.at(60, 0, robot.config)
                            .speed(0.8)
                            .turnSpeed(0.4)
                            .onReach(() -> {
                                telemetry.addData("Action", "Opening claw at (60, 0)");
                                telemetry.update();
                                robot.openClaw();
                            })
                            .build(),

                    Waypoint.at(60, 60, robot.config)
                            .slow(robot.config)
                            .onReach(() -> {
                                telemetry.addData("Action", "Scoring high basket at (60, 60)");
                                telemetry.update();
                                robot.scoreHighBasket();
                            })
                            .build(),

                    Waypoint.at(0, 0, robot.config)
                            .speed(0.8)
                            .turnSpeed(0.4)
                            .onReach(() -> {
                                telemetry.addData("Status", "Returned home!");
                                telemetry.update();
                            })
                            .build()
            );

            telemetry.addData("Status", "Path Complete");
            telemetry.addData("Final Pose X (cm)", String.format("%.2f", robot.getPose().getX()));
            telemetry.addData("Final Pose Y (cm)", String.format("%.2f", robot.getPose().getY()));
            telemetry.update();

        } catch (InterruptedException e) {
            telemetry.addData("Status", "Path interrupted");
            telemetry.addData("Error", e.getMessage());
            telemetry.update();
        } finally {
            // Always stop the robot when done
            robot.stop();
        }
    }
}
