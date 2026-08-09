package org.firstinspires.ftc.teamcode.Example;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

public class BobTheRobot extends CrawlerRobot {
    public final DcMotor gobbler;
    public final Servo openShooterServo;

    protected BobTheRobot(HardwareMap hwMap) {
        // CrawlerRobot's constructor takes the BUILDER (not a built robot), so the
        // chain is passed directly — do NOT call .build() here.
        super(
                new Builder(hwMap)
                        .frontLeft("leftFront")
                        .frontRight("rightFront")
                        .backLeft("leftBack")
                        .backRight("backRight")
                        .imu("imu")            // required by the builder
                        .motors()
                        .withThreeDeadWheels("leftFront", "leftBack", "rightFront")
                        .setTrackWidth(34.44)
                        .setCenterWheelOffset(8.98)
        );

        this.gobbler = hwMap.get(DcMotor.class, "goobler");
        this.openShooterServo = hwMap.get(Servo.class, "oss");
    }
}
