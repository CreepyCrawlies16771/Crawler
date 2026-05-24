package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

/**
 * Single place for all Robot Configuration device names and IMU mounting.
 * Edit only this file when hardware names change — then sync {@code TuningRobotConfig}
 * in the Crawler tuning package (same string values).
 */
public final class RobotHardware {

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

    /** Match physical REV hub / IMU mounting on your robot. */
    public static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    private RobotHardware() {}
}
