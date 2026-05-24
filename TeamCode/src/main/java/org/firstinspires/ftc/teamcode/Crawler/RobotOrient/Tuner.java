package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * @deprecated Use a team OpMode in {@code TeamscodeNotLibrary} that extends
 * {@link ROMovementEngine} and returns your {@code MyRobot}.
 */
@Deprecated
public class Tuner extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        throw new UnsupportedOperationException(
                "Extend ROMovementEngine in TeamscodeNotLibrary and use MyRobot.");
    }

    @Override
    public void runPath() {
    }
}
