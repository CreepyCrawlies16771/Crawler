package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
// Note: You must ensure this import correctly matches your project's GoBilda driver path
// import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DevLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.MotorEncoderLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.PinpointLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.ThreeDeadWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.TwoWheelLocaliser;

public class CrawlerRobot {

    public final MotorEx frontRight;
    public final MotorEx frontLeft;
    public final MotorEx backRight;
    public final MotorEx backLeft;
    public final IMU imu;
    public final Localisation localisation;
    public final CrawlerLocaliser localiser;

    final MotorEx leftEncoder;
    final MotorEx rightEncoder;
    final MotorEx centerEncoder;
    final double trackWidth;
    final double centerWheelOffset;
    final String pinpointDeviceName;

    protected CrawlerRobot(Builder builder) {
        this.frontLeft  = new MotorEx(builder.hwMap, builder.frontLeftName);
        this.frontRight = new MotorEx(builder.hwMap, builder.frontRightName);
        this.backLeft   = new MotorEx(builder.hwMap, builder.backLeftName);
        this.backRight  = new MotorEx(builder.hwMap, builder.backRightName);

        // Apply motor inversions
        if (builder.frontLeftInverted)  this.frontLeft.setInverted(true);
        if (builder.frontRightInverted) this.frontRight.setInverted(true);
        if (builder.backLeftInverted)   this.backLeft.setInverted(true);
        if (builder.backRightInverted)  this.backRight.setInverted(true);

        this.imu                = builder.hwMap.get(IMU.class, builder.imuName);
        this.localisation       = builder.localisation;
        this.leftEncoder        = builder.leftEncoder;
        this.rightEncoder       = builder.rightEncoder;
        this.centerEncoder      = builder.centerEncoder;
        this.trackWidth         = builder.trackWidth;
        this.centerWheelOffset  = builder.centerWheelOffset;
        this.pinpointDeviceName = builder.pinpointDeviceName;

        // Apply encoder inversions after encoders are assigned
        if (builder.leftEncoderInverted   && this.leftEncoder   != null)
            this.leftEncoder.setInverted(true);
        if (builder.rightEncoderInverted  && this.rightEncoder  != null)
            this.rightEncoder.setInverted(true);
        if (builder.centerEncoderInverted && this.centerEncoder != null)
            this.centerEncoder.setInverted(true);

        this.localiser = buildLocaliser(builder);
    }

    // -----------------------------------------------------------------------
    // Localiser factory
    // -----------------------------------------------------------------------

    private CrawlerLocaliser buildLocaliser(Builder builder) {
        if (builder.localisation == null) {
            return new DevLocaliser();
        }
        switch (builder.localisation) {
            case ThreeDeadWheel:
                return new ThreeDeadWheelLocaliser(
                        builder.leftEncoder,
                        builder.rightEncoder,
                        builder.centerEncoder,
                        builder.leftEncoderInverted,    // Fixed: Was incorrectly passing backLeftInverted
                        builder.rightEncoderInverted,   // Fixed: Was incorrectly passing backRightInverted
                        builder.centerEncoderInverted
                );
            case TwoDeadWheel:
                return new TwoWheelLocaliser(
                        builder.leftEncoder,
                        builder.centerEncoder,
                        builder.leftEncoderInverted,    // Fixed: Was incorrectly passing backLeftInverted
                        builder.centerEncoderInverted   // Fixed: Was incorrectly passing backRightInverted
                );
            case Pinpoint:
                return new PinpointLocaliser(
                        builder.hwMap,
                        builder.pinpointDeviceName,
                        builder.pinpointXOffset,
                        builder.pinpointYOffset,
                        builder.pinpointUnit,
                        builder.pinpointPod,
                        builder.pinpointXDir,
                        builder.pinpointYDir
                );
            case MotorEncoder:
                return new MotorEncoderLocaliser(
                        this.frontLeft,
                        this.frontRight,
                        this.backLeft,
                        this.backRight
                );
            case DevLocaliser:
            default:
                return new DevLocaliser();
        }
    }

