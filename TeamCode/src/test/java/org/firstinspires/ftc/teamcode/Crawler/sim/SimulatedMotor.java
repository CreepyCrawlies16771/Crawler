package org.firstinspires.ftc.teamcode.Crawler.sim;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * Simulated drive motor for JVM integration tests.
 *
 * <p>Like a real encoder, position integrates the commanded power over <b>wall-clock
 * time</b> ({@code position += power * ticksPerSecondPerUnitPower * dt} on each read).
 * Reading the encoder hundreds of times a second does not inflate the distance — only
 * real elapsed time does — so a path followed in a JVM test advances at a realistic
 * pace. Everything else is a benign no-op.</p>
 */
public class SimulatedMotor implements DcMotorEx {

    /** Motor speed: ticks per second at full power (200 RPM × 2000 CPR ≈ 6667). */
    private final double ticksPerSecondPerUnitPower;
    // No-arg constructor avoids ConfigurationTypeManager, whose static init calls
    // Android APIs that are not mocked in JVM tests.
    private final MotorConfigurationType motorType = new MotorConfigurationType();

    private double position;
    private double power;
    private long lastReadNanos = System.nanoTime();
    private RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private Direction direction = Direction.FORWARD;
    private ZeroPowerBehavior zeroPowerBehavior = ZeroPowerBehavior.BRAKE;
    private int targetPosition;
    private int targetPositionTolerance;
    private boolean motorEnabled = true;

    public SimulatedMotor(double ticksPerSecondPerUnitPower) {
        this.ticksPerSecondPerUnitPower = ticksPerSecondPerUnitPower;
        motorType.setTicksPerRev(2000);
        motorType.setMaxRPM(200);
    }

    // ------------------------------------------------------------------ power
    @Override public void setPower(double power) { this.power = power; }
    @Override public double getPower() { return power; }

    // ------------------------------------------------------------- position
    @Override
    public int getCurrentPosition() {
        long now = System.nanoTime();
        double dt = (now - lastReadNanos) / 1e9;
        lastReadNanos = now;
        position += power * ticksPerSecondPerUnitPower * dt;
        return (int) Math.round(position);
    }

    @Override public void setTargetPosition(int targetPosition) { this.targetPosition = targetPosition; }
    @Override public int getTargetPosition() { return targetPosition; }
    @Override public boolean isBusy() {
        return Math.abs(getCurrentPosition() - targetPosition) > targetPositionTolerance;
    }

    @Override public RunMode getMode() { return mode; }
    @Override
    public void setMode(RunMode mode) {
        this.mode = mode;
        if (mode == RunMode.STOP_AND_RESET_ENCODER) {
            position = 0;
        }
    }

    @Override public ZeroPowerBehavior getZeroPowerBehavior() { return zeroPowerBehavior; }
    @Override public void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior) {
        this.zeroPowerBehavior = zeroPowerBehavior;
    }

    @Override public void setPowerFloat() { power = 0; }
    @Override public boolean getPowerFloat() { return false; }

    @Override public Direction getDirection() { return direction; }
    @Override public void setDirection(Direction direction) { this.direction = direction; }

    // ------------------------------------------------------------- DcMotorEx
    @Override public double getVelocity() { return power * ticksPerSecondPerUnitPower; }
    @Override public double getVelocity(AngleUnit unit) { return power * ticksPerSecondPerUnitPower; }
    @Override public void setVelocity(double angularRate) {}
    @Override public void setVelocity(double angularRate, AngleUnit unit) {}
    @Override public void setMotorEnable() { motorEnabled = true; }
    @Override public void setMotorDisable() { motorEnabled = false; }
    @Override public boolean isMotorEnabled() { return motorEnabled; }
    @Override public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {}
    @Override public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) {}
    @Override public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {}
    @Override public void setPositionPIDFCoefficients(double p) {}
    @Override public PIDCoefficients getPIDCoefficients(RunMode mode) {
        return new PIDCoefficients(1.0, 0.0, 0.0);
    }
    @Override public PIDFCoefficients getPIDFCoefficients(RunMode mode) {
        return new PIDFCoefficients(1.0, 0.0, 0.0, 0.0);
    }
    @Override public void setTargetPositionTolerance(int tolerance) { this.targetPositionTolerance = tolerance; }
    @Override public int getTargetPositionTolerance() { return targetPositionTolerance; }
    @Override public double getCurrent(CurrentUnit unit) { return 0; }
    @Override public double getCurrentAlert(CurrentUnit unit) { return 0; }
    @Override public void setCurrentAlert(double current, CurrentUnit unit) {}
    @Override public boolean isOverCurrent() { return false; }

    // ------------------------------------------------------- DcMotor extras
    @Override public MotorConfigurationType getMotorType() { return motorType; }
    @Override public void setMotorType(MotorConfigurationType motorType) {
        this.motorType.setTicksPerRev(motorType.getTicksPerRev());
        this.motorType.setMaxRPM(motorType.getMaxRPM());
    }
    @Override public DcMotorController getController() { return null; }
    @Override public int getPortNumber() { return 0; }

    // ---------------------------------------------------------- HardwareDevice
    @Override public Manufacturer getManufacturer() { return Manufacturer.Other; }
    @Override public String getDeviceName() { return "SimulatedMotor"; }
    @Override public String getConnectionInfo() { return "simulation"; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}
}
