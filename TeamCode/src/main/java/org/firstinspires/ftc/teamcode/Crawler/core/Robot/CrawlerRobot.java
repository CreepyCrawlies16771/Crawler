package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
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

    public final MotorEx leftEncoder;
    public final MotorEx rightEncoder;
    public final MotorEx centerEncoder;
    final double trackWidth;
    final double centerWheelOffset;
    final String pinpointDeviceName;
    public driveTrain driveTrain;
    final CrawlerRobot instance;


    protected CrawlerRobot(Builder builder) {
        this.instance = this;
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
        this.driveTrain = new driveTrain(instance);
    }

    // -----------------------------------------------------------------------
    // Localiser factory
    // -----------------------------------------------------------------------

    /**
     * Constructs the appropriate localizer based on the builder configuration.
     *
     * <p>Performs validation to ensure all required hardware is available before
     * instantiation. Throws IllegalStateException with a descriptive message if
     * configuration is invalid.</p>
     *
     * @param builder the robot builder with localiser configuration
     * @return the constructed CrawlerLocaliser instance
     * @throws IllegalStateException if required encoders are not configured
     */
    private CrawlerLocaliser buildLocaliser(Builder builder) {
        if (builder.localisation == null) {
            return new DevLocaliser();
        }

        switch (builder.localisation) {
            case ThreeDeadWheel:
                if (builder.leftEncoder == null || builder.rightEncoder == null || builder.centerEncoder == null) {
                    throw new IllegalStateException(
                            "Three dead-wheel localizer requires left, right, and center encoders. " +
                            "Call withThreeDeadWheels(leftName, rightName, centerName) and verify all encoder names exist in your hardware config."
                    );
                }
                return new ThreeDeadWheelLocaliser(
                        builder.leftEncoder,
                        builder.rightEncoder,
                        builder.centerEncoder,
                        builder.leftEncoderInverted,
                        builder.rightEncoderInverted,
                        builder.centerEncoderInverted
                );

            case TwoDeadWheel:
                if (builder.leftEncoder == null || builder.centerEncoder == null) {
                    throw new IllegalStateException(
                            "Two dead-wheel localizer requires left and center encoders. " +
                            "Call withTwoDeadWheels(leftName, centerName) and verify both encoder names exist in your hardware config."
                    );
                }
                return new TwoWheelLocaliser(
                        builder.leftEncoder,
                        builder.centerEncoder,
                        builder.leftEncoderInverted,
                        builder.centerEncoderInverted
                );

            case Pinpoint:
                if (builder.pinpointDeviceName == null) {
                    throw new IllegalStateException(
                            "Pinpoint localizer requires a device name. " +
                            "Call withPinpoint(deviceName) and verify the device exists in your hardware config."
                    );
                }
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

    public double getHeading() {
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
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