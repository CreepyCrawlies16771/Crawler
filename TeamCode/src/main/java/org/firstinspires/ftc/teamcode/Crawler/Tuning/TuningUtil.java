package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

final class TuningUtil {

    static double angleWrapDeg(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    static double imuYawDeg(IMU imu) {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    static void initImu(IMU imu) {
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));
    }

    static boolean pressed(boolean current, boolean[] previous) {
        boolean rising = current && !previous[0];
        previous[0] = current;
        return rising;
    }

    static boolean dpadUp(Gamepad pad, boolean[] edge) {
        return pressed(pad.dpad_up, edge);
    }

    static boolean dpadDown(Gamepad pad, boolean[] edge) {
        return pressed(pad.dpad_down, edge);
    }

    static boolean circle(Gamepad pad, boolean[] edge) {
        return pressed(pad.circle, edge);
    }

    static boolean square(Gamepad pad, boolean[] edge) {
        return pressed(pad.square, edge);
    }

    static boolean xButton(Gamepad pad, boolean[] edge) {
        return pressed(pad.x, edge);
    }

    static boolean rightBumper(Gamepad pad, boolean[] edge) {
        return pressed(pad.right_bumper, edge);
    }

    private TuningUtil() {}
}
