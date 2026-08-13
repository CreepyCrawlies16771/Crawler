package org.firstinspires.ftc.teamcode.Teamcode.Examples;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobotRegistry;

public class MyRobot extends CrawlerRobot {

    /**
     * Registers this robot with the Crawler tooling (Tuner, System Test, Smoke Test) so
     * it can build <i>your</i> robot without hard-coding this example class. Copy this
     * static block into your own robot class.
     */
    // Registers your robot class so Crawler's tools (Tuner, tests) can build it without
    // needing to know your class's name ahead of time. If you rename this class, update
    // "MyRobot::new" below to match.
    static {
        CrawlerRobotRegistry.setProvider(
                MyRobot::new,
                (hwMap, config) -> builder(hwMap).withConfig(config).build()
        );
    }

    // ===== REQUIRED: change these to match your Driver Hub configuration names =====
    public static final String FRONT_LEFT  = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT   = "bl";
    public static final String BACK_RIGHT  = "br";
    public static final String IMU         = "imu";

    // ===== REQUIRED only if using 3 dead wheels (see localizer step in docs) =====
    // These are MOTOR PORT names from your Driver Station config, not separate
    // encoder devices — dead wheels read through whichever motor port they're
    // plugged into on the REV Hub. Name the (possibly unused) motor port in the
    // config app, and use that exact name here.
    public static final String ENC_LEFT    = "enc_l";
    public static final String ENC_RIGHT   = "enc_r";
    public static final String ENC_CENTER  = "enc_c";

    // ===== Your robot's own subsystems — rename/add as needed for your season =====
    public static final String CLAW_SERVO = "claw";
    public static final String LIFT_MOTOR = "lift";

    // Match the physical mounting of your REV Hub
    public static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    public final Servo clawServo;
    public final DcMotor liftMotor;

    public MyRobot(HardwareMap hwMap) {
        super(builder(hwMap));
        this.clawServo = hwMap.get(Servo.class, CLAW_SERVO);
        this.liftMotor = hwMap.get(DcMotor.class, LIFT_MOTOR);
    }

    /** The full builder chain — names, localizer, and tuned values in one place. */
    public static CrawlerRobot.Builder builder(HardwareMap hwMap) {
        return new CrawlerRobot.Builder(hwMap)
                .frontLeft(FRONT_LEFT)
                .frontRight(FRONT_RIGHT)
                .backLeft(BACK_LEFT)
                .backRight(BACK_RIGHT)
                .imu(IMU)
                .imuOrientation(IMU_LOGO, IMU_USB)
                .motors()
                // ---- Localizer (pick one) ----
                .withThreeDeadWheels(ENC_LEFT, ENC_RIGHT, ENC_CENTER)
                // ---- Tuned values — paste the Crawler Tuner output here ----
                .setTrackWidth(13.0)                    // inches, left↔right odometry wheels
                .setCenterWheelOffset(3.5)              // inches, forward of center
                .wheelDiameter(1.37795)                 // inches (35 mm GoBILDA pod)
                .ticksPerRev(2000)
                .drivePid(0.05, 0.0, 0.0)               // per meter, drive PID
                .strafePid(0.05, 0.0, 0.0)              // per meter, strafe PID
                .steerPid(0.03, 0.0, 0.0)               // per degree, heading hold / turn
                .minPower(0.15)                         // friction deadband
                .pathDefaults(0.7, 0.4, 25.4)           // move, turn, follow distance (cm)
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .timeoutSecs(5.0)
                .turnReferenceRadians(Math.toRadians(30))
                .maxDriveSpeed(1.0);
    }

    /* ADD YOUR OWN ROBOT CODE HERE OR YOUR SEASON-SPECIFIC ROBOT ACTIONS! */
    public void openClaw()  { clawServo.setPosition(0.8); }
    public void closeClaw() { clawServo.setPosition(0.2); }

    public void setLift(int height) {
        liftMotor.setTargetPosition(height*100);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.5);
    }

    public void stopLift() {
        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMotor.setPower(0);
    }
    public void scoreHighBasket() {
        liftMotor.setTargetPosition(800);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.8);
    }
}
