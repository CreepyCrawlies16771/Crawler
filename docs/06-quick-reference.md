# Quick Reference Guide

**Keep this open while coding.** Copy-paste patterns, hardware names, and common PID values.

---

## 1-Minute Setup

```java
// MyRobot.java
public class MyRobot {
    public static CrawlerRobot build(HardwareMap hwMap) {
        return CrawlerRobot.builder(hwMap)
                .frontLeftName("frontLeft")
                .frontRightName("frontRight")
                .backLeftName("backLeft")
                .backRightName("backRight")
                .localisation(CrawlerRobot.Localisation.ThreeDeadWheel)
                .leftEncoderName("enc_l")
                .rightEncoderName("enc_r")
                .centerEncoderName("enc_c")
                .imuName("imu")
                .trackWidth(13.0)
                .centerWheelOffset(3.5)
                .build();
    }
}
```

---

## Minimal OpMode Templates

### Robot-Oriented (Imperative)

```java
@Autonomous(name = "Simple RO", group = "Examples")
public class SimpleROAuto extends LinearOpMode {
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        waitForStart();
        if (opModeIsActive()) {
            robot.drivePID(12);
            robot.strafePID(6);
            robot.turnPID(90);
        }
    }
}
```

### Field-Oriented (Pure Pursuit)

```java
@Autonomous(name = "Simple FO", group = "Examples")
public class SimpleFOAuto extends LinearOpMode {
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        waitForStart();
        if (opModeIsActive()) {
            var path = Waypoint.at(0, 0)
                    .at(24, 0)
                    .at(24, 24)
                    .buildAll();
            follower.follow(path, 0);
        }
    }
}
```

---

## Hardware Names Checklist

| Device | Typical Name | Where to Find |
|--------|--------------|---------------|
| Front-left motor | `frontLeft` | Rev Hardware Client |
| Front-right motor | `frontRight` | Rev Hardware Client |
| Back-left motor | `backLeft` | Rev Hardware Client |
| Back-right motor | `backRight` | Rev Hardware Client |
| IMU | `imu` | Rev Expansion Hub |
| Left encoder | `enc_l` | I2C port 0 |
| Right encoder | `enc_r` | I2C port 1 |
| Center encoder | `enc_c` | I2C port 2 |

---

## Physical Constants: Typical Values

| Constant | Typical | Your Robot |
|----------|---------|-----------|
| `TRACK_WIDTH` | 13.0" | ________ |
| `CENTER_WHEEL_OFFSET` | 3.5" | ________ |
| `WHEEL_DIAMETER` | 1.37795" | ________ |
| `TICKS_PER_REV` | 2000 | ________ |

---

## PID Starting Values

```java
// Start here, then tune via Dashboard
RobotConfig.RobotOriented.Kp = 0.05;
RobotConfig.RobotOriented.Ki = 0.0;
RobotConfig.RobotOriented.Kd = 0.0;

RobotConfig.RobotOriented.strafe_Kp = 0.05;
RobotConfig.RobotOriented.strafe_Kd = 0.0;

RobotConfig.RobotOriented.STEER_P = 0.03;
RobotConfig.RobotOriented.STEER_D = 0.0;

RobotConfig.RobotOriented.MIN_POWER = 0.15;
```

---

## Command Cheat Sheet

### Robot-Oriented Commands

```java
robot.drivePID(12);           // Forward 12"
robot.drivePID(-6);           // Backward 6"
robot.strafePID(8);           // Right 8"
robot.strafePID(-12);         // Left 12"
robot.turnPID(90);            // Turn 90° CCW
robot.turnPID(-45);           // Turn 45° CW
robot.arc(90, 12);            // Arc 90° with 12" radius
robot.stop();                 // Stop all motors

// With telemetry
var pose = robot.localiser.getPose();
telemetry.addData("X", pose.getX());
telemetry.addData("Y", pose.getY());
telemetry.addData("Heading (rad)", pose.getHeading());
telemetry.addData("Heading (deg)", Math.toDegrees(pose.getHeading()));
```

### Field-Oriented Commands

```java
// Build path
var path = Waypoint.at(0, 0)
        .at(24, 0)
        .at(24, 24)
        .buildAll();

// Follow path
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
follower.follow(path, 0);  // followAngle in radians

// With callbacks
var path = Waypoint.at(0, 0)
        .at(24, 24)
            .slow()
            .onReach(() -> robot.depositSample())
        .buildAll();

// Speed presets
.speed(0.7)           // 70% speed
.slow()               // 30% speed (preset)
.turnSpeed(0.4)       // Rotation speed
```

