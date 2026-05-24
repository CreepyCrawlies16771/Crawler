package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;
// Note: You must ensure this import correctly matches your project's GoBilda driver path
// import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DevLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.MotorEncoderLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.PinpointLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.ThreeDeadWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.TwoWheelLocaliser;

/**
 * Base class for Crawler FTC robotics platform.
 *
 * <p>This class manages the holonomic (mecanum/omni) drivetrain and localisation system.
 * Teams extend this class to add season-specific hardware (servos, motors, sensors) and
 * high-level action methods. The builder pattern is used for safe and flexible construction
 * with optional localisation backends (three dead wheels, two dead wheels, Pinpoint, motor encoders, or none).</p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
 *     .frontLeft("fl").frontRight("fr")
 *     .backLeft("bl").backRight("br")
 *     .motors()
 *     .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
 *     .setTrackWidth(13.0)
 *     .setCenterWheelOffset(3.5)
 *     .build();
 * }</pre>
 *
 * @see Localisation
 * @see CrawlerLocaliser
 */
public class CrawlerRobot {

    /**
     * All tunable constants for this robot. Set values in {@link Builder} from your
     * team class (e.g. {@code MyRobot}) — teams should not edit library source files.
     */
    public static class Config {
        public double trackWidthIn          = 13.0;
        public double centerWheelOffsetIn   = 3.5;
        public double wheelDiameterIn       = 1.37795;
        public double ticksPerRev           = 2000;

        public double driveKp    = 0.05;
        public double driveKi    = 0.0;
        public double driveKd    = 0.0;
        public double strafeKp   = 0.05;
        public double strafeKi   = 0.0;
        public double strafeKd   = 0.0;
        public double steerP     = 0.03;
        public double steerI     = 0.0;
        public double steerD     = 0.0;
        public double minPower   = 0.15;

        public double defaultMoveSpeed       = 0.7;
        public double defaultTurnSpeed       = 0.4;
        public double followDistanceCm       = 25.4;
        public double arrivalThresholdCm     = 5.0;
        public double orbitThresholdCm       = 25.4;
        public double slowMoveSpeed          = 0.3;
        public double slowTurnSpeed          = 0.2;
        public double slowFollowDistanceCm   = 12.7;
        public double slowDownTurnRadians    = 0.5;
        public double slowDownTurnAmount     = 0.5;

        public double timeoutSecs    = 5.0;
        public double maxDriveSpeed  = 1.0;
        /** Heading error scale for path following turn power (radians). */
        public double turnReferenceRadians = Math.toRadians(30);

        public double ticksPerMeter() {
            double metersPerRev = wheelDiameterIn * 0.0254 * Math.PI;
            return metersPerRev > 1e-9 ? ticksPerRev / metersPerRev : 2000.0;
        }

        public double ticksPerCm() {
            return ticksPerMeter() / 100.0;
        }
    }

