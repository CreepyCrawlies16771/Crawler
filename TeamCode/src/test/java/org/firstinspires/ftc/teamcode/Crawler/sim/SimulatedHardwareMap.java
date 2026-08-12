package org.firstinspires.ftc.teamcode.Crawler.sim;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import java.util.HashMap;
import java.util.Map;

/**
 * HardwareMap stub that hands out simulated devices by name, so the real
 * {@code CrawlerRobot.Builder} construction path runs without any hardware.
 *
 * <p>FTCLib's {@code MotorEx} requests motors as {@code DcMotor.class}, so the map
 * answers both {@code DcMotor} and {@code DcMotorEx} lookups from the same pool.</p>
 */
public class SimulatedHardwareMap extends HardwareMap {

    private final Map<String, DcMotorEx> motors = new HashMap<>();
    private final IMU imu;

    public SimulatedHardwareMap(IMU imu) {
        super(null, null);   // no Android Context needed — get() is fully overridden
        this.imu = imu;
    }

    public void putMotor(String name, DcMotorEx motor) {
        motors.put(name, motor);
    }

    @Override
    public <T> T get(Class<? extends T> deviceClass, String deviceName) {
        if (deviceClass == DcMotor.class || deviceClass == DcMotorEx.class) {
            DcMotorEx motor = motors.get(deviceName);
            if (motor == null) {
                throw new IllegalArgumentException("no simulated motor named '" + deviceName + "'");
            }
            return deviceClass.cast(motor);
        }
        if (deviceClass == IMU.class) {
            return deviceClass.cast(imu);
        }
        throw new IllegalArgumentException(
                "no simulated device for " + deviceClass.getSimpleName());
    }
}