---

## Configuration Checklist

### Before First Autonomous

- [ ] Hardware names verified in Android Studio
- [ ] `MyRobot.java` created
- [ ] Motor directions verified (forwards = positive)
- [ ] Encoders configured and tested
- [ ] Physical constants measured:
  - [ ] `TRACK_WIDTH` measured and entered
  - [ ] `CENTER_WHEEL_OFFSET` measured and entered
  - [ ] `WHEEL_DIAMETER` verified
- [ ] Odometry tested (TuneOdometry OpMode)
- [ ] FTC Dashboard installed
- [ ] First autonomous uploaded and tested

### Before Competition

- [ ] PID tuned via Dashboard
- [ ] Multiple test runs on competition field
- [ ] Autonomous time verified (should be consistent ±2s)
- [ ] All waypoints reached within ±2"
- [ ] Callbacks execute at correct waypoints
- [ ] Battery voltage logged (affects motor power)
- [ ] Second robot profile created if multiple robots

---

## Common PID Tuning Patterns

### Problem: Robot overshoots target

```java
// Decrease Kp, increase Kd
Kp = 0.05;  // ← Was 0.08, too aggressive
Kd = 0.03;  // ← Add damping
```

### Problem: Robot too slow, creeps to target

```java
// Increase Kp
Kp = 0.08;  // ← Was 0.05, too conservative
```

### Problem: Robot oscillates (vibrates)

```java
// Decrease Kp, increase Kd significantly
Kp = 0.04;   // ← Reduce sensitivity
Kd = 0.05;   // ← Increase damping
```

### Problem: Robot stops short

```java
// Increase MIN_POWER (friction compensation)
MIN_POWER = 0.18;  // ← Was 0.15, too weak
```

---

## Field Coordinates Reference

```
STANDARD FTC FIELD (144" × 144")

(0, 144) ─────────────── (144, 144)
  │  Blue Team            │
  │  Left Corner          │
  │                       │
  │                       │
(0, 0) ────────────────── (144, 0)
  Red Team
  Right Corner

Orient robot at (0,0) facing (0,144) for standard setup
```

---

## Telemetry Patterns

### Debug Odometry

```java
while (opModeIsActive()) {
    robot.localiser.update();
    var pose = robot.localiser.getPose();
    telemetry.addData("X (in)", pose.getX());
    telemetry.addData("Y (in)", pose.getY());
    telemetry.addData("θ (deg)", Math.toDegrees(pose.getHeading()));
    telemetry.update();
}
```

### Debug Motor Directions

```java
// Test each motor individually
robot.drive(0.3, 0, 0);  // Should move forward
telemetry.addLine("Forward (should be +X)");

robot.drive(0, 0.3, 0);  // Should move right
telemetry.addLine("Right (should be +Y)");

robot.drive(0, 0, 0.3);  // Should rotate CCW
telemetry.addLine("Rotate (should be +θ)");
```

### Debug Waypoint Execution

```java
var path = Waypoint.at(0, 0)
        .at(24, 24)
            .onReach(() -> {
                telemetry.addLine("REACHED (24, 24)");
                telemetry.update();
                robot.depositSample();
            })
        .buildAll();

for (var wp : path) {
    telemetry.addData("Waypoint", "(" + wp.x + ", " + wp.y + ")");
}
telemetry.update();
```

---

## Troubleshooting Decision Tree

```
Does robot move at all?
├─ NO  → Check hardware names, motor inversions, battery voltage
└─ YES → Continue

Does odometry update when robot moves?
├─ NO  → Check encoder names, encoder inversions, I2C cables
└─ YES → Continue

Does robot drive straight without drifting?
├─ NO  → Tune TRACK_WIDTH via TuneOdometry OpMode
└─ YES → Continue

Do position commands (drivePID, strafePID) reach target?
├─ NO  → Tune Kp/Kd via Dashboard (see PID Tuning Pattern above)
└─ YES → Continue

Does pure pursuit path execute smoothly?
├─ NO  → Adjust DEFAULT_FOLLOW_DISTANCE in Dashboard
└─ YES → Ready for competition!
```

---

## Copy-Paste: Speed Profiles

```java
// SPEED PROFILE 1: Conservative (Safe)
.speed(0.5).turnSpeed(0.3)

// SPEED PROFILE 2: Balanced (Default)
.speed(0.7).turnSpeed(0.4)

// SPEED PROFILE 3: Aggressive (Fast)
.speed(0.85).turnSpeed(0.5)

// SPEED PROFILE 4: Precision (Slow)
.speed(0.3).turnSpeed(0.2)
```

