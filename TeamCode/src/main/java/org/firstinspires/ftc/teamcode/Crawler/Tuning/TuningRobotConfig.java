package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;

/**
 * Hardware names for {@link CrawlerTuner}.
 * Keep in sync with your team's {@code RobotHardware} class.
 */
final class TuningRobotConfig {
    static final String FRONT_LEFT  = "fl";
    static final String FRONT_RIGHT = "fr";
    static final String BACK_LEFT   = "bl";
    static final String BACK_RIGHT  = "br";
    static final String IMU         = "imu";
    static final String ENC_LEFT    = "enc_l";
    static final String ENC_RIGHT   = "enc_r";
    static final String ENC_CENTER  = "enc_c";

    static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO_FACING =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB_FACING =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    private TuningRobotConfig() {}
}
