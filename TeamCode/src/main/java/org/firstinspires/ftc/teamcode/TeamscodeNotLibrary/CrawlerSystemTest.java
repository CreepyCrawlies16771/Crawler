package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.Arrays;

/**
 * Interactive validation for a tuned {@link MyRobot} (~15–20 min with Crawler Tuner).
 *
 * <p>Circle = next test, Square = show pass criteria, X = previous.</p>
 */
@TeleOp(name = "Crawler System Test", group = "Crawler Tests")
public class CrawlerSystemTest extends LinearOpMode {

    private enum Test {
        TELEOP_DRIVE(1, "Manual drive — sticks move robot"),
        ODOMETRY_FORWARD(2, "RB: drive forward ~50 cm"),
        ODOMETRY_STRAFE(3, "RB: strafe right ~50 cm"),
        PATH_SQUARE(4, "RB: 50 cm square path"),
        CONFIG_REVIEW(5, "Review config on DS"),
        DONE(6, "All tests complete");

        final int num;
        final String title;

        Test(int num, String title) {
            this.num = num;
            this.title = title;
        }
    }

    private static final double MOVE_TARGET_CM = 50.0;
    private static final double MOVE_POWER = 0.4;

    private MyRobot robot;
    private Test test = Test.TELEOP_DRIVE;
    private boolean testRunning;
    private String result = "";

    private final boolean[] circleEdge = {false};
    private final boolean[] squareEdge = {false};
    private final boolean[] xEdge = {false};
    private final boolean[] rbEdge = {false};

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Crawler System Test");
        telemetry.addLine("Run after Crawler Tuner + Smoke Test");
        telemetry.update();

        robot = new MyRobot(hardwareMap);
        waitForStart();

        while (opModeIsActive()) {
            if (testRunning) {
                idle();
                continue;
            }

            handleButtons();
            telemetry.clear();
            telemetry.addLine("Test " + test.num + "/6: " + test.title);
            telemetry.addLine("Circle=next  X=back  Square=help");

            switch (test) {
                case TELEOP_DRIVE:     runTeleopDrive(); break;
                case ODOMETRY_FORWARD: runOdometryPrompt(true); break;
                case ODOMETRY_STRAFE:  runOdometryPrompt(false); break;
                case PATH_SQUARE:      runPathPrompt(); break;
                case CONFIG_REVIEW:    runConfigReview(); break;
                case DONE:             runDone(); break;
            }

            if (!result.isEmpty()) telemetry.addLine(result);
            telemetry.update();
        }
        robot.stop();
    }

    private void handleButtons() {
        if (pressed(gamepad1.circle, circleEdge) && test != Test.DONE) {
            test = Test.values()[test.ordinal() + 1];
            result = "";
        }
        if (pressed(gamepad1.x, xEdge) && test != Test.TELEOP_DRIVE) {
            test = Test.values()[test.ordinal() - 1];
            result = "";
        }
        if (pressed(gamepad1.square, squareEdge)) {
            result = helpFor(test);
        }
    }

    private void runTeleopDrive() {
        robot.update();
        double fwd = -gamepad1.left_stick_y;
        double str = gamepad1.left_stick_x;
        double rot = gamepad1.right_stick_x;
        robot.driveFieldRelative(fwd, str, rot);
        telemetry.addData("X", robot.getPose().getX());
        telemetry.addData("Y", robot.getPose().getY());
        telemetry.addData("Heading°", Math.toDegrees(robot.getHeading()));
    }

    private void runOdometryPrompt(boolean forward) throws InterruptedException {
        telemetry.addLine("RB: run move test");
        telemetry.addData("target cm", MOVE_TARGET_CM);
        if (pressed(gamepad1.right_bumper, rbEdge)) {
            testRunning = true;
            result = forward ? runMoveTest(0) : runMoveTest(1);
            testRunning = false;
        }
    }

    /** @param mode 0 forward, 1 strafe right */
    private String runMoveTest(int mode) throws InterruptedException {
        robot.resetPose();
        sleep(100);
        Pose2d start = robot.getPose();
        ElapsedTime timer = new ElapsedTime();

        while (opModeIsActive() && timer.seconds() < robot.config.timeoutSecs) {
            robot.update();
            double d = Math.hypot(
                    robot.getPose().getX() - start.getX(),
                    robot.getPose().getY() - start.getY());
            if (d >= MOVE_TARGET_CM) break;
            if (mode == 0) robot.drive(MOVE_POWER, 0, 0);
            else robot.drive(0, MOVE_POWER, 0);
            Thread.yield();
        }
        robot.stop();
        robot.update();
        double finalD = Math.hypot(
                robot.getPose().getX() - start.getX(),
                robot.getPose().getY() - start.getY());
        double errPct = Math.abs(finalD - MOVE_TARGET_CM) / MOVE_TARGET_CM * 100;
        if (errPct < 15) return "PASS: " + String.format("%.1f cm (%.0f%% err)", finalD, errPct);
        return "CHECK: " + String.format("%.1f cm (%.0f%% err) — tweak MyRobot builder", finalD, errPct);
    }

    private void runPathPrompt() throws InterruptedException {
        telemetry.addLine("RB: run 50 cm square");
        if (pressed(gamepad1.right_bumper, rbEdge)) {
            testRunning = true;
            robot.resetPose();
            FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
            try {
                follower.follow(Arrays.asList(
                        Waypoint.at(0, 0, robot.config).build(),
                        Waypoint.at(50, 0, robot.config).build(),
                        Waypoint.at(50, 50, robot.config).build(),
                        Waypoint.at(0, 50, robot.config).build(),
                        Waypoint.at(0, 0, robot.config).build()
                ));
                double err = Math.hypot(robot.getPose().getX(), robot.getPose().getY());
                result = err < robot.config.arrivalThresholdCm * 2
                        ? "PASS: returned near origin"
                        : "CHECK: ended " + String.format("%.1f cm from start", err);
            } catch (InterruptedException e) {
                result = "Interrupted";
            }
            testRunning = false;
        }
    }

    private void runConfigReview() {
        telemetry.addData("trackWidth in", robot.config.trackWidthIn);
        telemetry.addData("centerOffset in", robot.config.centerWheelOffsetIn);
        telemetry.addData("wheelDiameter in", robot.config.wheelDiameterIn);
        telemetry.addData("ticksPerRev", robot.config.ticksPerRev);
        telemetry.addData("driveKp", robot.config.driveKp);
        telemetry.addData("moveSpeed", robot.config.defaultMoveSpeed);
        telemetry.addData("arrival cm", robot.config.arrivalThresholdCm);
        robot.stop();
    }

    private void runDone() {
        telemetry.addLine("If all steps passed, robot is ready for autos.");
        telemetry.addLine("Use Example Auto as a template.");
        robot.stop();
    }

    private static String helpFor(Test t) {
        switch (t) {
            case TELEOP_DRIVE:
                return "PASS if sticks move robot smoothly in all directions.";
            case ODOMETRY_FORWARD:
                return "PASS if distance within ~15% of 50 cm.";
            case ODOMETRY_STRAFE:
                return "PASS if strafe within ~15%; low heading drift.";
            case PATH_SQUARE:
                return "PASS if path completes and ends near start.";
            case CONFIG_REVIEW:
                return "Values should match MyRobot.java builder chain.";
            default:
                return "Run Example Auto on the field.";
        }
    }

    private static boolean pressed(boolean current, boolean[] edge) {
        boolean rising = current && !edge[0];
        edge[0] = current;
        return rising;
    }
}