---

## Dashboard JSON Export Template

After tuning, export config from Dashboard:

```json
{
  "Odometry": {
    "TRACK_WIDTH": 13.2,
    "CENTER_WHEEL_OFFSET": 3.4,
    "WHEEL_DIAMETER": 1.37795,
    "TICKS_PER_REV": 2000
  },
  "RobotOriented": {
    "Kp": 0.07,
    "Ki": 0.0,
    "Kd": 0.03,
    "strafe_Kp": 0.07,
    "strafe_Ki": 0.0,
    "strafe_Kd": 0.03,
    "STEER_P": 0.05,
    "STEER_I": 0.0,
    "STEER_D": 0.02,
    "MIN_POWER": 0.15
  }
}
```

Save this after successful tuning — useful for next season or second robot.

---

## Useful Conversions

```java
// Degrees ↔ Radians
double radians = Math.toRadians(degrees);
double degrees = Math.toDegrees(radians);

// Example: 90° in radians
double angle = Math.toRadians(90);  // = 1.5708 radians

// Common angles
0° = 0 rad
90° = π/2 ≈ 1.57 rad
180° = π ≈ 3.14 rad
270° = 3π/2 ≈ 4.71 rad
```

---

## Performance Targets

| Metric | Target | Good | Excellent |
|--------|--------|------|-----------|
| Odometry accuracy | ±2" per 100" | ±1.5" | ±0.5" |
| PID response time | < 1 sec to target | 0.5-1 sec | < 0.5 sec |
| Pure pursuit overshoot | < 3" | 1-2" | < 1" |
| Autonomous consistency | ±2 sec | ±1.5 sec | ±0.5 sec |
| Callback timing | Within ±1" | Within ±0.5" | < 0.5" |

---

## Communication & Debugging

### Print diagnostics to telemetry

```java
telemetry.addLine("=== DIAGNOSTICS ===");
telemetry.addData("Battery", hardwareMap.voltageSensor.get("Control Hub").getVoltage());
telemetry.addData("OpMode active", opModeIsActive());
telemetry.addData("Current Pose", robot.localiser.getPose());
telemetry.update();
```

### Check logcat for exceptions

```bash
adb logcat | grep "Exception\|Error"
```

### Enable FTC Dashboard telemetry

```java
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;

TelemetryPacket packet = new TelemetryPacket();
packet.put("Robot X", robot.localiser.getPose().getX());
// Send to Dashboard...
```

---

## Next: Where to Go From Here

- **Still struggling with setup?** → Return to [Getting Started & Project Setup](01-getting-started.md)
- **Odometry not accurate?** → Go to [Understanding Odometry Troubleshooting](02-understanding-odometry.md#common-pitfalls)
- **Need PID details?** → See [PID Tuning Section](03-robot-configuration.md#pid-tuning)
- **Building complex paths?** → Study [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md#real-world-autonomous-sequences)

---

## Pro Tips

1. **Always measure twice** — 0.1" error in track width causes 1-2% odometry drift
2. **Create two robot configurations** — competition vs practice (different battery voltage affects tuning)
3. **Log telemetry during tuning** — save robot logcat, identify patterns in failures
4. **Test on actual competition field** — carpet vs tile changes friction significantly
5. **Backup your tuned values** — export `RobotConfig` JSON after successful tuning
6. **Start conservative** — low Kp values, increase until good performance
7. **Parallel subsystem actions** — fire `onReach` callbacks early to spin up actuators

---

## Emergency Quick Fixes

| Symptom | 1-Minute Fix |
|---------|-------------|
| Autonomous doesn't start | Check `waitForStart()` is present, press PLAY |
| Motors don't respond | check device names in MyRobot.java |
| Encoders won't update | Verify I2C cable connections |
| Robot goes backwards | Invert motor: `.frontLeftInverted(true)` |
| Path doesn't reach waypoint | Decrease follow distance in Dashboard |
| Callback doesn't fire | Ensure `.onReach(() -> { })` is called |

---

## Resource Links

- **FTC SDK Documentation** — https://ftc-docs.firstinspires.org/
- **FTC Dashboard** — https://acmerobotics.org/dashboard/
- **FTCLib Javadoc** — https://github.com/FTC-Team-15314/FTCLib
- **Crawler GitHub** — https://github.com/CreepyCrawlies16771/Crawler

---

Print this page or bookmark it. You'll reference it constantly.
