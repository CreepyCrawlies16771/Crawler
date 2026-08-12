package org.firstinspires.ftc.teamcode.Crawler.sim;

import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/**
 * Fake IMU for JVM integration tests: constant zero orientation, every call a no-op.
 * Heading in simulation comes from the {@link org.firstinspires.ftc.teamcode.Crawler.core.Localizers.SimulatedLocaliser},
 * not from this device.
 */
public class SimulatedImu implements IMU {

    private final YawPitchRollAngles angles =
            new YawPitchRollAngles(AngleUnit.RADIANS, 0, 0, 0, 0);

    @Override public boolean initialize(Parameters parameters) { return true; }
    @Override public void resetYaw() {}
    @Override public YawPitchRollAngles getRobotYawPitchRollAngles() { return angles; }
    @Override
    public Orientation getRobotOrientation(AxesReference reference, AxesOrder order, AngleUnit unit) {
        return new Orientation(reference, order, unit, 0, 0, 0, 0);
    }
    @Override public Quaternion getRobotOrientationAsQuaternion() { return new Quaternion(1, 0, 0, 0, 0); }
    @Override public AngularVelocity getRobotAngularVelocity(AngleUnit unit) {
        return new AngularVelocity(unit, 0, 0, 0, 0);
    }

    // ---------------------------------------------------------- HardwareDevice
    // IMU extends HardwareDevice, so these are real interface obligations.
    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "SimulatedImu"; }
    @Override public String getConnectionInfo() { return "simulation"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}
}
