package org.firstinspires.ftc.teamcode.Teamcode.CrawlerOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Crawler.Tuning.TuningRobotFactory;
import org.firstinspires.ftc.teamcode.Crawler.Tuning.TuningSession;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobotRegistry;

/**
 * One OpMode to tune Crawler.
 *
 * <p>The tuning robot is built through {@link CrawlerRobotRegistry}, so the tuner works
 * with whatever robot class you registered (see docs/setup.md) — it never references a
 * shipped example robot. Tuning values are seeded from your robot's builder, and pressing
 * <b>Square</b> (or finishing Step 7) prints the tuned builder lines to paste back into
 * your robot class.</p>
 */
@TeleOp(name = "Crawler Tuner", group = "Crawler")
public class CrawlerTuner extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Crawler Tuner");
        telemetry.addLine("Robot built from your registered robot — no sync needed");
        telemetry.addLine("Edit values in FTC Dashboard -> Crawler Tuner");
        telemetry.addLine("Square = copy-paste builder lines for your robot");
        telemetry.update();

        TuningRobotFactory factory = new TuningRobotFactory() {
            @Override
            public CrawlerRobot create() {
                return CrawlerRobotRegistry.create(hardwareMap);
            }

            @Override
            public CrawlerRobot create(CrawlerRobot.Config config) {
                return CrawlerRobotRegistry.create(hardwareMap, config);
            }
        };

        TuningSession session;
        try {
            session = new TuningSession(factory, telemetry, gamepad1,
                    () -> opModeIsActive());
        } catch (Exception e) {
            telemetry.addLine("Crawler Tuner could not build your robot.");
            telemetry.addLine(e.getMessage());
            telemetry.update();
            return;
        }

        waitForStart();

        while (opModeIsActive()) {
            session.loop();
        }
        session.getRobot().stop();
    }
}
