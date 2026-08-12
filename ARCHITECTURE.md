# Crawler — Architecture & Integration Guide

> This document describes the **current** library as it ships. Every code example
> below is verified against the real public API in
> `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/`. For the
> user-facing walkthrough see the `docs/` site; for the full public-API inventory
> see `BUILD_SPECIFICATION.md`.

## Table of Contents

1. [Project Overview](#project-overview)
2. [Project Structure](#project-structure)
3. [Architecture Layers](#architecture-layers)
4. [Configuration System](#configuration-system)
5. [Data Flow](#data-flow)
6. [Development Workflow](#development-workflow)
7. [Integration Examples](#integration-examples)

---

## Project Overview

**Crawler** is an FTC (FIRST Tech Challenge) pathing library built on the FTC SDK
(DECODE 2025-2026) and FTCLib. Its pitch is simplicity: a rookie team drives a real
path in under an hour because every control loop is a few readable lines of source.

It provides:

- **A hardware + odometry base** — `CrawlerRobot`, a staged builder, and a swappable
  `CrawlerLocaliser` (three dead wheels, two dead wheels, GoBILDA Pinpoint, motor
  encoders, or a dev stub)
- **Field-oriented pure pursuit** — `Waypoint` + `FOFollower` (blocking) over
  `RobotMovement` (the pursuit math)
- **Robot-relative PID moves** — `RobotOrientedDrive` behind the `ROMovementEngine`
  base class (`drivePID`, `strafePID`, `turnPID`)
- **A guided tuner** — `TuningSession` / `TuningConfig`, driven by the
  `CrawlerTuner` OpMode with live editing in FTC Dashboard
- **Helpers** — `DashboardFieldViewUtils` (field-view drawing), `AprilTagWebcam`
  (vision), `CrawlerMath` / `Point` / `Vector2d` / `HeadingTimeline` (math utils)

There is deliberately **no global `RobotConfig` statics class** — every tunable value
lives on `CrawlerRobot.Config`, one object per robot instance.

## Project Structure

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/                          ← the library (read, don't break)
│   ├── core/
│   │   ├── Robot/
│   │   │   ├── CrawlerRobot.java     ← robot + staged Builder + Config
│   │   │   └── driveTrain.java       ← legacy, unused (see notes below)
│   │   ├── Localizers/
│   │   │   ├── CrawlerLocaliser.java         ← interface
│   │   │   ├── ThreeDeadWheelLocaliser.java
│   │   │   ├── TwoWheelLocaliser.java
│   │   │   ├── PinpointLocaliser.java
│   │   │   ├── MotorEncoderLocaliser.java
│   │   │   ├── SimulatedLocaliser.java   ← sim odometry for JVM integration tests
│   │   │   └── DevLocaliser.java
│   │   └── utils/
│   │       ├── Waypoint.java         ← builder: at(x,y,config).speed().slow().onReach()…
│   │       ├── Point.java / Vector2d.java
│   │       ├── CrawlerMath.java      ← wrapAngle, wrapRadians, clamp, lineCircleIntersection
│   │       └── UnitConverter.java    ← in↔cm↔m↔mm↔ft (field cm vs builder inches)
│   ├── FieldOrient/
│   │   ├── RobotMovement.java        ← pure pursuit core
│   │   └── FOFollower.java           ← blocking waypoint follower
│   ├── RobotOrient/
│   │   ├── RobotOrientedDrive.java   ← drivePID / strafePID / turnPID
│   │   ├── ROMovementEngine.java     ← abstract LinearOpMode base for short moves
│   │   ├── HeadingTimeline.java      ← heading keyframe interpolation (not yet wired in)
│   │   ├── AnimationBuilder.java     ← functional interface (not yet wired in)
│   │   ├── IndexerRotation.java      ← enum (orphaned)
│   │   └── Tuner.java                ← @Deprecated placeholder
│   ├── Tuning/
│   │   ├── TuningConfig.java         ← @Config("Crawler Tuner") live values
│   │   ├── TuningSession.java        ← 7-step guided session
│   │   ├── TuningPidRunner.java      ← runs PID tests through RobotOrientedDrive
│   │   ├── TuningRobotFactory.java   ← interface: build the tuning robot from your registered robot
│   │   ├── TuningTelemetry.java      ← Driver Station + Dashboard telemetry
│   │   ├── TuningActiveCheck.java    ← interface: is the OpMode still running?
│   │   ├── TuningDashboard.java      ← draws robot on the Dashboard field view
│   │   └── TuningUtil.java           ← gamepad edge helpers
│   ├── Dashboard/
│   │   └── DashboardFieldViewUtils.java  ← drawLine / drawPoint / drawRobot
│   ├── Vision/
│   │   ├── AprilTagWebcam.java       ← wraps AprilTagProcessor + VisionPortal
│   │   └── Rotation.java             ← ROLL / PITCH / YAW / RANGE / BEARING
│   └── annotations/
│       ├── Experimental.java
│       └── processor/ExperimentalProcessor.java
└── TeamscodeNotLibrary/              ← YOUR code (edit freely)
    ├── MyRobot.java                  ← extends CrawlerRobot; names + localizer + tuned numbers in one builder()
    ├── CrawlerTuner.java             ← the tuning OpMode
    ├── ExampleAuto.java / ExampleTeleOp.java
    ├── CrawlerSmokeTest.java / CrawlerSystemTest.java
    └── ManualAdjustExample.java
```

## Architecture Layers

### Layer 1 — `CrawlerRobot`: hardware + odometry base

`CrawlerRobot` owns the four `MotorEx` drive motors, the IMU, a `CrawlerLocaliser`,
and one `Config` object of tuned numbers. Teams never construct it directly — they
subclass it and pass a **staged builder** to `super(...)`:

```java
public class MyRobot extends CrawlerRobot {

    public MyRobot(HardwareMap hwMap) {
        super(builder(hwMap));
        // ...your season hardware...
    }

    public static CrawlerRobot.Builder builder(HardwareMap hwMap) {
        return new CrawlerRobot.Builder(hwMap)
                .frontLeft("fl").frontRight("fr")
                .backLeft("bl").backRight("br")
                .imu("imu")
                .motors()
                .withThreeDeadWheels("enc_l", "enc_r", "enc_c")   // pick ONE localizer
                .setTrackWidth(13.0)            // tuned numbers inline — paste tuner output here
                .setCenterWheelOffset(3.5)
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
                .maxDriveSpeed(1.0);
    }
}
```

Key public surface of `CrawlerRobot`:

| Member | Signature | Notes |
|---|---|---|
| Drive motors | `public final MotorEx frontLeft, frontRight, backLeft, backRight` | `MotorEx.set(double)` |
| IMU | `public final IMU imu` | |
| Localiser | `public final CrawlerLocaliser localiser` | |
| Localisation | `public final Localisation localisation` | enum: `MotorEncoder`, `TwoDeadWheel`, `ThreeDeadWheel`, `Pinpoint`, `Simulated`, `DevLocaliser` |
| Config | `public final Config config` | all tuned numbers |
| `drive` | `void drive(double forward, double strafe, double rotate)` | robot frame; clamped to `maxDriveSpeed` |
| `driveFieldRelative` | `void driveFieldRelative(double forward, double strafe, double rotate)` | field frame |
| `stop` | `void stop()` | stops all four motors |
| `update` | `void update()` | must be called every loop; advances the localiser |
| `getPose` | `Pose2d getPose()` | x/y in **centimeters**, heading in **radians** (FTCLib `Pose2d`) |
| `getHeading` | `double getHeading()` | radians |
| `resetPose` | `void resetPose()` | zero pose + IMU yaw reset |
| Encoders | `getLeftEncoder() / getRightEncoder() / getCenterEncoder()` | `MotorEx` or `null` |

> **Drive commands** (`drive`, `driveFieldRelative`) take power −1.0…1.0 and are
> clamped to `config.maxDriveSpeed` before the holonomic mix is applied.

### Layer 2 — Localizers

`CrawlerLocaliser` is a three-method interface (`update()`, `getPose()`, `resetPose(Pose2d)`).
The builder picks the implementation:

| Builder call | Implementation | Hardware |
|---|---|---|
| `.withThreeDeadWheels("l","r","c")` | `ThreeDeadWheelLocaliser` | three odometry pods (FTCLib `HolonomicOdometry`) |
| `.withTwoDeadWheels("l","c")` | `TwoWheelLocaliser` | two pods |
| `.withPinpoint("odo")` | `PinpointLocaliser` | GoBILDA Pinpoint |
| `.withMotorEncoders()` | `MotorEncoderLocaliser` | drive motor encoders |
| `.withSimulatedLocaliser()` | `SimulatedLocaliser` | sim mecanum odometry (JVM integration tests) |
| `.withDevLocaliser()` | `DevLocaliser` | zero pose (dev/tests only) |

All poses are normalized to **centimeters** by the library (e.g. `PinpointLocaliser`
converts the GoBILDA output to `DistanceUnit.CM`).

### Layer 3 — Movement

**Field-oriented (pure pursuit).** `RobotMovement` is the pursuit core:
`follow(List<Waypoint>, double preferredAngle)`, `getFollowPointPath(...)`,
`goToPosition(x, y, moveSpeed, preferredAngle, turnSpeed)`, plus path extension and
dynamic look-ahead. `FOFollower` wraps it for OpMode use:

```java
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
follower.follow(
    Waypoint.at(0, 0, robot.config).build(),
    Waypoint.at(100, 0, robot.config).speed(0.8).build(),
    Waypoint.at(100, 100, robot.config)
        .slow(robot.config)
        .onReach(robot::openClaw)
        .build()
);
```

- Blocks until the path is done; needs ≥ 2 waypoints; fires `onReach` once per arrival;
  aborts a leg after `config.timeoutSecs`; stops the robot when finished.
- **Note:** `Waypoint.heading` and `slowDownTurnRadians/slowDownTurnAmount` are
  stored by the builder but **not yet read by the follower** — the follower aims at
  the robot's current world heading with `RobotMovement.goToPosition`.

**Robot-oriented (PID).** `RobotOrientedDrive` runs `drivePID(double meters, int headingDeg)`,
`strafePID(double meters, int headingDeg)`, and `turnPID(int headingDeg)` — blocking,
clamped to ±0.7, heading-hold active. The convenient entry point is the abstract base
class `ROMovementEngine extends LinearOpMode`:

```java
@Autonomous(name = "Manual Adjust", group = "Crawler Tests")
public class ManualAdjust extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }

    @Override
    public void runPath() {
        drivePID(0.30, 0);    // 30 cm forward, hold 0°
        turnPID(45);          // turn to absolute 45°
        strafePID(0.20, 45);  // 20 cm right, hold 45°
    }
}
```

`ROMovementEngine` wires the robot, resets pose/IMU, waits for start, calls
`runPath()`, and stops the robot at the end.

### Layer 4 — The tuner

`TuningConfig` is a `@Config("Crawler Tuner")` class of `public static` fields — FTC
Dashboard edits them live. `TuningSession` seeds those fields from the robot's builder
(no presets), rebuilds the tuning robot (via a `TuningRobotFactory`) whenever a value
changes, and walks 7 steps: Motors → Encoders → Track width → Center offset → PID →
Auto path → Finish. The PID tests run through the **real** `RobotOrientedDrive` engine,
so tuned gains behave identically in a match. The OpMode lives in team code and builds
whatever robot is registered with `CrawlerRobotRegistry`:

```java
TuningRobotFactory factory = new TuningRobotFactory() {
    public CrawlerRobot create() { return CrawlerRobotRegistry.create(hardwareMap); }
    public CrawlerRobot create(CrawlerRobot.Config config) {
        return CrawlerRobotRegistry.create(hardwareMap, config);
    }
};
TuningSession session = new TuningSession(factory, telemetry, gamepad1, () -> opModeIsActive());
```

### Layer 5 — Vision, Dashboard, annotations

- `AprilTagWebcam` wraps `AprilTagProcessor` + `VisionPortal`; `Rotation` is an enum
  (`ROLL`, `PITCH`, `YAW`, `RANGE`, `BEARING`) used by `getAngle(detection, rotation)`.
- `DashboardFieldViewUtils` draws on the FTC Dashboard field overlay
  (`drawLine`, `drawPoint`, `drawRobot`, `FieldColor`).
- `annotations/Experimental` is a marker annotation with a compile-time processor.

## Configuration System

All tuned numbers live on `CrawlerRobot.Config` (one per robot instance). They are set
two ways:

1. **In code** — builder methods in `MyRobot.builder(...)`:
   `.setTrackWidth(in)`, `.setCenterWheelOffset(in)`, `.wheelDiameter(in)`,
   `.ticksPerRev(n)`, `.drivePid(kp,ki,kd)`, `.strafePid(kp,ki,kd)`,
   `.steerPid(p,i,d)`, `.minPower(p)`, `.pathDefaults(move, turn, followCm)`,
   `.arrivalThresholdCm(cm)`, `.orbitThresholdCm(cm)`, `.timeoutSecs(s)`,
   `.maxDriveSpeed(p)`. The tuned numbers are **inline in the builder chain** — the
   Crawler Tuner prints matching builder lines to paste there.
2. **Live** — the tuner's `TuningConfig` mirrors the same fields into the Dashboard
   (`http://<robot-ip>:8080/dash` → `Crawler Tuner` panel), seeded from the robot's
   builder when the tuner starts. The tuner rebuilds the robot via the registered
   provider (`CrawlerRobotRegistry`) when a value changes.

The slow-mode values (`slowMoveSpeed`, `slowTurnSpeed`, `slowFollowDistanceCm`, …)
are set from the builder with `.slowSpeeds(...)` / `.slowDownTurn(...)` (0 = not used).

## Data Flow

```
OpMode (LinearOpMode)
  │  build: new MyRobot(hardwareMap)          → CrawlerRobot.Builder → CrawlerRobot
  │  every loop: robot.update()               → localiser.update() → getPose()
  │
  ├─ Field-oriented: FOFollower.follow(waypoints)
  │      └─ RobotMovement.goToPosition(x, y, speed, angle, turn)
  │             └─ robot.driveFieldRelative(f, s, r)   (field → robot frame)
  │
  ├─ Robot-oriented: RobotOrientedDrive.drivePID/strafePID/turnPID
  │      └─ robot.drive(f, s, r)
  │
  └─ TeleOp: robot.driveFieldRelative(-ls_y, ls_x, rs_x)
             └─ robot.drive(rotated_x, rotated_y, rotate)
                      └─ motor.set(power) on FL/FR/BL/BR
```

## Development Workflow

1. **Edit only `TeamscodeNotLibrary/`.** Match device names at the top of
   `MyRobot.java` to the Driver Hub configuration.
2. **Run the Crawler Tuner** → paste the printed builder lines into the tuned section
   of your robot's `builder()` (the tuner rebuilds your registered robot via
   `CrawlerRobotRegistry`, so hardware names never drift).
3. **Validate** — `CrawlerSmokeTest` (2 min) then `CrawlerSystemTest` (TeleOp).
4. **Write autos** — copy `ExampleAuto.java` (waypoints) and `ManualAdjustExample.java`
   (short PID moves).
5. **JVM unit tests** for the pure math/config live under
   `TeamCode/src/test/...` — run with `./gradlew :TeamCode:testDebugUnitTest`.

## Integration Examples

**Field-oriented autonomous** (from `ExampleAuto.java`):

```java
@Autonomous(name = "Example Auto", group = "Crawler Examples")
public class ExampleAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

        waitForStart();

        follower.follow(
                Waypoint.at(0, 0, robot.config).build(),
                Waypoint.at(60, 0, robot.config)
                        .speed(0.8).turnSpeed(0.4)
                        .onReach(() -> robot.openClaw())
                        .build(),
                Waypoint.at(60, 60, robot.config)
                        .slow(robot.config)
                        .onReach(() -> robot.scoreHighBasket())
                        .build(),
                Waypoint.at(0, 0, robot.config).speed(0.8).turnSpeed(0.4).build()
        );

        robot.stop();
    }
}
```

**TeleOp** (from `ExampleTeleOp.java`):

```java
while (opModeIsActive()) {
    robot.update();
    robot.driveFieldRelative(
            -gamepad1.left_stick_y,   // forward / backward (field frame)
            gamepad1.left_stick_x,    // strafe
            gamepad1.right_stick_x);  // rotate
    if (gamepad1.a) robot.openClaw();
    else if (gamepad1.b) robot.closeClaw();
    idle();
}
robot.stop();
```

**Drawing on the Dashboard field view**:

```java
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;

TelemetryPacket packet = new TelemetryPacket();
DashboardFieldViewUtils.drawRobot(packet,
        robot.getPose().getX(), robot.getPose().getY(),
        robot.getPose().getHeading(), DashboardFieldViewUtils.FieldColor.BLUE);
FtcDashboard.getInstance().sendTelemetryPacket(packet);
```

## Known dead / not-yet-wired code

These exist in the library but are **not part of the supported path** — flag for
removal or wiring-up, don't build on them:

- `core/Robot/driveTrain.java` — legacy wrapper that casts `MotorEx` to `DcMotor`;
  unused (movement goes through `robot.drive(...)`).
- `RobotOrient/HeadingTimeline.java`, `AnimationBuilder.java`, `IndexerRotation.java` —
  heading-keyframe pieces with unit tests but **no engine consumes them**.
- `RobotOrient/Tuner.java` — `@Deprecated` stub that throws.
- `Waypoint.heading` / `Waypoint.slowDown(...)` — stored but ignored by `FOFollower`.
- `RobotMovement.follow(List, double)` — builds the Dashboard field-view packet
  (path + follow point) but **never sends it**; the field view stays blank during
  paths. Only the tuner's tests (`TuningDashboard.drawRobot`) draw live.

## More Information

- **Docs site** — `docs/` (built with `npm run build` into `docs-html/`)
- **Public API inventory** — `BUILD_SPECIFICATION.md`
- **FTC SDK** — https://ftc-docs.firstinspires.org/
- **FTC Dashboard** — https://acmerobotics.github.io/ftc-dashboard/
