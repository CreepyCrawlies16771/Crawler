# Crawler — Public API Reference & Build Specification

> This is the ground-truth reference for the **current** Crawler library, verified
> line-by-line against the source in
> `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/`. Anything in this
> document compiles against the real API. If a behavior is ambiguous in the source,
> it is flagged here rather than guessed.
>
> User-facing walkthroughs live in `docs/` (built to `docs-html/` with `npm run build`).

## What Crawler is

A source-included FTC pathing library. There is no JitPack artifact — teams get it by
copying the `Crawler` package into `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`
and adding the FTCLib + Dashboard dependencies.

**Dependencies (as configured in this repo):**

```groovy
// build.dependencies.gradle
implementation 'org.firstinspires.ftc:Inspection:11.1.0'    // … etc (FTC SDK 11.1.0)
implementation 'com.acmerobotics.dashboard:dashboard:0.5.1'

// TeamCode/build.gradle
implementation project(':FtcRobotController')
implementation 'org.ftclib.ftclib:core:2.1.1'
testImplementation 'junit:junit:4.13.2'
```

## Package layout

| Package | Contents |
|---|---|
| `…teamcode.Crawler.core.Robot` | `CrawlerRobot` (+ nested `Config`, `Localisation`, `Builder`, stage interfaces), `driveTrain` (legacy, unused) |
| `…teamcode.Crawler.core.Localizers` | `CrawlerLocaliser`, `ThreeDeadWheelLocaliser`, `TwoWheelLocaliser`, `PinpointLocaliser`, `MotorEncoderLocaliser`, `DevLocaliser` |
| `…teamcode.Crawler.core.utils` | `Waypoint`, `Point`, `Vector2d`, `CrawlerMath`, `UnitConverter` |
| `…teamcode.Crawler.FieldOrient` | `FOFollower`, `RobotMovement` |
| `…teamcode.Crawler.RobotOrient` | `RobotOrientedDrive`, `ROMovementEngine`, `HeadingTimeline`, `AnimationBuilder`, `IndexerRotation`, `Tuner` (deprecated) |
| `…teamcode.Crawler.Tuning` | `TuningSession`, `TuningConfig`, `TuningRobotFactory`, `TuningActiveCheck`, `TuningTelemetry` (+ package-private `TuningPidRunner`, `TuningDashboard`, `TuningUtil`, `MyRobotSnippet`) |
| `…teamcode.Crawler.Dashboard` | `DashboardFieldViewUtils` |
| `…teamcode.Crawler.Vision` | `AprilTagWebcam`, `Rotation` |
| `…teamcode.Crawler.annotations` | `Experimental` (+ processor) |

**Internal / do-not-reference in user code:** `TuningPidRunner`, `TuningDashboard`,
`TuningUtil`, `MyRobotSnippet` (all package-private), `DevLocaliser` (dev-only),
`driveTrain`, `Tuner`, `IndexerRotation`, `AnimationBuilder` (unwired).

---

## 1. `CrawlerRobot` (`core.Robot.CrawlerRobot`)

```java
public class CrawlerRobot {
    public final MotorEx frontLeft, frontRight, backLeft, backRight;   // com.arcrobotics.ftclib.hardware.motors.MotorEx
    public final IMU imu;                                              // com.qualcomm.robotcore.hardware.IMU
    public final Localisation localisation;
    public final CrawlerLocaliser localiser;
    public final Config config;

    protected CrawlerRobot(Builder builder);          // subclass via super(builder)

    public void resetPose();                          // zero pose + IMU yaw reset
    public void drive(double forward, double strafe, double rotate);       // robot frame, clamped to maxDriveSpeed
    public void driveFieldRelative(double forward, double strafe, double rotate);
    public void stop();
    public void update();                             // advance localiser — call every loop
    public Pose2d getPose();                          // FTCLib Pose2d: x/y CENTIMETERS, heading RADIANS
    public double getHeading();                       // radians
    public MotorEx getLeftEncoder();                  // null if the localizer doesn't use one
    public MotorEx getRightEncoder();
    public MotorEx getCenterEncoder();
}
```

### `CrawlerRobot.Config`

Every tunable value, with library defaults. Set via builder methods (below) — the
shipped `MyRobot` example keeps the numbers inline in `MyRobot.builder()`; the tuner
prints matching builder lines to paste there.

