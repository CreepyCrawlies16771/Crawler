package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Robot {

    private static DcMotorEx frontRight;
    private static DcMotorEx frontLeft;
    private static DcMotorEx backRight;
    private static DcMotorEx backLeft;

    private CrawlerRobot userRobot;

    public Robot(HardwareMap hwMap) {

        frontRight = (DcMotorEx) userRobot.frontRight;
        frontLeft = (DcMotorEx) userRobot.frontLeft;
        backRight = (DcMotorEx) userRobot.backRight;
        backLeft = (DcMotorEx) userRobot.backLeft;


    }


    public static void simpleDriveTrainPower(double frontRightPower, double frontLeftPower, double backRightPower, double backLeftPower)
    {
        frontRight.setPower(frontRightPower);
        frontLeft.setPower(frontLeftPower);
        backRight.setPower(backRightPower);
        backLeft.setPower(backLeftPower);
    }

    public static void applyClampedDriveTrainPower(double motorPower, double direction){

    }

}