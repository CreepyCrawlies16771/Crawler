package org.firstinspires.ftc.teamcode.Crawler.core;

import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.Crawler.RobotConfig;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DeadWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DevLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.MotorEncoderLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.PinpointLocaliser;

public class CrawlerRobot {

    public final MotorEx frontRight;
    public final MotorEx frontLeft;
    public final MotorEx backRight;
    public final MotorEx backLeft;
    public final IMU imu;
    public final Localisation localisation;

    // The active localiser — use this everywhere in Crawler internals
    public final CrawlerLocaliser localiser;

    // Raw encoder references — kept for tuning OpModes that need direct access
    final MotorEx leftEncoder;
    final MotorEx rightEncoder;
    final MotorEx centerEncoder;
    final double trackWidth;
    final double centerWheelOffset;
    final String pinpointDeviceName;

    protected CrawlerRobot(Builder builder) {
        this.frontRight  = new MotorEx(builder.hwMap, builder.frontRightName);
        this.frontLeft   = new MotorEx(builder.hwMap, builder.frontLeftName);
        this.backRight   = new MotorEx(builder.hwMap, builder.backRightName);
        this.backLeft    = new MotorEx(builder.hwMap, builder.backLeftName);
        this.imu         = builder.hwMap.get(IMU.class, builder.imuName);
        this.localisation = builder.localisation;

        this.leftEncoder        = builder.leftEncoder;
        this.rightEncoder       = builder.rightEncoder;
        this.centerEncoder      = builder.centerEncoder;
        this.trackWidth         = builder.trackWidth;
        this.centerWheelOffset  = builder.centerWheelOffset;
        this.pinpointDeviceName = builder.pinpointDeviceName;

        // Build the correct localiser from the builder's chosen type
        this.localiser = buildLocaliser(builder);
    }

    // -----------------------------------------------------------------------
    // Localiser factory — reads builder state, constructs the right impl
    // -----------------------------------------------------------------------

    private CrawlerLocaliser buildLocaliser(Builder builder) {
        if (builder.localisation == null) {
            // No localiser configured — fall back to dev localiser
            // This lets tuning OpModes run without a real localiser
            return new DevLocaliser();
        }

        switch (builder.localisation) {
            case ThreeDeadWheel:
                return new DeadWheelLocaliser(
                        builder.leftEncoder,
                        builder.rightEncoder,
                        builder.centerEncoder,
                        builder.trackWidth,
                        builder.centerWheelOffset
                );

            case TwoDeadWheel:
                // Two dead wheel uses left + center only, no right encoder
                return new DeadWheelLocaliser(
                        builder.leftEncoder,
                        builder.leftEncoder, // mirrored — two wheel impl handles this
                        builder.centerEncoder,
                        builder.trackWidth,
                        0
                );

            case Pinpoint:
                return new PinpointLocaliser(
                        builder.hwMap,
                        builder.pinpointDeviceName,
                        builder.centerWheelOffset,
                        builder.trackWidth
                );

            case MotorEncoder:
            default:
                return new MotorEncoderLocaliser(
                        this.frontLeft,
                        this.frontRight,
                        this.backLeft,
                        RobotConfig.Odometry.TRACK_WIDTH,
                        RobotConfig.Odometry.CENTER_WHEEL_OFFSET
                );
        }
    }

    // -----------------------------------------------------------------------
    // Drive helpers
    // -----------------------------------------------------------------------

    public void drive(double forward, double strafe, double rotate) {
        double fl = forward + strafe + rotate;
        double fr = forward - strafe - rotate;
        double bl = forward - strafe + rotate;
        double br = forward + strafe - rotate;

        double max = Math.max(1.0,
                Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                        Math.max(Math.abs(bl), Math.abs(br))));

        frontLeft.set(fl  / max);
        frontRight.set(fr / max);
        backLeft.set(bl   / max);
        backRight.set(br  / max);
    }

    public void driveFieldRelative(double forward, double strafe, double rotate) {
        double heading   = localiser.getPose().getHeading();
        double rotated_x = strafe  * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotated_y = strafe  * Math.sin(-heading) + forward * Math.cos(-heading);
        drive(rotated_y, rotated_x, rotate);
    }

    public void stop() {
        frontLeft.set(0);
        frontRight.set(0);
        backLeft.set(0);
        backRight.set(0);
    }

    // -----------------------------------------------------------------------
    // Localisation enum
    // -----------------------------------------------------------------------

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

    public static class Builder implements
            IMotorStage, ILocaliserStage,
            IDeadWheelStage, ITwoDeadWheelStage,
            IPinpointStage, IReadyStage {

        final HardwareMap hwMap;

        String frontRightName;
        String frontLeftName;
        String backRightName;
        String backLeftName;
        String imuName = "imu";

        Localisation localisation;

        MotorEx leftEncoder;
        MotorEx rightEncoder;
        MotorEx centerEncoder;
        double trackWidth;
        double centerWheelOffset;
        String pinpointDeviceName;

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
                throw new IllegalStateException(
                        "All four drive motor names must be set before calling motors().");
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
            this.localisation  = Localisation.TwoDeadWheel;
            this.leftEncoder   = new MotorEx(hwMap, left);
            this.centerEncoder = new MotorEx(hwMap, center);
            Builder self = this;
            return new ITwoDeadWheelStage() {
                @Override
                public IReadyStage trackWidth(double trackWidth) {
                    self.trackWidth = trackWidth;
                    return self;
                }
            };
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