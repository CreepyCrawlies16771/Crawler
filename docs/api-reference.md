---
title: API Reference
description: Every public class, method, and value in the Crawler library — verified against the source
---

# API Reference

*Every public piece of Crawler, with exact signatures — verified against the library source.*

This is the ground-truth reference. If a doc page and this page disagree, this page is right. All code below is real usage you can paste — parameter names, order, and types come straight from the source.

## Packages at a glance

| Package | What lives there |
|---|---|
| `Crawler.core.Robot` | `CrawlerRobot` (the robot + builder + `Config`) |
| `Crawler.core.Localizers` | 5 localizers (3 dead wheel, 2 dead wheel, Pinpoint, motor encoders, dev) |
| `Crawler.core.utils` | `Waypoint`, `Point`, `Vector2d`, `CrawlerMath`, `UnitConverter` |
| `Crawler.core.errors` | `CrawlerError`, `CrawlerErrors`, `CrawlerPreflight`, `CrawlerErrorException` |
| `Crawler.FieldOrient` | `FOFollower`, `RobotMovement` (pure pursuit) |
| `Crawler.RobotOrient` | `RobotOrientedDrive`, `ROMovementEngine` (robot-relative PID) |
| `Crawler.Tuning` | `TuningSession`, `TuningConfig`, `TuningRobotFactory`, `TuningPidRunner`, `TuningDashboard`, `TuningTelemetry` |
| `Crawler.Dashboard` | `DashboardFieldViewUtils` (FTC Dashboard field-view drawing) |
| `Crawler.Vision` | `AprilTagWebcam`, `Rotation` |

Teams only write code that imports the **utils**, **FieldOrient**, **RobotOrient**, and **errors** packages directly. `Tuning` is driven by the shipped `CrawlerTuner` OpMode.

---

## 1 · `CrawlerRobot` — the robot

`TeamCode/.../Crawler/core/Robot/CrawlerRobot.java`

Your class extends it; the builder constructs it. There is **no public constructor** — build via the builder:

```java
CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
        .frontLeft("fl").frontRight("fr")
        .backLeft("bl").backRight("br")
        .imu("imu")
        .imuOrientation(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)
        .motors()
        .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
        .build();
```

### Builder stages (order matters)

The builder is **staged** — each stage only exposes the calls valid at that point:

| Stage | Methods | Ends with |
|---|---|---|
| Motor names | `.frontLeft(name)` `.frontRight(name)` `.backLeft(name)` `.backRight(name)` `.imu(name)` `.imuOrientation(logo, usb)` `.invertFrontLeft()` `.invertFrontRight()` `.invertBackLeft()` `.invertBackRight()` | `.motors()` |
| Localizer | `.withMotorEncoders()` · `.withSimulatedLocaliser()` · `.withDevLocaliser()` · `.withThreeDeadWheels(l, r, c)` · `.withTwoDeadWheels(l, c)` · `.withPinpoint(name)` | varies |
| Localizer config | `.setTrackWidth(in)` · `.setCenterWheelOffset(in)` · `.invertLeftEncoder()` `.invertRightEncoder()` `.invertCenterEncoder()` · `.setConfig(x, y, unit, pod, xDir, yDir)` (Pinpoint) | `.build()` or `IReadyStage` |
| Tuning | `.wheelDiameter(in)` `.ticksPerRev(n)` `.drivePid(kp, ki, kd)` `.strafePid(kp, ki, kd)` `.steerPid(p, i, d)` `.minPower(x)` `.pathDefaults(move, turn, followCm)` `.arrivalThresholdCm(cm)` `.orbitThresholdCm(cm)` `.timeoutSecs(s)` `.maxDriveSpeed(x)` | `.build()` |

The fully-configured chain that teams actually use lives in `MyRobot.builder()`:

```java
public static CrawlerRobot.Builder builder(HardwareMap hwMap) {
    return new CrawlerRobot.Builder(hwMap)
            .frontLeft("fl").frontRight("fr")
            .backLeft("bl").backRight("br")
            .imu("imu")
            .imuOrientation(
                    RevHubOrientationOnRobot.LogoFacingDirection.UP,
                    RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)
            .motors()
            .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
            .setTrackWidth(13.0)
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
```

