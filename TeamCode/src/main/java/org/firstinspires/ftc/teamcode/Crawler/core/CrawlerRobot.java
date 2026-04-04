package org.firstinspires.ftc.teamcode.Crawler.core;

import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DevLocaliser;

public class CrawlerRobot {

    public final MotorEx frontRight;
    public final MotorEx frontLeft;
    public final MotorEx backRight;
    public final MotorEx backLeft;
    public final IMU imu;
    public final Localisation localisation;

    // Localiser data — only populated for the relevant localiser type
    public final MotorEx leftEncoder;
    public final MotorEx rightEncoder;
    public final MotorEx centerEncoder;
    public final double trackWidth;
    public final double centerWheelOffset;
    public final String pinpointDeviceName;

    // DevLocaliser instance
    public final CrawlerLocaliser devLocaliser;

    private CrawlerRobot(Builder builder) {
        this.frontRight  = new MotorEx(builder.hwMap, builder.frontRightName);
        this.frontLeft   = new MotorEx(builder.hwMap, builder.frontLeftName);
        this.backRight   = new MotorEx(builder.hwMap, builder.backRightName);
        this.backLeft    = new MotorEx(builder.hwMap, builder.backLeftName);
        this.imu         = builder.hwMap.get(IMU.class, builder.imuName);
        this.localisation = builder.localisation;

        this.leftEncoder       = builder.leftEncoder;
        this.rightEncoder      = builder.rightEncoder;
        this.centerEncoder     = builder.centerEncoder;
        this.trackWidth        = builder.trackWidth;
        this.centerWheelOffset = builder.centerWheelOffset;
        this.pinpointDeviceName = builder.pinpointDeviceName;

        //DevLocaliser for dev/testingaa
        this.devLocaliser = new DevLocaliser();
    }

    public enum Localisation {
        MotorEncoder,
        TwoDeadWheel,
        ThreeDeadWheel,
        Pinpoint
    }

    // -----------------------------------------------------------------------
    // Stage interfaces
    // -----------------------------------------------------------------------

    public interface IMotorStage {
        IMotorStage frontLeft(String name);
        IMotorStage frontRight(String name);
        IMotorStage backLeft(String name);
        IMotorStage backRight(String name);
        IMotorStage imu(String name);
        ILocaliserStage motors();
    }

    public interface ILocaliserStage {
        IReadyStage withMotorEncoders();
        IDeadWheelStage withThreeDeadWheels(String left, String right, String center);
        ITwoDeadWheelStage withTwoDeadWheels(String left, String right);
        IPinpointStage withPinpoint(String deviceName);
    }

    public interface ITwoDeadWheelStage {
        IReadyStage trackWidth(double trackWidth);
    }

    public interface IDeadWheelStage {
        IDeadWheelStage trackWidth(double trackWidth);
        IReadyStage centerWheelOffset(double offset);
    }

    public interface IPinpointStage {
        IReadyStage offsets(double xOffset, double yOffset);
    }

    public interface IReadyStage {
        CrawlerRobot build();
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static class Builder implements IMotorStage, ILocaliserStage, IDeadWheelStage, IPinpointStage, IReadyStage {

        private final HardwareMap hwMap;

        private String frontRightName;
        private String frontLeftName;
        private String backRightName;
        private String backLeftName;
        private String imuName = "imu";

        private Localisation localisation;

        private MotorEx leftEncoder;
        private MotorEx rightEncoder;
        private MotorEx centerEncoder;
        private double trackWidth;
        private double centerWheelOffset;
        private String pinpointDeviceName;

        public Builder(HardwareMap hwMap) {
            this.hwMap = hwMap;
        }

        @Override public IMotorStage frontLeft(String name)  { this.frontLeftName  = name; return this; }
        @Override public IMotorStage frontRight(String name) { this.frontRightName = name; return this; }
        @Override public IMotorStage backLeft(String name)   { this.backLeftName   = name; return this; }
        @Override public IMotorStage backRight(String name)  { this.backRightName  = name; return this; }
        @Override public IMotorStage imu(String name)        { this.imuName        = name; return this; }

        @Override
        public ILocaliserStage motors() {
            if (frontLeftName == null || frontRightName == null
                    || backLeftName == null || backRightName == null)
                throw new IllegalStateException("All four drive motor names must be set before calling motors().");
            return this;
        }

        @Override
        public IReadyStage withMotorEncoders() {
            this.localisation = Localisation.MotorEncoder;
            return this;
        }

        @Override
        public IDeadWheelStage withThreeDeadWheels(String left, String right, String center) {
            this.localisation  = Localisation.ThreeDeadWheel;
            this.leftEncoder   = new MotorEx(hwMap, left);
            this.rightEncoder  = new MotorEx(hwMap, right);
            this.centerEncoder = new MotorEx(hwMap, center);
            return this;
        }

        @Override
        public ITwoDeadWheelStage withTwoDeadWheels(String left, String center) {
            this.localisation = Localisation.TwoDeadWheel;
            return (ITwoDeadWheelStage) this;
        }

        @Override
        public IDeadWheelStage trackWidth(double trackWidth) {
            this.trackWidth = trackWidth;
            return this;
        }

        @Override
        public IReadyStage centerWheelOffset(double offset) {
            this.centerWheelOffset = offset;
            return this;
        }

        @Override
        public IPinpointStage withPinpoint(String deviceName) {
            this.localisation       = Localisation.Pinpoint;
            this.pinpointDeviceName = deviceName;
            return this;
        }

        @Override
        public IReadyStage offsets(double xOffset, double yOffset) {
            this.centerWheelOffset = xOffset;
            this.trackWidth        = yOffset;
            return this;
        }

        @Override
        public CrawlerRobot build() {
            return new CrawlerRobot(this);
        }
    }
}