    // -----------------------------------------------------------------------
    // Drive
    // -----------------------------------------------------------------------

    public void drive(double forward, double strafe, double rotate) {
        double fl = forward + strafe + rotate;
        double fr = forward - strafe - rotate;
        double bl = forward - strafe + rotate;
        double br = forward + strafe - rotate;
        double max = Math.max(1.0, Math.max(Math.max(Math.abs(fl), Math.abs(fr)),
                Math.max(Math.abs(bl), Math.abs(br))));
        frontLeft.set(fl / max);
        frontRight.set(fr / max);
        backLeft.set(bl / max);
        backRight.set(br / max);
    }

    public void driveFieldRelative(double forward, double strafe, double rotate) {
        double heading   = localiser.getPose().getHeading();
        double rotated_x = strafe  * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotated_y = strafe  * Math.sin(-heading) + forward * Math.cos(-heading);
        drive(rotated_y, rotated_x, rotate);
    }

    public void stop() {
        frontLeft.set(0); frontRight.set(0);
        backLeft.set(0);  backRight.set(0);
    }

    // -----------------------------------------------------------------------
    // Localisation enum
    // -----------------------------------------------------------------------

    public enum Localisation {
        MotorEncoder,
        TwoDeadWheel,
        ThreeDeadWheel,
        Pinpoint,
        DevLocaliser
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

        // Inversions — call after the motor name
        IMotorStage invertFrontLeft();
        IMotorStage invertFrontRight();
        IMotorStage invertBackLeft();
        IMotorStage invertBackRight();

        ILocaliserStage motors();
    }

    public interface ILocaliserStage {
        IReadyStage withMotorEncoders();
        IReadyStage withDevLocaliser();
        IThreeDeadWheelStage withThreeDeadWheels(String left, String right, String center);
        ITwoDeadWheelStage withTwoDeadWheels(String left, String center);
        IPinpointStage withPinpoint(String deviceName);
    }

    public interface IThreeDeadWheelStage {
        IThreeDeadWheelStage setTrackWidth(double trackWidth);
        IThreeDeadWheelStage invertLeftEncoder();
        IThreeDeadWheelStage invertRightEncoder();
        IThreeDeadWheelStage invertCenterEncoder();
        IReadyStage setCenterWheelOffset(double offset);
    }

    public interface ITwoDeadWheelStage {
        ITwoDeadWheelStage invertLeftEncoder();
        ITwoDeadWheelStage invertCenterEncoder();
        IReadyStage setTrackWidth(double trackWidth);
    }

    public interface IPinpointStage {
        // Replaced old offset method with full configuration
        IReadyStage setConfig(double xOffset, double yOffset,
                              DistanceUnit distanceUnit,
                              GoBildaPinpointDriver.GoBildaOdometryPods pod,
                              GoBildaPinpointDriver.EncoderDirection xDirection,
                              GoBildaPinpointDriver.EncoderDirection yDirection);
    }