| Field | Default | Meaning |
|---|---|---|
| `trackWidthIn` | 13.0 | distance between the parallel odometry wheels (in) |
| `centerWheelOffsetIn` | 3.5 | center pod distance forward of robot center (in) |
| `wheelDiameterIn` | 1.37795 | odometry wheel diameter (in) |
| `ticksPerRev` | 2000 | encoder counts per wheel revolution |
| `driveKp / driveKi / driveKd` | 0.05 / 0.0 / 0.0 | drive PID (per meter of error) |
| `strafeKp / strafeKi / strafeKd` | 0.05 / 0.0 / 0.0 | strafe PID (per meter) |
| `steerP / steerI / steerD` | 0.03 / 0.0 / 0.0 | heading PID (per degree) |
| `minPower` | 0.15 | friction deadband |
| `defaultMoveSpeed` | 0.7 | cruise power between waypoints |
| `defaultTurnSpeed` | 0.4 | path turn-power scale |
| `followDistanceCm` | 25.4 | pure-pursuit look-ahead radius (cm) |
| `arrivalThresholdCm` | 5.0 | "arrived" distance (cm) |
| `orbitThresholdCm` | 25.4 | distance over which turn power fades |
| `slowMoveSpeed` | 0.3 | used by `Waypoint.slow(config)` |
| `slowTurnSpeed` | 0.2 | used by `Waypoint.slow(config)` |
| `slowFollowDistanceCm` | 12.7 | used by `Waypoint.slow(config)` |
| `slowDownTurnRadians` | 0.5 | **stored, unused by the follower** |
| `slowDownTurnAmount` | 0.5 | **stored, unused by the follower** |
| `timeoutSecs` | 5.0 | waypoint leg timeout |
| `maxDriveSpeed` | 1.0 | clamps every drive input |
| `turnReferenceRadians` | `toRadians(30)` | heading-error scale for path turn power |
| `ticksPerMeter()` | — | `ticksPerRev / (wheelDiameterIn × 0.0254 × π)` |
| `ticksPerCm()` | — | `ticksPerMeter() / 100` |

### The staged builder

`new CrawlerRobot.Builder(HardwareMap)` returns a builder typed through four stage
interfaces — `IMotorStage → ILocaliserStage → (localizer-specific) → IReadyStage → build()`.
Ordering is enforced by validation at `motors()` / `build()`, not by the compiler.

```java
new CrawlerRobot.Builder(hwMap)
        // IMotorStage
        .frontLeft("fl").frontRight("fr").backLeft("bl").backRight("br")
        .imu("imu")
        .imuOrientation(LogoFacingDirection.UP, UsbFacingDirection.FORWARD)
        .invertFrontLeft()            // optional per-motor inversions
        .motors()                     // throws if any motor name is missing
        // ILocaliserStage — pick exactly one
        .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
            .setTrackWidth(13.0)
            .invertLeftEncoder()      // optional
            .setCenterWheelOffset(3.5)
        // or .withTwoDeadWheels("l","c").setTrackWidth(13.0)
        // or .withMotorEncoders()
        // or .withPinpoint("odo").setConfig(xOff, yOff, unit, pod, xDir, yDir)
        // or .withDevLocaliser()
        // IReadyStage — all optional (Config defaults otherwise)
        .wheelDiameter(1.37795)
        .ticksPerRev(2000)
        .drivePid(0.05, 0.0, 0.0)
        .strafePid(0.05, 0.0, 0.0)
        .steerPid(0.03, 0.0, 0.0)
        .minPower(0.15)
        .pathDefaults(0.7, 0.4, 25.4)
        .arrivalThresholdCm(5.0)
        .orbitThresholdCm(25.4)
        .timeoutSecs(5.0)
        .maxDriveSpeed(1.0)
        .build();
```

**Pinpoint note:** `setConfig(double xOffset, double yOffset, DistanceUnit,
GoBildaPinpointDriver.GoBildaOdometryPods, EncoderDirection xDirection,
EncoderDirection yDirection)` — the Pinpoint device must be configured in the Robot
Controller app as a GoBILDA Pinpoint.

### `CrawlerRobot.Localisation`

`MotorEncoder`, `TwoDeadWheel`, `ThreeDeadWheel`, `Pinpoint`, `DevLocaliser`.

---

## 2. Localizers (`core.Localizers`)

```java
public interface CrawlerLocaliser {
    void update();
    Pose2d getPose();               // FTCLib Pose2d, cm + radians
    void resetPose(Pose2d pose);
}
```

All implementations are chosen through the builder — do not construct them directly.

---

## 3. Waypoint (`core.utils.Waypoint`)

