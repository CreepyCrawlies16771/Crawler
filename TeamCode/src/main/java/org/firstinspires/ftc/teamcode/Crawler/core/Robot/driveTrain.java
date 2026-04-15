package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * This is the class that controls/powers the motors
 */
public class driveTrain {
    DcMotor frontLeft,frontRight,backLeft,backRight;

    public driveTrain(CrawlerRobot robotInstance) {
        frontLeft = (DcMotor) robotInstance.frontLeft;
        frontRight = (DcMotor) robotInstance.frontRight;
        backLeft = (DcMotor) robotInstance.backLeft;
        backRight = (DcMotor) robotInstance.backRight;
    }

    public void stop() {
        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        frontLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        backLeft.setPower(0);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.UNKNOWN);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.UNKNOWN);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.UNKNOWN);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.UNKNOWN);
    }

    public void applyDriveTrainPowerSingle(double frontLeftPower,
                                           double frontRightPower,
                                           double backLeftPower,
                                           double backRightPower)
    {

        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        backLeft.setPower(backLeftPower);
        backRight.setPower(backRightPower);

    }

    /**
     * spins all motors with power
     * @param power
     */
    public void spin(double power) {
        applyDriveTrainPowerSingle(
                power,
                power,
                power,
                power
        );
    }

    public void drive(double forward, double strafe, double rotate) {
        double fl = forward + strafe + rotate;
        double fr = forward - strafe - rotate;
        double bl = forward - strafe + rotate;
        double br = forward + strafe - rotate;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br))));
        frontLeft.setPower(fl / max);
        frontRight.setPower(fr / max);
        backLeft.setPower(bl / max);
        backRight.setPower(br / max);
    }
}
