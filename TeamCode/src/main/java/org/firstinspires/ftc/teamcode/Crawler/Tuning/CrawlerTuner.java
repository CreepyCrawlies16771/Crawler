package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/**
 * One OpMode to tune Crawler. Copy printed builder lines into
 * {@code TeamscodeNotLibrary/MyRobot.java} — do not edit the Crawler library.
 */
@TeleOp(name = "Crawler Tuner", group = "Crawler")
public class CrawlerTuner extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addLine("Crawler Tuner");
        telemetry.addLine("Sync TuningRobotConfig with RobotHardware.java");
        telemetry.addLine("Square = copy-paste for MyRobot.java");
        telemetry.update();

        TuningSession session = new TuningSession(
                hardwareMap, telemetry, gamepad1, () -> opModeIsActive());

        waitForStart();

        while (opModeIsActive()) {
            session.loop();
        }
        session.getRobot().stop();
    }
}