```java
public class Waypoint {
    public final double x, y;             // centimeters, field frame
    public final double heading;          // stored; NOT used by FOFollower
    public final double moveSpeed, turnSpeed, followDistance;
    public final double slowDownTurnRadians, slowDownTurnAmount;  // stored; NOT used
    public final Runnable onReach;

    public static Builder at(double x, double y);                 // uses a default Config
    public static Builder at(double x, double y, CrawlerRobot.Config config); // preferred
    public Waypoint(Waypoint other);                              // copy constructor
    public Vector2d toVector();
    public Point toPoint();

    public static class Builder {
        public Builder(double x, double y, CrawlerRobot.Config config); // via at(...)
        public Builder heading(double heading);   // stored only
        public Builder speed(double speed);       // overrides moveSpeed
        public Builder turnSpeed(double turnSpeed);
        public Builder followDistance(double followDistance);
        public Builder slowDown(double radians, double amount); // stored only
        public Builder slow(CrawlerRobot.Config config);        // slowMoveSpeed/slowTurnSpeed/slowFollowDistanceCm
        public Builder onReach(Runnable action);
        public Waypoint build();
    }
}
```

> `at(x, y, null)` throws `IllegalArgumentException`. `at(x, y)` uses a fresh default
> `Config` — use the config overload so your tuned defaults apply.

### `UnitConverter` (`core.utils.UnitConverter`)

Crawler's field geometry is **centimeters** (waypoints, `Pose2d`, thresholds), the
builder's odometry sizes are **inches** (track width, wheel diameter, center offset),
and `drivePID`/`strafePID` take **meters**. `UnitConverter` bridges all of them:

```java
public final class UnitConverter {
    public static final double INCHES_PER_CM;   // 0.3937…
    public static final double CM_PER_INCH;     // 2.54

    public static double inToCm(double inches);
    public static double cmToIn(double cm);
    public static double mToCm(double meters);
    public static double cmToM(double cm);
    public static double mmToCm(double mm);
    public static double cmToMm(double cm);
    public static double ftToCm(double feet);
    public static double cmToFt(double cm);
}
```

---

## 4. Field-oriented movement (`FieldOrient`)

### `FOFollower`

```java
public class FOFollower {
    public FOFollower(CrawlerRobot robot, Telemetry telemetry, OpModeProxy proxy);
    public void follow(List<Waypoint> waypoints) throws InterruptedException;  // ≥ 2 waypoints
    public void follow(Waypoint... waypoints) throws InterruptedException;

    public interface OpModeProxy { boolean isActive(); }   // pass this::opModeIsActive
}
```

Behavior: blocks per leg; fires each waypoint's `onReach` once; aborts after
`config.timeoutSecs` with a warning; stops the robot at the end.

### `RobotMovement`

```java
public class RobotMovement {
    public RobotMovement(CrawlerRobot robot);
    public void follow(List<Waypoint> allPoints, double followAngle);          // one pursuit step
    public Waypoint getFollowPointPath(List<Waypoint> path, Point robotLocation, double followRadius);
    public void goToPosition(double x, double y, double moveSpeed, double preferredAngle, double turnSpeed);
    public double getWorldX();
    public double getWorldY();
    public double getWorldHeading();      // radians
}
```

`goToPosition` is the per-cycle command; it calls `robot.driveFieldRelative(...)`
with an orbit-faded turn power (`config.orbitThresholdCm`, `config.turnReferenceRadians`).

---

## 5. Robot-oriented movement (`RobotOrient`)

### `RobotOrientedDrive`

```java
public class RobotOrientedDrive {
    public RobotOrientedDrive(CrawlerRobot robot, ActiveCheck active, Telemetry telemetry);
    public void drivePID(double targetMeters, int targetHeadingDeg);   // heading in DEGREES
    public void strafePID(double targetMeters, int targetHeadingDeg);  // positive = right
    public void turnPID(int targetHeadingDeg);                         // absolute IMU heading
    public void setDebugSink(DebugSink sink);                          // per-loop observer

    public interface ActiveCheck { boolean isActive(); }
    public interface DebugSink { void onLoop(double error, double power, double p, double i, double d); }
}
```

All three block until done, timeout on `config.timeoutSecs`, and clamp power to ±0.7
with the `minPower` deadband at low error.

### `ROMovementEngine`

```java
public abstract class ROMovementEngine extends LinearOpMode {
    protected CrawlerRobot robot;
    protected RobotOrientedDrive movement;

    protected abstract CrawlerRobot buildRobot(HardwareMap hwMap);
    public abstract void runPath() throws InterruptedException;

    public void drivePID(double targetMeters, int targetAngle);   // wrappers → movement
    public void strafePID(double targetMeters, int targetAngle);
    public void turnPID(int targetAngle);
}
```