    public interface IReadyStage {
        CrawlerRobot build();
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static class Builder implements
            IMotorStage, ILocaliserStage,
            IPinpointStage, IReadyStage {

        final HardwareMap hwMap;

        String frontRightName;
        String frontLeftName;
        String backRightName;
        String backLeftName;
        String imuName = "imu";

        // Motor inversions
        boolean frontLeftInverted   = false;
        boolean frontRightInverted  = false;
        boolean backLeftInverted    = false;
        boolean backRightInverted   = false;

        Localisation localisation;

        MotorEx leftEncoder;
        MotorEx rightEncoder;
        MotorEx centerEncoder;

        // Encoder inversions
        boolean leftEncoderInverted   = false;
        boolean rightEncoderInverted  = false;
        boolean centerEncoderInverted = false;

        double trackWidth;
        double centerWheelOffset;

        // Pinpoint specific fields
        String pinpointDeviceName;
        double pinpointXOffset;
        double pinpointYOffset;
        DistanceUnit pinpointUnit;
        GoBildaPinpointDriver.GoBildaOdometryPods pinpointPod;
        GoBildaPinpointDriver.EncoderDirection pinpointXDir;
        GoBildaPinpointDriver.EncoderDirection pinpointYDir;

        public Builder(HardwareMap hwMap) { this.hwMap = hwMap; }

        // Motor names
        @Override public IMotorStage frontLeft(String name)  { this.frontLeftName  = name; return this; }
        @Override public IMotorStage frontRight(String name) { this.frontRightName = name; return this; }
        @Override public IMotorStage backLeft(String name)   { this.backLeftName   = name; return this; }
        @Override public IMotorStage backRight(String name)  { this.backRightName  = name; return this; }
        @Override public IMotorStage imu(String name)        { this.imuName        = name; return this; }

        // Motor inversions
        @Override public IMotorStage invertFrontLeft()  { this.frontLeftInverted  = true; return this; }
        @Override public IMotorStage invertFrontRight() { this.frontRightInverted = true; return this; }
        @Override public IMotorStage invertBackLeft()   { this.backLeftInverted   = true; return this; }
        @Override public IMotorStage invertBackRight()  { this.backRightInverted  = true; return this; }

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
        public IReadyStage withDevLocaliser() {
            this.localisation = Localisation.DevLocaliser;
            return this;
        }

        @Override
        public IThreeDeadWheelStage withThreeDeadWheels(String left, String right, String center) {
            this.localisation  = Localisation.ThreeDeadWheel;
            this.leftEncoder   = new MotorEx(hwMap, left);
            this.rightEncoder  = new MotorEx(hwMap, right);
            this.centerEncoder = new MotorEx(hwMap, center);
            Builder self = this;
            return new IThreeDeadWheelStage() {
                @Override
                public IThreeDeadWheelStage setTrackWidth(double tw) {
                    self.trackWidth = tw;
                    return this;
                }
                @Override
                public IThreeDeadWheelStage invertLeftEncoder() {
                    self.leftEncoderInverted = true;
                    return this;
                }
                @Override
                public IThreeDeadWheelStage invertRightEncoder() {
                    self.rightEncoderInverted = true;
                    return this;
                }
                @Override
                public IThreeDeadWheelStage invertCenterEncoder() {
                    self.centerEncoderInverted = true;
                    return this;
                }
                @Override
                public IReadyStage setCenterWheelOffset(double offset) {
                    self.centerWheelOffset = offset;
                    return self;
                }
            };
        }

        @Override
        public ITwoDeadWheelStage withTwoDeadWheels(String left, String center) {
            this.localisation  = Localisation.TwoDeadWheel;
            this.leftEncoder   = new MotorEx(hwMap, left);
            this.centerEncoder = new MotorEx(hwMap, center);
            Builder self = this;
            return new ITwoDeadWheelStage() {
                @Override
                public ITwoDeadWheelStage invertLeftEncoder() {
                    self.leftEncoderInverted = true;
                    return this;
                }
                @Override
                public ITwoDeadWheelStage invertCenterEncoder() {
                    self.centerEncoderInverted = true;
                    return this;
                }
                @Override
                public IReadyStage setTrackWidth(double tw) {
                    self.trackWidth = tw;
                    return self;
                }
            };
        }

        @Override
        public IPinpointStage withPinpoint(String deviceName) {
            this.localisation       = Localisation.Pinpoint;
            this.pinpointDeviceName = deviceName;
            return this;
        }

        @Override
        public IReadyStage setConfig(double xOffset, double yOffset,
                                     DistanceUnit distanceUnit,
                                     GoBildaPinpointDriver.GoBildaOdometryPods pod,
                                     GoBildaPinpointDriver.EncoderDirection xDirection,
                                     GoBildaPinpointDriver.EncoderDirection yDirection) {
            this.pinpointXOffset = xOffset;
            this.pinpointYOffset = yOffset;
            this.pinpointUnit    = distanceUnit;
            this.pinpointPod     = pod;
            this.pinpointXDir    = xDirection;
            this.pinpointYDir    = yDirection;
            return this;
        }

        @Override
        public CrawlerRobot build() {
            return new CrawlerRobot(this);
        }
    }
}