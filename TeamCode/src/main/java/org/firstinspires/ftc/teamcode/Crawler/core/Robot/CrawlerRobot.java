package org.firstinspires.ftc.teamcode.Crawler.core.Robot;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.CrawlerLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.DevLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.MotorEncoderLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.PinpointLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.SimulatedLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.ThreeDeadWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.TwoWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrors;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerPreflight;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.CrawlerMath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
     * All tunable constants for this robot.
     *
     * <p>There are <b>no preset values</b> — every constant is {@code 0} until you set it
     * in the {@link Builder} chain of your own robot class. {@link #validate()} rejects
     * any config that isn't fully specified, so an unconfigured value fails at build
     * time instead of silently using a library default.</p>
     */
    public static class Config {
        public double trackWidth          = 0;
        public double centerWheelOffset   = 0;
        public double wheelDiameter       = 0;
        public double ticksPerRev         = 0;

        public double driveKp    = 0.0;
        public double driveKi    = 0.0;
        public double driveKd    = 0.0;
        public double strafeKp   = 0.0;
        public double strafeKi   = 0.0;
        public double strafeKd   = 0.0;
        public double steerP     = 0.0;
        public double steerI     = 0.0;
        public double steerD     = 0.0;
        public double minPower   = 0.0;

        public double defaultMoveSpeed       = 0.0;
        public double defaultTurnSpeed       = 0.0;
        public double followDistanceCm       = 0.0;
        public double arrivalThresholdCm     = 0.0;
        public double orbitThresholdCm       = 0.0;
        public double slowMoveSpeed          = 0.0;
        public double slowTurnSpeed          = 0.0;
        public double slowFollowDistanceCm   = 0.0;
        public double slowDownTurnRadians    = 0.0;
        public double slowDownTurnAmount     = 0.0;

        public double timeoutSecs    = 0.0;
        public double maxDriveSpeed  = 0.0;
        /** Heading error scale for path following turn power (radians). */
        public double turnReferenceRadians = 0.0;

        /**
         * Encoder ticks per meter of wheel travel.
         *
         * <p>Requires {@link #wheelDiameter} and {@link #ticksPerRev} to be configured
         * (both are enforced by {@link #validate()}); returns {@code NaN} if they
         * haven't been set yet.</p>
         */
        public double ticksPerMeter() {
            return ticksPerRev / (wheelDiameter * 0.0254 * Math.PI);
        }

        public double ticksPerCm() {
            return ticksPerMeter() / 100.0;
        }

        // -------------------------------------------------------------------
        // Fluent configuration API
        // -------------------------------------------------------------------

        /** Sets the odometry wheel diameter in inches. */
        public Config setWheelDiameter(double value)      { this.wheelDiameter = value; return this; }

        /** Sets the odometry encoder ticks per wheel revolution. */
        public Config setTicksPerRev(double value)          { this.ticksPerRev = value; return this; }

        /** Sets the track width (left↔right odometry wheel distance) in inches. */
        public Config setTrackWidth(double value)         { this.trackWidth = value; return this; }

        /** Sets the center-pod offset from robot center in inches (signed — negative is behind center). */
        public Config setCenterWheelOffset(double value)  { this.centerWheelOffset = value; return this; }

        /** Sets the per-leg path-following timeout in seconds. */
        public Config setTimeoutSecs(double value)          { this.timeoutSecs = value; return this; }

        /** Sets the arrival threshold ("close enough") distance in centimeters. */
        public Config setArrivalThresholdCm(double value)   { this.arrivalThresholdCm = value; return this; }

        /** Sets the orbit (turn-fade) threshold distance in centimeters. */
        public Config setOrbitThresholdCm(double value)     { this.orbitThresholdCm = value; return this; }

        /** Sets the heading-error scale for path-following turn power, in radians. */
        public Config setTurnReferenceRadians(double value) { this.turnReferenceRadians = value; return this; }

        /**
         * Validates every tunable value in this config, throwing an
         * {@link IllegalArgumentException} that names each invalid field.
         *
         * <p>Rejects non-finite values (NaN/Infinity), non-positive physical dimensions,
         * thresholds and timeouts, and out-of-range speeds. Signed geometric offsets
         * such as {@link #centerWheelOffset} may legitimately be negative, so they are
         * only required to be finite.</p>
         */
        public void validate() {
            List<String> bad = new ArrayList<>();
            requireFinitePositive(bad, "wheelDiameter", wheelDiameter);
            requireFinitePositive(bad, "ticksPerRev", ticksPerRev);
            requireFinitePositive(bad, "trackWidth", trackWidth);
            if (!Double.isFinite(centerWheelOffset)) {
                bad.add("centerWheelOffsetIn must be finite, got " + centerWheelOffset);
            }
            requireFinitePositive(bad, "timeoutSecs", timeoutSecs);
            requireFinitePositive(bad, "arrivalThresholdCm", arrivalThresholdCm);
            requireFinitePositive(bad, "orbitThresholdCm", orbitThresholdCm);
            requireFinitePositive(bad, "turnReferenceRadians", turnReferenceRadians);
            requireFinitePositive(bad, "followDistanceCm", followDistanceCm);
            requireInRange(bad, "defaultMoveSpeed", defaultMoveSpeed, true);
            requireInRange(bad, "defaultTurnSpeed", defaultTurnSpeed, true);
            requireInRange(bad, "maxDriveSpeed", maxDriveSpeed, true);
            requireInRange(bad, "minPower", minPower, false);
            // Slow-down values are optional (0 = not used), so they only need to be sane.
            requireInRange(bad, "slowMoveSpeed", slowMoveSpeed, false);
            requireInRange(bad, "slowTurnSpeed", slowTurnSpeed, false);
            requireFiniteNonNegative(bad, "slowFollowDistanceCm", slowFollowDistanceCm);
            requireFinite(bad, "slowDownTurnRadians", slowDownTurnRadians);
            requireFinite(bad, "slowDownTurnAmount", slowDownTurnAmount);
            requireFinite(bad, "driveKp", driveKp);
            requireFinite(bad, "driveKi", driveKi);
            requireFinite(bad, "driveKd", driveKd);
            requireFinite(bad, "strafeKp", strafeKp);
            requireFinite(bad, "strafeKi", strafeKi);
            requireFinite(bad, "strafeKd", strafeKd);
            requireFinite(bad, "steerP", steerP);
            requireFinite(bad, "steerI", steerI);
            requireFinite(bad, "steerD", steerD);
            if (!bad.isEmpty()) {
                throw new IllegalArgumentException(
                        "Invalid CrawlerRobot.Config: " + String.join("; ", bad));
            }
        }

        private static void requireFinitePositive(List<String> bad, String name, double value) {
            if (!Double.isFinite(value) || value <= 0) {
                bad.add(name + " must be finite and > 0, got " + value);
            }
        }

        private static void requireFinite(List<String> bad, String name, double value) {
            if (!Double.isFinite(value)) {
                bad.add(name + " must be finite, got " + value);
            }
        }

        private static void requireFiniteNonNegative(List<String> bad, String name, double value) {
            if (!Double.isFinite(value) || value < 0) {
                bad.add(name + " must be finite and >= 0, got " + value);
            }
        }

        /** Speeds use the exclusive {@code (0, 1]} range; {@code minPower} uses {@code [0, 1]}. */
        private static void requireInRange(List<String> bad, String name, double value, boolean exclusive) {
            boolean ok = exclusive ? (value > 0 && value <= 1) : (value >= 0 && value <= 1);
            if (!ok) {
                bad.add(name + " must be " + (exclusive ? "in (0, 1]" : "in [0, 1]") + ", got " + value);
            }
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

    private boolean poseInitialized;

    protected CrawlerRobot(Builder builder) {
        this.config = builder.config != null ? builder.config : new Config();
        this.config.validate();
        this.frontLeft  = motor(builder.hwMap, builder.frontLeftName);
        this.frontRight = motor(builder.hwMap, builder.frontRightName);
        this.backLeft   = motor(builder.hwMap, builder.backLeftName);
        this.backRight  = motor(builder.hwMap, builder.backRightName);

        // Apply motor inversions
        if (builder.frontLeftInverted)  this.frontLeft.setInverted(true);
        if (builder.frontRightInverted) this.frontRight.setInverted(true);
        if (builder.backLeftInverted)   this.backLeft.setInverted(true);
        if (builder.backRightInverted)  this.backRight.setInverted(true);

        this.imu                = device(builder.hwMap, IMU.class, builder.imuName);
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
     * Sets the robot's pose estimate. x/y are in <b>centimeters</b>, heading in
     * <b>radians</b>. Rejects {@code null}.
     *
     * <p>Delegates to the localiser's own reset/set mechanism, so the pose stays
     * consistent with whatever odometry backend is in use.</p>
     */
    public void setPose(Pose2d pose) {
        if (pose == null) {
            throw new IllegalArgumentException(
                    "pose cannot be null — pass e.g. new Pose2d(xCm, yCm, new Rotation2d(headingRad))");
        }
        localiser.resetPose(pose);
        poseInitialized = true;
    }

    /**
     * Resets the field pose estimate to the origin {@code (0, 0, 0)} and re-zeroes the
     * IMU yaw (for tests and auto start).
     */
    public void resetPose() {
        setPose(new Pose2d());
        imu.resetYaw();
    }

    /**
     * Sets a custom starting pose before a path: x/y in <b>centimeters</b>, heading in
     * <b>radians</b>. Re-zeroes the IMU yaw. Call this (or {@link #resetPose()}) once in
     * {@code init()} / after {@code waitForStart()} — {@link CrawlerError#SETUP_NO_START_POSE}
     * fires if a path is followed without it.
     */
    public void startPose(double xCm, double yCm, double headingRad) {
        localiser.resetPose(new Pose2d(xCm, yCm, new Rotation2d(headingRad)));
        imu.resetYaw();
        poseInitialized = true;
    }

    /** {@code true} once {@link #resetPose()} or {@link #startPose(double, double, double)} has been called. */
    public boolean isPoseInitialized() {
        return poseInitialized;
    }

    // -----------------------------------------------------------------------
    // Device construction (with CRWL-104 wrapping)
    // -----------------------------------------------------------------------

    private static MotorEx motor(HardwareMap hwMap, String name) {
        try {
            return new MotorEx(hwMap, name);
        } catch (RuntimeException e) {
            CrawlerErrors.throwError(CrawlerError.SETUP_DEVICE_NOT_FOUND, name);
            throw new AssertionError("unreachable");
        }
    }

    private static <T> T device(HardwareMap hwMap, Class<T> type, String name) {
        try {
            return hwMap.get(type, name);
        } catch (RuntimeException e) {
            CrawlerErrors.throwError(CrawlerError.SETUP_DEVICE_NOT_FOUND, name);
            throw new AssertionError("unreachable");
        }
    }

    // -----------------------------------------------------------------------
    // Localiser factory
    // -----------------------------------------------------------------------

    private CrawlerLocaliser buildLocaliser(Builder builder) {
        if (builder.localisation == null) {
            throw new IllegalStateException(
                    "No localisation method configured. Call withMotorEncoders(), withTwoDeadWheels(), "
                            + "withThreeDeadWheels(), withPinpoint(), or withDevLocaliser().");
        }
        switch (builder.localisation) {
            case ThreeDeadWheel:
                return new ThreeDeadWheelLocaliser(
                        builder.leftEncoder,
                        builder.rightEncoder,
                        builder.centerEncoder,
                        builder.config
                );
            case TwoDeadWheel:
                return new TwoWheelLocaliser(
                        builder.leftEncoder,
                        builder.centerEncoder,
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
            case Simulated:
                return new SimulatedLocaliser(
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
        if (!Double.isFinite(forward)) {
            CrawlerErrors.throwError(CrawlerError.RUNTIME_NON_FINITE_POWER, forward);
        }
        if (!Double.isFinite(strafe)) {
            CrawlerErrors.throwError(CrawlerError.RUNTIME_NON_FINITE_POWER, strafe);
        }
        if (!Double.isFinite(rotate)) {
            CrawlerErrors.throwError(CrawlerError.RUNTIME_NON_FINITE_POWER, rotate);
        }

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
    // Odometry encoder access (used by the Crawler Tuner's encoder step)
    // -----------------------------------------------------------------------

    /** The left odometry encoder, or {@code null} if the localizer doesn't use one. */
    public MotorEx getLeftEncoder() { return leftEncoder; }

    /** The right odometry encoder, or {@code null} if the localizer doesn't use one. */
    public MotorEx getRightEncoder() { return rightEncoder; }

    /** The center odometry encoder, or {@code null} if the localizer doesn't use one. */
    public MotorEx getCenterEncoder() { return centerEncoder; }

    // -----------------------------------------------------------------------
    // Localisation enum
    // -----------------------------------------------------------------------

    public enum Localisation {
        MotorEncoder,
        TwoDeadWheel,
        ThreeDeadWheel,
        Pinpoint,
        Simulated,
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
        /** Simulated mecanum odometry for PC testing — no hardware required. */
        IReadyStage withSimulatedLocaliser();
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
        IReadyStage setTrackWidth(double trackWidth);
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
        /** Heading-error scale for path-following turn power, in radians (e.g. {@code Math.toRadians(30)}). */
        IReadyStage turnReferenceRadians(double radians);
        IReadyStage maxDriveSpeed(double speed);
        /** Optional per-waypoint slow-down speeds (move, turn) and follow distance (cm). 0 = not used. */
        IReadyStage slowSpeeds(double moveSpeed, double turnSpeed, double followDistanceCm);
        /** Optional turn slow-down trigger (radians of heading error) and amount. 0 = not used. */
        IReadyStage slowDownTurn(double radians, double amount);
        IReadyStage withConfig(Config config);
        CrawlerRobot build();
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static class Builder implements
            IMotorStage, ILocaliserStage, IThreeDeadWheelStage,
            ITwoDeadWheelStage, IPinpointStage, IReadyStage {

        final HardwareMap hwMap;
        Config config = new Config();

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

        // Motor names — covariant Builder returns keep the whole chain typed as Builder
        @Override public Builder frontLeft(String name)  { this.frontLeftName  = name; return this; }
        @Override public Builder frontRight(String name) { this.frontRightName = name; return this; }
        @Override public Builder backLeft(String name)   { this.backLeftName   = name; return this; }
        @Override public Builder backRight(String name)  { this.backRightName  = name; return this; }
        @Override public Builder imu(String name)        { this.imuName        = name; return this; }
        @Override public Builder imuOrientation(
                RevHubOrientationOnRobot.LogoFacingDirection logo,
                RevHubOrientationOnRobot.UsbFacingDirection usb) {
            imuLogoFacing = logo;
            imuUsbFacing = usb;
            return this;
        }

        // Motor inversions
        @Override public Builder invertFrontLeft()  { this.frontLeftInverted  = true; return this; }
        @Override public Builder invertFrontRight() { this.frontRightInverted = true; return this; }
        @Override public Builder invertBackLeft()   { this.backLeftInverted   = true; return this; }
        @Override public Builder invertBackRight()  { this.backRightInverted  = true; return this; }

        @Override
        public Builder motors() {
            if (frontLeftName == null || frontRightName == null
                    || backLeftName == null || backRightName == null) {
                CrawlerErrors.throwError(CrawlerError.SETUP_MOTOR_NAMES_MISSING);
            }
            return this;
        }

        @Override
        public Builder withMotorEncoders() {
            this.localisation = Localisation.MotorEncoder;
            return this;
        }

        @Override
        public Builder withSimulatedLocaliser() {
            this.localisation = Localisation.Simulated;
            return this;
        }

        @Override
        public Builder withDevLocaliser() {
            this.localisation = Localisation.DevLocaliser;
            return this;
        }

        @Override
        public Builder withThreeDeadWheels(String left, String right, String center) {
            this.localisation  = Localisation.ThreeDeadWheel;
            this.leftEncoder   = motor(hwMap, left);
            this.rightEncoder  = motor(hwMap, right);
            this.centerEncoder = motor(hwMap, center);
            return this;
        }

        @Override
        public Builder withTwoDeadWheels(String left, String center) {
            this.localisation  = Localisation.TwoDeadWheel;
            this.leftEncoder   = motor(hwMap, left);
            this.centerEncoder = motor(hwMap, center);
            return this;
        }

        @Override
        public Builder withPinpoint(String deviceName) {
            this.localisation       = Localisation.Pinpoint;
            this.pinpointDeviceName = deviceName;
            return this;
        }

        @Override
        public Builder setConfig(double xOffset, double yOffset,
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

        // --- IThreeDeadWheelStage / ITwoDeadWheelStage --------------------
        @Override
        public Builder setTrackWidth(double trackWidth) {
            this.trackWidth = trackWidth;
            this.config.trackWidth = trackWidth;
            return this;
        }

        @Override
        public Builder setCenterWheelOffset(double offset) {
            this.centerWheelOffset = offset;
            this.config.centerWheelOffset = offset;
            return this;
        }

        @Override public Builder invertLeftEncoder()   { this.leftEncoderInverted   = true; return this; }
        @Override public Builder invertRightEncoder()  { this.rightEncoderInverted  = true; return this; }
        @Override public Builder invertCenterEncoder() { this.centerEncoderInverted = true; return this; }

        @Override public Builder wheelDiameter(double inches) {
            config.wheelDiameter = inches; return this;
        }
        @Override public Builder ticksPerRev(double ticks) {
            config.ticksPerRev = ticks; return this;
        }
        @Override public Builder drivePid(double kp, double ki, double kd) {
            config.driveKp = kp; config.driveKi = ki; config.driveKd = kd; return this;
        }
        @Override public Builder strafePid(double kp, double ki, double kd) {
            config.strafeKp = kp; config.strafeKi = ki; config.strafeKd = kd; return this;
        }
        @Override public Builder steerPid(double p, double i, double d) {
            config.steerP = p; config.steerI = i; config.steerD = d; return this;
        }
        @Override public Builder minPower(double minPower) {
            config.minPower = minPower; return this;
        }
        @Override public Builder pathDefaults(double moveSpeed, double turnSpeed, double followDistanceCm) {
            config.defaultMoveSpeed = moveSpeed;
            config.defaultTurnSpeed = turnSpeed;
            config.followDistanceCm = followDistanceCm;
            return this;
        }
        @Override public Builder arrivalThresholdCm(double cm) {
            config.arrivalThresholdCm = cm; return this;
        }
        @Override public Builder orbitThresholdCm(double cm) {
            config.orbitThresholdCm = cm; return this;
        }
        @Override public Builder timeoutSecs(double seconds) {
            config.timeoutSecs = seconds; return this;
        }
        @Override public Builder turnReferenceRadians(double radians) {
            config.turnReferenceRadians = radians; return this;
        }
        @Override public Builder maxDriveSpeed(double speed) {
            config.maxDriveSpeed = speed; return this;
        }
        @Override public Builder slowSpeeds(double moveSpeed, double turnSpeed, double followDistanceCm) {
            config.slowMoveSpeed       = moveSpeed;
            config.slowTurnSpeed       = turnSpeed;
            config.slowFollowDistanceCm = followDistanceCm;
            return this;
        }
        @Override public Builder slowDownTurn(double radians, double amount) {
            config.slowDownTurnRadians = radians;
            config.slowDownTurnAmount  = amount;
            return this;
        }

        /**
         * Replaces the default {@link Config} with your own. The supplied config is the
         * single authoritative source for the built robot — values tuned earlier in the
         * chain (e.g. {@code setTrackWidth}) are carried by the config you pass, so pass
         * one that already contains them.
         */
        @Override
        public IReadyStage withConfig(Config config) {
            this.config = Objects.requireNonNull(config, "config cannot be null");
            // Keep the builder-level geometry fields in sync — the robot snapshots them
            // directly, while localizers read them from the config.
            this.trackWidth        = config.trackWidth;
            this.centerWheelOffset = config.centerWheelOffset;
            return this;
        }

        @Override
        public CrawlerRobot build() {
            validate();
            CrawlerPreflight.checkConfigOrThrow(config);
            config.validate();
            return new CrawlerRobot(this);
        }

        void validate() {
            if (frontLeftName == null || frontRightName == null
                    || backLeftName == null || backRightName == null) {
                CrawlerErrors.throwError(CrawlerError.SETUP_MOTOR_NAMES_MISSING);
            }
            if (imuName == null) {
                CrawlerErrors.throwError(CrawlerError.SETUP_IMU_NAME_MISSING);
            }
            if (localisation == Localisation.ThreeDeadWheel) {
                // Geometry comes from the (possibly user-supplied) config; the center
                // pod offset is a signed distance, so only track width is range-checked
                // here — non-finite offsets are rejected by config.validate().
                if (config.trackWidth <= 0) {
                    CrawlerErrors.throwError(
                            CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "three-dead-wheel");
                }
                if (config.wheelDiameter <= 0 || config.ticksPerRev <= 0) {
                    CrawlerErrors.throwError(
                            CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "three-dead-wheel");
                }
            }
            if (localisation == Localisation.TwoDeadWheel) {
                if (config.trackWidth <= 0) {
                    CrawlerErrors.throwError(
                            CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "two-dead-wheel");
                }
                if (config.wheelDiameter <= 0 || config.ticksPerRev <= 0) {
                    CrawlerErrors.throwError(
                            CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "two-dead-wheel");
                }
            }
            if (localisation == Localisation.MotorEncoder
                    && (config.wheelDiameter <= 0 || config.ticksPerRev <= 0)) {
                CrawlerErrors.throwError(
                        CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "motor-encoder");
            }
            if (localisation == Localisation.Simulated
                    && (config.wheelDiameter <= 0 || config.ticksPerRev <= 0)) {
                CrawlerErrors.throwError(
                        CrawlerError.SETUP_LOCALIZER_CONFIG_MISSING, "simulated");
            }
            if (localisation == Localisation.Pinpoint && pinpointDeviceName == null) {
                CrawlerErrors.throwError(CrawlerError.SETUP_PINPOINT_CONFIG_MISSING);
            }

            if (localisation == null) {
                throw new IllegalStateException(
                        "No localisation method configured. Call withMotorEncoders(), withTwoDeadWheels(), "
                                + "withThreeDeadWheels(), withPinpoint(), or withDevLocaliser().");
            }
        }
    }
}