### Public fields

| Field | Type | Meaning |
|---|---|---|
| `config` | `CrawlerRobot.Config` | All tuned values, read live by every movement system |
| `frontLeft` / `frontRight` / `backLeft` / `backRight` | `MotorEx` (FTCLib) | Drive motors — `set(power)` them directly if you need raw control |
| `imu` | `IMU` | The REV IMU, already initialized with your orientation |
| `localisation` | `Localisation` | Which localizer was selected (`MotorEncoder`, `TwoDeadWheel`, `ThreeDeadWheel`, `Pinpoint`, `Simulated`, `DevLocaliser`) |
| `localiser` | `CrawlerLocaliser` | The active localizer — `update()`, `getPose()`, `resetPose(pose)` |

### Public methods

```java
robot.drive(forward, strafe, rotate);        // robot-relative powers, -1..1, clamped by maxDriveSpeed
robot.driveFieldRelative(f, s, r);           // field-relative powers (heading applied for you)
robot.stop();                                // zero all four motors
robot.update();                              // advance the localizer (call every loop)
Pose2d pose = robot.getPose();               // X/Y in CM, heading in RADIANS
double headingRad = robot.getHeading();
robot.resetPose();                           // pose = (0, 0, 0), IMU yaw reset — required before paths
robot.startPose(xCm, yCm, headingRad);       // custom start pose, IMU yaw reset
boolean ok = robot.isPoseInitialized();      // true once resetPose()/startPose() called
robot.getLeftEncoder();                      // MotorEx or null (tuner Step 2)
robot.getRightEncoder();
robot.getCenterEncoder();
```

> ⚠️ **`getPose()` units:** X/Y are **centimeters**, heading is **radians**. `FOFollower`/`Waypoint` are also cm. Only the builder's odometry sizes (`setTrackWidth`, `wheelDiameter`, …) and `drivePID`/`strafePID` distances are inches/meters.

### `CrawlerRobot.Config` — every tunable value

All fields are `public double` with **no preset values** — every one must be set in the
builder chain of your robot class, or `build()` fails. Read them via `robot.config`:

| Field | Builder setter | Unit | Meaning |
|---|---|---|---|
| `trackWidth` | `.setTrackWidth(x)` | in | Distance between the two parallel odometry wheels |
| `centerWheelOffset` | `.setCenterWheelOffset(x)` | in | Forward distance from center to the perpendicular wheel |
| `wheelDiameter` | `.wheelDiameter(x)` | in | Odometry wheel diameter (35 mm pod) |
| `ticksPerRev` | `.ticksPerRev(n)` | ticks | Encoder counts per wheel revolution |
| `driveKp` / `driveKi` / `driveKd` | `.drivePid(kp, ki, kd)` | per m | Drive PID gains |
| `strafeKp` / `strafeKi` / `strafeKd` | `.strafePid(kp, ki, kd)` | per m | Strafe PID gains |
| `steerP` / `steerI` / `steerD` | `.steerPid(p, i, d)` | per ° | Heading-hold and turn PID gains |
| `minPower` | `.minPower(x)` | power | Friction deadband — smallest power that moves the robot |
| `defaultMoveSpeed` | `.pathDefaults(move, turn, follow)` | power | Cruise power between waypoints |
| `defaultTurnSpeed` | `.pathDefaults(move, turn, follow)` | power | Turn-power scale during paths |
| `followDistanceCm` | `.pathDefaults(move, turn, follow)` | cm | Pure-pursuit look-ahead radius |
| `arrivalThresholdCm` | `.arrivalThresholdCm(x)` | cm | "Arrived" distance → fires `onReach` |
| `orbitThresholdCm` | `.orbitThresholdCm(x)` | cm | Distance over which turn power fades to zero near a waypoint |
| `slowMoveSpeed` / `slowTurnSpeed` / `slowFollowDistanceCm` | `.slowSpeeds(move, turn, followCm)` | — | Used by `Waypoint.slow(config)`; 0 = not used |
| `slowDownTurnRadians` / `slowDownTurnAmount` | `.slowDownTurn(radians, amount)` | — | Turn slow-down trigger/amount; 0 = not used |
| `timeoutSecs` | `.timeoutSecs(x)` | s | Max seconds per path leg |
| `maxDriveSpeed` | `.maxDriveSpeed(x)` | power | Clamp for `drive()`/`driveFieldRelative()` |
| `turnReferenceRadians` | `.turnReferenceRadians(x)` | rad | Heading-error scale for path turn power |