`runOpMode()` builds the robot, resets pose/IMU, waits for start, calls `runPath()`,
then stops the robot.

---

## 6. Tuning (`Tuning`)

Public surface:

```java
public final class TuningConfig {                        // @Config("Crawler Tuner")
    // public static double fields mirroring CrawlerRobot.Config (minus slow* / turnReferenceRadians):
    public static double trackWidthIn, centerWheelOffsetIn, wheelDiameterIn, ticksPerRev;
    public static double driveKp, driveKi, driveKd, strafeKp, strafeKi, strafeKd;
    public static double steerP, steerI, steerD, minPower;
    public static double moveSpeed, turnSpeed, followDistanceCm, arrivalThresholdCm,
                         orbitThresholdCm, timeoutSecs, maxDriveSpeed;
    // package-private static CrawlerRobot.Config toConfig();
}

public interface TuningRobotFactory { CrawlerRobot create(CrawlerRobot.Config config); }
public interface TuningActiveCheck { boolean isActive(); }

public final class TuningTelemetry {
    public TuningTelemetry(Telemetry driverStationTelemetry);   // wraps MultipleTelemetry (DS + Dashboard)
    public Telemetry get();
    public void clear();
    public void addLine(String line);
    public void addData(String caption, Object value);
    public void displayMovementDebug(Pose2d pose, double power, double error);
    public void update();
}

public final class TuningSession {
    public TuningSession(TuningRobotFactory factory, Telemetry driverTelemetry,
                         Gamepad gamepad, TuningActiveCheck active);
    public CrawlerRobot getRobot();
    public void loop() throws InterruptedException;    // call every OpMode loop
}
```

**Tuner steps (1–7):** Motors → Encoders → Track width → Center offset → PID
(Drive / Strafe / Turn / Min power) → Auto path (1 m square) → Finish (copy snippet).
Gamepad: RB run, D-pad ↑/↓ adjust, D-pad ←/→ term, Triangle cycle test, X back,
Circle next, Square snippet. Values persist in static fields until the app restarts.

---

## 7. Dashboard & Vision

```java
public class DashboardFieldViewUtils {
    public static final double ROBOT_RADIUS = 9.0;
    public enum FieldColor { RED, BLUE, GREEN, YELLOW, ORANGE, PURPLE, CYAN, MAGENTA, BLACK, WHITE; String getCode(); }
    public static void drawLine(TelemetryPacket packet, double startX, double startY,
                                double endX, double endY, FieldColor color);
    public static void drawPoint(TelemetryPacket packet, double x, double y, FieldColor color);
    public static void drawRobot(TelemetryPacket packet, double x, double y,
                                 double headingRads, FieldColor color);
}

public class AprilTagWebcam {
    public void init(HardwareMap hwMap, Telemetry telemetry);      // Webcam 1, 640x480
    public void update();                                          // refresh detections
    public List<AprilTagDetection> getDetectedTags();
    public double getAngle(AprilTagDetection apd, Rotation rotation);
    public AprilTagDetection getTagBySpecificId(int id);
    public void displayDetectionTelemetry(AprilTagDetection id);
    public void close();
}

public enum Rotation { ROLL, PITCH, YAW, RANGE, BEARING }
```

---

## Javadoc & style rules (as followed by the current source)

- Units in `@param` descriptions (in / cm / m / deg / rad).
- No `@author` tags; cross-reference with `@see`.
- Public/protected members get Javadoc; package-private/private get `//` comments.
- **No global `RobotConfig` statics** — all constants come from `CrawlerRobot.Config`
  via the builder, and live-tuning mirrors them in `TuningConfig`.
- `robot.drive(...)` / `driveFieldRelative(...)` are the only motor-power entry points
  in library logic.

---

## Deprecated / unwired API (flagged, not guessed)

- `RobotOrient/Tuner.java` — `@Deprecated`, throws `UnsupportedOperationException`.
- `core/Robot/driveTrain.java` — unused; unsafe cast of `MotorEx` → `DcMotor`.
- `RobotOrient/HeadingTimeline.java`, `AnimationBuilder.java`, `IndexerRotation.java`
  — real classes/tests, but **no engine consumes them**; `HeadingTimeline` semantics
  are clear from its unit tests, but intended consumers are gone.
- `Waypoint.heading` / `.slowDown(...)` — builder options whose values the follower
  never reads.
- `Vision/` package — functional but has no user-facing docs coverage yet.
