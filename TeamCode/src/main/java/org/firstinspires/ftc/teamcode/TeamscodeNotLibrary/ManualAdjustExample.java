package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Example short robot-relative moves using {@link ROMovementEngine}.
 * Use for small alignment adjustments between field-oriented segments.
 */
@Autonomous(name = "Manual Adjust Example", group = "Crawler Tests")
public class ManualAdjustExample extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }

    @Override
    public void runPath() {
        // 30 cm forward, hold 0°
        drivePID(0.30, 0);
        // Turn to 45°
        turnPID(45);
        // 20 cm strafe right, hold 45°
        strafePID(0.20, 45);
    }
}
