package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Your robot — edit {@link RobotHardware} for names and the builder chain for tuning.
 *
 * <p>Run <b>Crawler Tuner</b>, press Square, paste printed lines into the builder below.</p>
 */
public class MyRobot extends CrawlerRobot {

    public final Servo clawServo;
    public final DcMotor liftMotor;

    public MyRobot(HardwareMap hwMap) {
        super(new CrawlerRobot.Builder(hwMap)
                .frontLeft(RobotHardware.FRONT_LEFT)
                .frontRight(RobotHardware.FRONT_RIGHT)
                .backLeft(RobotHardware.BACK_LEFT)
                .backRight(RobotHardware.BACK_RIGHT)
                .imu(RobotHardware.IMU)
                .imuOrientation(RobotHardware.IMU_LOGO, RobotHardware.IMU_USB)
                .motors()
                .withThreeDeadWheels(
                        RobotHardware.ENC_LEFT,
                        RobotHardware.ENC_RIGHT,
                        RobotHardware.ENC_CENTER)
                // --- Paste from Crawler Tuner (Square) ---
                .setTrackWidth(33.02)
                .setCenterWheelOffset(8.89)
                .wheelDiameter(1.37795)
                .ticksPerRev(2000)
                .drivePid(0.05, 0.0, 0.0)
                .strafePid(0.05, 0.0, 0.0)
                .steerPid(0.03, 0.0, 0.0)
                .pathDefaults(0.7, 0.4, 25.4)
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .build());
        this.clawServo = hwMap.get(Servo.class, RobotHardware.CLAW_SERVO);
        this.liftMotor = hwMap.get(DcMotor.class, RobotHardware.LIFT_MOTOR);
    }

    public void openClaw() {
        clawServo.setPosition(0.8);
    }

    public void closeClaw() {
        clawServo.setPosition(0.2);
    }

    public void scoreHighBasket() {
        setLift(800);
        openClaw();
    }

    public void setLift(int targetTicks) {
        liftMotor.setTargetPosition(targetTicks);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.8);
    }

    public void stopLift() {
        liftMotor.setPower(0);
    }
}
