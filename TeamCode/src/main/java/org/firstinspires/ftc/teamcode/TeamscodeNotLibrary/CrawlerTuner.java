package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Crawler.Tuning.TuningRobotFactory;
import org.firstinspires.ftc.teamcode.Crawler.Tuning.TuningSession;

/**
 * One OpMode to tune Crawler.
 *
 * <p>The tuning robot is rebuilt from {@link MyRobot#builder} (via {@link MyRobot#buildTuned})
 * with the live tuning values — there is no separate config file to keep in sync. Press
 * <b>Square</b> (or finish Step 7) to print the tuned builder lines and paste them into
 * {@link MyRobot#builder} to make them permanent.</p>
 */
@TeleOp(name = "Crawler Tuner", group = "Crawler")
public class CrawlerTuner extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Crawler Tuner");
        telemetry.addLine("Robot built from MyRobot.builder — no sync needed");
        telemetry.addLine("Edit values in FTC Dashboard -> Crawler Tuner");
        telemetry.addLine("Square = copy-paste builder lines for MyRobot.java");
        telemetry.update();

        TuningRobotFactory factory = config -> MyRobot.buildTuned(hardwareMap, config);
        TuningSession session = new TuningSession(factory, telemetry, gamepad1,
                () -> opModeIsActive());

        waitForStart();

        while (opModeIsActive()) {
            session.loop();
        }
        session.getRobot().stop();
    }
}
