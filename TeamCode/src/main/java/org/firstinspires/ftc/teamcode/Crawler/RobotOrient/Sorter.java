package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.Robot;

@Deprecated()
public class Sorter {

    int tries = 3;
    public static Robot robot;

    public Sorter(HardwareMap hwMap) {
        robot = new Robot(hwMap);
    }

    public void getBall(BALLCOLOR ballcolor) {
        int temp = tries;
        if(tries <= 0) return;
        if(!(getDetectedColor() == ballcolor)) {
            robot.indexer.setTargetPosition(10); //TODO tune this
            temp--;
            tries = temp;
            getBall(ballcolor);
        }

        //Got the correct ball at the color sensor
        // and then go the the shooter position
        robot.indexer.setTargetPosition(0); //TODO tune this
        robot.lifter.setPosition(0.5); //TODO tune this
        robot.activateShooters(false);

    }

    public static BALLCOLOR getDetectedColor() {
        if (robot.ballColorSensor.red() > robot.ballColorSensor.blue()) {
            return BALLCOLOR.PURPLE;
        }

        if(robot.ballColorSensor.green() > robot.ballColorSensor.blue()) {
            return BALLCOLOR.GREEN;
        }

        return BALLCOLOR.UNKNOWN;
    }


}
