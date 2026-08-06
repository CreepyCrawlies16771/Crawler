package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Your robot — everything lives in {@link #builder(HardwareMap)}: device names,
 * localizer, and every tuned number, in one builder chain.
 *
 * <p>Run <b>Crawler Tuner</b>, press Square (or finish Step 7), and paste the printed
 * builder lines into {@link #builder(HardwareMap)} — there is no separate config
 * function to edit.</p>
 */
public class MyRobot extends CrawlerRobot {

    // -----------------------------------------------------------------------
    // Device names — must match your Driver Hub configuration exactly.
    // -----------------------------------------------------------------------

    public static final String FRONT_LEFT  = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT   = "bl";
    public static final String BACK_RIGHT  = "br";
    public static final String IMU         = "imu";
    public static final String ENC_LEFT    = "enc_l";
    public static final String ENC_RIGHT   = "enc_r";
    public static final String ENC_CENTER  = "enc_c";

    public static final String CLAW_SERVO = "claw";
    public static final String LIFT_MOTOR = "lift";

    /** Match the physical mounting of your REV Hub. */
    public static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    // -----------------------------------------------------------------------
    // Season hardware
    // -----------------------------------------------------------------------

    public final Servo clawServo;
    public final DcMotor liftMotor;

    public MyRobot(HardwareMap hwMap) {
        super(builder(hwMap));
        this.clawServo = hwMap.get(Servo.class, CLAW_SERVO);
        this.liftMotor = hwMap.get(DcMotor.class, LIFT_MOTOR);
    }

    /**
     * The full builder chain — device names, localizer, and every tuned value in
     * one place. Paste the Crawler Tuner's Square output into the tuned section.
     */
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
                .pathDefaults(0.7, 0.4, 25.4)           // move, turn, follow distance cm
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .timeoutSecs(5.0)
                .maxDriveSpeed(1.0);
    }

    /**
     * Crawler Tuner support: rebuilds this robot from live tuning values.
     * Called by {@code CrawlerTuner} whenever a value changes in the FTC Dashboard —
     * you never need to edit or call this yourself.
     */
    public static CrawlerRobot buildTuned(HardwareMap hwMap, CrawlerRobot.Config c) {
        return builder(hwMap)
                .setTrackWidth(c.trackWidthIn)
                .setCenterWheelOffset(c.centerWheelOffsetIn)
                .wheelDiameter(c.wheelDiameterIn)
                .ticksPerRev(c.ticksPerRev)
                .drivePid(c.driveKp, c.driveKi, c.driveKd)
                .strafePid(c.strafeKp, c.strafeKi, c.strafeKd)
                .steerPid(c.steerP, c.steerI, c.steerD)
                .minPower(c.minPower)
                .pathDefaults(c.defaultMoveSpeed, c.defaultTurnSpeed, c.followDistanceCm)
                .arrivalThresholdCm(c.arrivalThresholdCm)
                .orbitThresholdCm(c.orbitThresholdCm)
                .timeoutSecs(c.timeoutSecs)
                .maxDriveSpeed(c.maxDriveSpeed)
                .build();
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