    public final Config config;

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
        this.config = builder.config;
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

        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                builder.imuLogoFacing,
                builder.imuUsbFacing)));
    }

    /**
     * Resets the field pose estimate to the origin (for tests and auto start).
     */
    public void resetPose() {
        localiser.resetPose(new Pose2d());
        imu.resetYaw();
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
                        builder.leftEncoderInverted,
                        builder.rightEncoderInverted,
                        builder.centerEncoderInverted,
                        builder.config
                );
            case TwoDeadWheel:
                return new TwoWheelLocaliser(
                        builder.leftEncoder,
                        builder.centerEncoder,
                        builder.leftEncoderInverted,
                        builder.centerEncoderInverted,
                        builder.config
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
                        this.backRight,
                        builder.config
                );
            case DevLocaliser:
            default:
                return new DevLocaliser();
        }
    }

    // -----------------------------------------------------------------------
    // Drive
    // -----------------------------------------------------------------------

    /**
     * Applies raw motor powers using holonomic (mecanum) kinematics.
     *
     * <p>All parameters are in the robot's local frame: forward/backward along the
     * robot's heading, strafe left/right perpendicular to the heading, and rotate
     * counterclockwise (positive = turn left).</p>
     *
     * @param forward  forward movement power (-1.0 to 1.0)
     * @param strafe   strafe (left/right) movement power (-1.0 to 1.0)
     * @param rotate   rotation power (-1.0 to 1.0)
     */
    public void drive(double forward, double strafe, double rotate) {
        forward = CrawlerMath.clamp(forward, -config.maxDriveSpeed, config.maxDriveSpeed);
        strafe  = CrawlerMath.clamp(strafe,  -config.maxDriveSpeed, config.maxDriveSpeed);
        rotate  = CrawlerMath.clamp(rotate,  -config.maxDriveSpeed, config.maxDriveSpeed);

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

    /**
     * Applies holonomic movement in the field's fixed frame, not the robot's frame.
     *
     * <p>The heading angle is automatically applied to rotate the input powers from
     * field coordinates to robot coordinates. This is the primary method for autonomous
     * and field-oriented TeleOp movement.</p>
     *
     * @param forward  forward movement power in the field frame (-1.0 to 1.0)
     * @param strafe   strafe movement power in the field frame (-1.0 to 1.0)
     * @param rotate   rotation power (-1.0 to 1.0)
     */
    public void driveFieldRelative(double forward, double strafe, double rotate) {
        double heading   = localiser.getPose().getHeading();
        double rotated_x = strafe  * Math.cos(-heading) - forward * Math.sin(-heading);
        double rotated_y = strafe  * Math.sin(-heading) + forward * Math.cos(-heading);
        drive(rotated_y, rotated_x, rotate);
    }

    /**
     * Stops all drive motors immediately.
     */
    public void stop() {
        frontLeft.set(0); frontRight.set(0);
        backLeft.set(0);  backRight.set(0);
    }

    /**
     * Updates the localiser pose from hardware sensors.
     *
     * <p>Must be called regularly (every loop cycle) for accurate odometry tracking.
     * This method synchronously updates the robot's position estimate from
     * encoders, IMU, or other localization hardware.</p>
     */
    public void update() {
        localiser.update();
    }

    /**
     * Gets the current pose (position and heading) from the localiser.
     *
     * @return the robot's current {@code Pose2d} (x, y in centimeters, heading in radians)
     */
    public Pose2d getPose() {
        return localiser.getPose();
    }

    /**
     * Gets the current heading angle from the localiser.
     *
     * @return the robot's heading in radians
     */
    public double getHeading() {
        return localiser.getPose().getHeading();
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
        IMotorStage imuOrientation(RevHubOrientationOnRobot.LogoFacingDirection logo,
                                   RevHubOrientationOnRobot.UsbFacingDirection usb);

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
        IReadyStage wheelDiameter(double inches);
        IReadyStage ticksPerRev(double ticks);
        IReadyStage drivePid(double kp, double ki, double kd);
        IReadyStage strafePid(double kp, double ki, double kd);
        IReadyStage steerPid(double p, double i, double d);
        IReadyStage minPower(double minPower);
        IReadyStage pathDefaults(double moveSpeed, double turnSpeed, double followDistanceCm);
        IReadyStage arrivalThresholdCm(double cm);
        IReadyStage orbitThresholdCm(double cm);
        IReadyStage timeoutSecs(double seconds);
        IReadyStage maxDriveSpeed(double speed);
        Builder build();
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static class Builder implements
            IMotorStage, ILocaliserStage,
            IPinpointStage, IReadyStage {

        final HardwareMap hwMap;
        final Config config = new Config();

        String frontRightName;
        String frontLeftName;
        String backRightName;
        String backLeftName;
        String imuName;

        RevHubOrientationOnRobot.LogoFacingDirection imuLogoFacing =
                RevHubOrientationOnRobot.LogoFacingDirection.UP;
        RevHubOrientationOnRobot.UsbFacingDirection imuUsbFacing =
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

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
        @Override public IMotorStage imuOrientation(
                RevHubOrientationOnRobot.LogoFacingDirection logo,
                RevHubOrientationOnRobot.UsbFacingDirection usb) {
            imuLogoFacing = logo;
            imuUsbFacing = usb;
            return this;
        }

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
                    self.config.trackWidthIn = tw;
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
                    self.config.centerWheelOffsetIn = offset;
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
                    self.config.trackWidthIn = tw;
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

        @Override public IReadyStage wheelDiameter(double inches) {
            config.wheelDiameterIn = inches; return this;
        }
        @Override public IReadyStage ticksPerRev(double ticks) {
            config.ticksPerRev = ticks; return this;
        }
        @Override public IReadyStage drivePid(double kp, double ki, double kd) {
            config.driveKp = kp; config.driveKi = ki; config.driveKd = kd; return this;
        }
        @Override public IReadyStage strafePid(double kp, double ki, double kd) {
            config.strafeKp = kp; config.strafeKi = ki; config.strafeKd = kd; return this;
        }
        @Override public IReadyStage steerPid(double p, double i, double d) {
            config.steerP = p; config.steerI = i; config.steerD = d; return this;
        }
        @Override public IReadyStage minPower(double minPower) {
            config.minPower = minPower; return this;
        }
        @Override public IReadyStage pathDefaults(double moveSpeed, double turnSpeed, double followDistanceCm) {
            config.defaultMoveSpeed = moveSpeed;
            config.defaultTurnSpeed = turnSpeed;
            config.followDistanceCm = followDistanceCm;
            return this;
        }
        @Override public IReadyStage arrivalThresholdCm(double cm) {
            config.arrivalThresholdCm = cm; return this;
        }
        @Override public IReadyStage orbitThresholdCm(double cm) {
            config.orbitThresholdCm = cm; return this;
        }
        @Override public IReadyStage timeoutSecs(double seconds) {
            config.timeoutSecs = seconds; return this;
        }
        @Override public IReadyStage maxDriveSpeed(double speed) {
            config.maxDriveSpeed = speed; return this;
        }

        @Override
        public Builder build() {
            validate();
            return new CrawlerRobot(this);
        }

        private void validate() {
            if (frontLeftName == null || frontRightName == null
                    || backLeftName == null || backRightName == null) {
                throw new IllegalStateException(
                        "Set frontLeft, frontRight, backLeft, backRight before build().");
            }
            if (imuName == null) {
                throw new IllegalStateException("Set imu(\"name\") before build().");
            }
            if (localisation == Localisation.ThreeDeadWheel) {
                if (trackWidth <= 0 || centerWheelOffset < 0) {
                    throw new IllegalStateException(
                            "Three-wheel odometry needs setTrackWidth() and setCenterWheelOffset().");
                }
                if (config.wheelDiameterIn <= 0 || config.ticksPerRev <= 0) {
                    throw new IllegalStateException(
                            "Call wheelDiameter() and ticksPerRev() before build().");
                }
            }
            if (localisation == Localisation.TwoDeadWheel) {
                if (trackWidth <= 0) {
                    throw new IllegalStateException("Two-wheel odometry needs setTrackWidth().");
                }
                if (config.wheelDiameterIn <= 0 || config.ticksPerRev <= 0) {
                    throw new IllegalStateException(
                            "Call wheelDiameter() and ticksPerRev() before build().");
                }
            }
            if (localisation == Localisation.MotorEncoder
                    && (config.wheelDiameterIn <= 0 || config.ticksPerRev <= 0)) {
                throw new IllegalStateException(
                        "Motor encoders need wheelDiameter() and ticksPerRev().");
            }
            if (localisation == Localisation.Pinpoint && pinpointDeviceName == null) {
                throw new IllegalStateException("Pinpoint needs withPinpoint(deviceName).");
            }
        }
    }
}