```java
double tpm = robot.config.ticksPerMeter();   // ticks per meter (computed from diameter/ticks)
double tpc = robot.config.ticksPerCm();      // ticks per cm
```

---

## 2 · Localizers

All five implement `CrawlerLocaliser` (`update()`, `getPose()`, `resetPose(Pose2d)`). Pick one in the builder:

| Localizer | Builder | Notes |
|---|---|---|
| `ThreeDeadWheelLocaliser` | `.withThreeDeadWheels(l, r, c)` | Requires `.setTrackWidth(...)` + `.setCenterWheelOffset(...)`; supports `.invertLeftEncoder()` etc. |
| `TwoWheelLocaliser` | `.withTwoDeadWheels(l, c)` | Requires `.setTrackWidth(...)` |
| `PinpointLocaliser` | `.withPinpoint(name)` + `.setConfig(...)` | GoBILDA Pinpoint v1/v2, full config via `.setConfig` |
| `MotorEncoderLocaliser` | `.withMotorEncoders()` | Uses the four drive motors |
| `SimulatedLocaliser` | `.withSimulatedLocaliser()` | Simulated mecanum odometry — for JVM integration tests (see [Setup → SimulatedLocaliser](setup.md#6--simulatedlocaliser-integration-tests)) |
| `DevLocaliser` | `.withDevLocaliser()` | Simulated pose — for testing without hardware |

**Pinpoint full config:**

```java
.withPinpoint("odo")
.setConfig(
        3.0, 2.5,                                          // x, y offsets from robot center
        DistanceUnit.CM,                                   // your offset unit
        GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD,
        GoBildaPinpointDriver.EncoderDirection.REVERSED,
        GoBildaPinpointDriver.EncoderDirection.FORWARD)
.build();
```

---

## 3 · Robot-relative PID — `RobotOrientedDrive`

`TeamCode/.../Crawler/RobotOrient/RobotOrientedDrive.java`

```java
RobotOrientedDrive ro = new RobotOrientedDrive(robot, this::opModeIsActive, telemetry);

ro.drivePID(0.30, 0);    // 30 cm forward (0.30 m), hold 0° heading
ro.strafePID(0.20, 45);  // 20 cm right, hold 45°
ro.turnPID(90);          // turn to absolute 90°
```

| Method | Target units | Gains used |
|---|---|---|
| `drivePID(double meters, int headingDeg)` | **meters**, degrees | `driveKp/Ki/Kd` (per meter) |
| `strafePID(double meters, int headingDeg)` | **meters** (positive = right), degrees | `strafeKp/Ki/Kd` (per meter) |
| `turnPID(int headingDeg)` | **degrees** (absolute, IMU) | `steerP/I/D` (per degree) |

### The control loop (exactly as implemented)

For `drivePID`/`strafePID`, each cycle computes the remaining distance in cm, converts to meters, and feeds the PID:

```java
double errorM = (targetCm - traveledCm) / 100.0;
double power  = pid.update(errorM, timer.seconds());     // P + I + D
power = clamp(power, ±0.7);                              // hard clamp
if (traveled < 3 cm) apply minPower deadband;            // deadband only at the start
power *= sign;                                           // direction
robot.drive(power, 0, steer);                            // + heading hold
```

`turnPID` feeds the **wrapped degree error** directly (per-degree gains), and its deadband applies while `|error| > 8°`.

### The PID math

```java
P = kp * error
I = ki * integral          // integral clamped to ±0.3
D = kd * d(error)/dt       // derivative on error, dt from a timer
```

- **Integral** resets to zero when the error is 0 or changes sign (anti-windup), and is clamped to **±0.3**.
- **Derivative** is computed on the error signal, so it damps approach speed.
- All three loops stop at `config.timeoutSecs` and always call `robot.stop()` at the end.

### Heading hold

While driving/strafing, `headingHoldPower(targetDeg)` applies `steerP × wrappedError` with the min-power deadband **always on**:

```java
double steer = clamp(angleWrapDeg(targetDeg - imuYawDeg()) * config.steerP, true);
```

### `DebugSink` — watch every loop

`RobotOrientedDrive` exposes an optional observer used by the Crawler Tuner to stream P/I/D live:

```java
ro.setDebugSink((error, power, p, i, d) -> {
    telemetry.addData("error", error);
    telemetry.addData("P/I/D", String.format("%.3f / %.4f / %.4f", p, i, d));
});
```

```java
public interface DebugSink {
    void onLoop(double error, double power, double p, double i, double d);
}
```

### `ROMovementEngine` — the base class for PID autos

```java
@Autonomous(name = "Manual Adjust", group = "Crawler Tests")
public class ManualAdjust extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }

    @Override
    public void runPath() throws InterruptedException {
        drivePID(0.30, 0);    // these delegate to RobotOrientedDrive
        turnPID(45);
        strafePID(0.20, 45);
    }
}
```

`ROMovementEngine` (extends `LinearOpMode`) wires everything: builds the robot from your `buildRobot()`, constructs `RobotOrientedDrive`, waits for start, resets IMU + pose, calls `runPath()`, and stops. Your `drivePID`/`strafePID`/`turnPID` calls delegate to it.

---

## 4 · Field-oriented paths — `FOFollower` + `Waypoint`

`TeamCode/.../Crawler/FieldOrient/FOFollower.java` · `TeamCode/.../Crawler/core/utils/Waypoint.java`

```java
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

follower.follow(                       // blocks until done or OpMode stops
    Waypoint.at(0, 0, robot.config).build(),              // first waypoint = start
    Waypoint.at(100, 0, robot.config).speed(0.8).build(),
    Waypoint.at(100, 100, robot.config)
        .slow(robot.config)
        .onReach(robot::scoreHighBasket)
        .build()
);
```

### `FOFollower` methods

| Method | Signature | Notes |
|---|---|---|
| Constructor | `FOFollower(CrawlerRobot, Telemetry, OpModeProxy)` | `OpModeProxy` is a one-method interface: `boolean isActive()` — pass `this::opModeIsActive` |
| `follow` | `void follow(List<Waypoint>) throws InterruptedException` | Runs preflight first, then each waypoint in order |
| `follow` | `void follow(Waypoint...) throws InterruptedException` | Varargs convenience |

`follow()` runs **`CrawlerPreflight.run(...)`** before moving: config sanity, start-pose set, localizer health, IMU, and path validation. A waypoint is "reached" within `arrivalThresholdCm`; each leg aborts after `timeoutSecs` with a warning; every `onReach` fires once on arrival.

### `Waypoint.at(...)` — the builder

| Call | Defaults from | Notes |
|---|---|---|
| `Waypoint.at(x, y, robot.config)` | your tuned `Config` | **Prefer this** — picks up your defaults |
| `Waypoint.at(x, y)` | a fresh default `Config` | Bare overload — library defaults only |

Builder methods (all optional except `.build()`):

```java
Waypoint.at(x, y, robot.config)     // x, y in CM, field frame
        .heading(0.5)               // stored, NOT read by FOFollower yet
        .speed(0.8)                 // move-power override
        .turnSpeed(0.4)             // turn-power override
        .followDistance(20.0)       // look-ahead radius override (cm)
        .slowDown(0.5, 0.5)         // stored, NOT read by FOFollower yet
        .slow(robot.config)         // slowMoveSpeed / slowTurnSpeed / slowFollowDistanceCm
        .onReach(() -> robot.openClaw())   // runs on arrival
        .build();                   // required — validates + returns the Waypoint
```

### `RobotMovement` — the low-level pursuit engine

`FOFollower` wraps `RobotMovement`. You usually don't call it directly, but it's public:

```java
RobotMovement m = new RobotMovement(robot);
m.goToPosition(xCm, yCm, moveSpeed, preferredHeadingRad, turnSpeed);
m.follow(List<Waypoint>, double followHeadingRad);   // one pure-pursuit cycle
m.getWorldX(); m.getWorldY(); m.getWorldHeading();   // cached pose, cm + radians
```

`goToPosition` computes robot-relative movement from the field frame, scales turn power by `orbitThresholdCm` (fades near the target), and calls `robot.driveFieldRelative(...)`.

---

## 5 · Tuning subsystem

All in `TeamCode/.../Crawler/Tuning/`. Driven by the shipped `CrawlerTuner` OpMode — see [Tuning](tuning.md).

| Class | Role |
|---|---|
| `TuningSession` | The 7-step guided session; `loop()` per OpMode cycle |
| `TuningConfig` | `public static` live values, annotated `@Config("Crawler Tuner")` for FTC Dashboard |
| `TuningRobotFactory` | Interface `CrawlerRobot create()` + `create(CrawlerRobot.Config)` — built from your registered robot |
| `TuningPidRunner` | Runs PID tests through the **real** `RobotOrientedDrive` engine |
| `TuningDashboard` | `drawRobot(robot)` → robot on the Dashboard field view |
| `TuningTelemetry` | `MultipleTelemetry` wrapping Driver Station + Dashboard |
| `TuningActiveCheck` | `boolean isActive()` — OpMode stop detection |
| `TuningUtil` | Gamepad edge-detection + IMU helpers |
| `TuningSnippet` | Builds the copy-paste builder lines |

**Your hook is `CrawlerRobotRegistry`** — the shipped OpModes build whatever robot you
registered (see [Setup](setup.md)), and `TuningSession` seeds the tuner's starting
values from that robot's builder instead of hard-coded presets:

```java
// In CrawlerTuner.java:
TuningRobotFactory factory = new TuningRobotFactory() {
    public CrawlerRobot create() { return CrawlerRobotRegistry.create(hardwareMap); }
    public CrawlerRobot create(CrawlerRobot.Config config) {
        return CrawlerRobotRegistry.create(hardwareMap, config);
    }
};
```

---

## 6 · FTC Dashboard — `DashboardFieldViewUtils`

`TeamCode/.../Crawler/Dashboard/DashboardFieldViewUtils.java`

```java
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;

TelemetryPacket packet = new TelemetryPacket();

DashboardFieldViewUtils.drawRobot(packet,
        robot.getPose().getX(), robot.getPose().getY(),
        robot.getPose().getHeading(),                       // radians
        DashboardFieldViewUtils.FieldColor.BLUE);

DashboardFieldViewUtils.drawLine(packet, x1, y1, x2, y2, FieldColor.GREEN);
DashboardFieldViewUtils.drawPoint(packet, x, y, FieldColor.RED);

FtcDashboard.getInstance().sendTelemetryPacket(packet);
```

`FieldColor` enum: `RED BLUE GREEN YELLOW ORANGE PURPLE CYAN MAGENTA BLACK WHITE`. All three drawing methods take a `TelemetryPacket` plus coordinates in **field cm**. (`RobotMovement.follow` builds a path polyline + follow-point packet internally, but that packet isn't sent to the Dashboard yet — the tuner draws the robot itself.)

---

## 7 · Vision — `AprilTagWebcam` + `Rotation`

`TeamCode/.../Crawler/Vision/AprilTagWebcam.java` · `Rotation.java`

```java
AprilTagWebcam vision = new AprilTagWebcam();
vision.init(hardwareMap, telemetry);   // camera name is "Webcam 1" in the source

while (opModeIsActive()) {
    vision.update();
    AprilTagDetection tag = vision.getTagBySpecificId(1);
    if (tag != null) {
        double range = vision.getAngle(tag, Rotation.RANGE);   // cm (init sets DistanceUnit.CM)
        double yaw   = vision.getAngle(tag, Rotation.YAW);     // degrees
        robot.drivePID(UnitConverter.cmToM(range - 20.0), 0);  // approach the tag
    }
}
vision.close();
```

| Method | Returns |
|---|---|
| `init(HardwareMap, Telemetry)` | Sets up the AprilTag processor (`DistanceUnit.CM`, `AngleUnit.DEGREES`) at 640×480 on **"Webcam 1"** |
| `update()` | Refreshes detections |
| `getDetectedTags()` | `List<AprilTagDetection>` |
| `getTagBySpecificId(int)` | `AprilTagDetection` or `null` |
| `getAngle(AprilTagDetection, Rotation)` | `ROLL`/`PITCH`/`YAW`/`RANGE`/`BEARING` from `ftcPose` |
| `displayDetectionTelemetry(tag)` | Prints tag info to telemetry |
| `close()` | Closes the vision portal |

---

## 8 · Utils

### `CrawlerMath`

```java
CrawlerMath.wrapAngle(deg);        // → [-180, 180] degrees
CrawlerMath.wrapRadians(rad);      // → [-π, π] radians
CrawlerMath.clamp(v, min, max);    // → clamped double
CrawlerMath.lineCircleIntersection(center, radius, a, b);  // → ArrayList<Point>
```

### `Point` / `Vector2d`

```java
Point p = new Point(x, y);                  // public mutable x, y
Point.fromCurvePoint(waypoint);             // Point from a Waypoint
Vector2d v = new Vector2d(x, y);            // immutable
v.distanceTo(other); v.magnitude(); v.normalized();
v.plus(o); v.minus(o); v.times(s); v.dot(o);
v.angleTo(o);                               // radians
Point pp = v.toPoint();
```

### `UnitConverter` — new in this release

Crawler's **field geometry is centimeters**; the **builder's odometry sizes are inches**; `drivePID`/`strafePID` take **meters**. `UnitConverter` is the bridge:

```java
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

UnitConverter.inToCm(13.0);      // 33.02  — track width in → field cm
UnitConverter.cmToIn(33.02);     // 13.0
UnitConverter.mToCm(0.30);       // 30.0   — drivePID meters → cm
UnitConverter.cmToM(30.0);       // 0.3
UnitConverter.mmToCm(35.0);      // 3.5    — pod size
UnitConverter.cmToMm(3.5);       // 35.0
UnitConverter.ftToCm(12.0);      // 365.76 — FTC field length
UnitConverter.cmToFt(365.76);    // 12.0
```

Constants: `CM_PER_INCH = 2.54`, `INCHES_PER_CM = 0.3937…`. All methods are `static`.

```java
.setTrackWidth(UnitConverter.inToCm(13.0))   // builder odometry sizes accept cm too
```

---

## 9 · Errors — `CrawlerError`, `CrawlerErrors`, `CrawlerPreflight`

Errors render as `CRWL-XXX` with a fix line and the source location. See the full [Error Catalog](errors.md).

```java
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrors;

CrawlerErrors.throwError(CrawlerError.PATH_BAD_SPEED, speed);          // throws, stops OpMode
CrawlerErrors.postToTelemetry(telemetry, CrawlerError.ODO_DRIFT_DETECTED, drift);  // warning only
```

**Where validation runs:**

| Check | When |
|---|---|
| `CrawlerPreflight.checkConfigOrThrow(config)` | `Builder.build()` — before the robot exists |
| `CrawlerPreflight.checkEngine(robot, telemetry)` | `RobotOrientedDrive` constructor (INIT) |
| `CrawlerPreflight.run(robot, waypoints, telemetry)` | `FOFollower.follow()` — before the first motor spins |

---

## 10 · Things that exist but aren't wired in yet

Verified against the source — present, but not consumed by the movement code today:

| Symbol | Status |
|---|---|
| `Waypoint.heading` / `Waypoint.slowDownTurnRadians` / `slowDownTurnAmount` | Stored on the waypoint; **`FOFollower` never reads them** — shape legs with `.speed()`/`.turnSpeed()`/`.followDistance()` instead |
| `HeadingTimeline` / `AnimationBuilder` / `IndexerRotation` / `Tuner` (`RobotOrient`) | Legacy/unused pieces — not part of any supported workflow |
| `RobotMovement.follow(...)` Dashboard packet | Built but never sent — the tuner draws the robot itself |
| `Config.slowMoveSpeed` / `slowTurnSpeed` / `slowFollowDistanceCm` | Read by `Waypoint.slow(config)`, but no builder methods (edit the library defaults if you need them) |
| `annotations.Experimental` | Annotation + processor exist; no team-facing behavior yet |

---

## Next Steps

- **[Configuration →](configuration.md)** What every `Config` value does and how to set it
- **[Robot-Oriented Movement →](robot-oriented.md)** `drivePID` / `strafePID` / `turnPID` in practice
- **[Pure Pursuit →](pure-pursuit.md)** How `FOFollower` steers paths
