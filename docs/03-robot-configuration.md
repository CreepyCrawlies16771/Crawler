# Robot Configuration & FTC Dashboard

**Objective:** Understand `RobotConfig.java`, configure hardware names and physical constants, set up FTC Dashboard for real-time tuning, and dial in PID parameters without code changes.

---

## Overview: Where All Constants Live

Crawler follows a **single source of truth** principle: all configurable constants live in `RobotConfig.java`. This file contains:

- **Hardware names** — device names from your configuration.json
- **Physical constants** — measured robot dimensions (track width, wheel diameter)
- **PID tuning values** — controller gains for movement
- **Speed presets** — default movement speeds

The key feature: most values are annotated with `@Config`, which makes them **editable in real-time via FTC Dashboard** — no recompilation needed.

---

## RobotConfig.java Structure

Open `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/core/RobotConfig.java`:

```java
package org.firstinspires.ftc.teamcode.Crawler.core;

import com.acmerobotics.dashboard.config.Config;

/**
 * Centralized configuration for all Crawler robots.
 * 
 * Most values are annotated @Config and tunable via FTC Dashboard
 * without code recompilation. Values are read at runtime.
 */
public class RobotConfig {

    // -----------------------------------------------------------------------
    // Hardware Names (static, not @Config — set once at build time)
    // -----------------------------------------------------------------------
    static String FRONT_LEFT     = "frontLeft";
    static String FRONT_RIGHT    = "frontRight";
    static String BACK_LEFT      = "backLeft";
    static String BACK_RIGHT     = "backRight";
    static String IMU_NAME       = "imu";
    static String LEFT_ENCODER   = "enc_l";
    static String RIGHT_ENCODER  = "enc_r";
    static String CENTER_ENCODER = "enc_c";

    // -----------------------------------------------------------------------
    // Tunable Constants (marked @Config for FTC Dashboard)
    // -----------------------------------------------------------------------
    
    @Config
    public static class Odometry {
        public static double TRACK_WIDTH          = 13.0;
        public static double CENTER_WHEEL_OFFSET  = 3.5;
        public static double WHEEL_DIAMETER       = 1.37795;
        public static double TICKS_PER_REV        = 2000;
    }

    @Config
    public static class RobotOriented {
        // PID gains for driving straight
        public static double Kp        = 0.05;
        public static double Ki        = 0.0;
        public static double Kd        = 0.0;
        
        // PID gains for strafing
        public static double strafe_Kp = 0.05;
        public static double strafe_Ki = 0.0;
        public static double strafe_Kd = 0.0;
        
        // PID gains for rotation
        public static double STEER_P   = 0.03;
        public static double STEER_I   = 0.0;
        public static double STEER_D   = 0.0;
        
        // Minimum power to overcome friction
        public static double MIN_POWER = 0.15;
    }

    @Config
    public static class FieldOriented {
        // Path following speeds
        public static double DEFAULT_MOVE_SPEED             = 0.7;
        public static double DEFAULT_TURN_SPEED             = 0.4;
        public static double DEFAULT_FOLLOW_DISTANCE        = 10.0;
        
        // Thresholds for arrival detection
        public static double ARRIVAL_THRESHOLD              = 2.0;
        public static double ORBIT_THRESHOLD                = 10.0;
        
        // Slow mode (used for precision placement)
        public static double SLOW_MOVE_SPEED                = 0.3;
        public static double SLOW_TURN_SPEED                = 0.2;
    }

    public static class RobotBase {
        public static double MAX_DRIVE_SPEED = 1.0;
        public static double MIN_DRIVE_SPEED = 0.1;
    }
}
```

### Key Sections

| Section | Purpose | Tunable? |
|---------|---------|----------|
| **Hardware Names** | Device names from configuration.json | ❌ No — set in `MyRobot.java` |
| **Odometry** | Track width, wheel diameter, ticks/rev | ✅ Yes — calibrate via tuning OpModes |
| **RobotOriented** | PID gains for imperative movement | ✅ Yes — tune via FTC Dashboard |
| **FieldOriented** | Path follower speeds and thresholds | ✅ Yes — adjust for your strategy |
| **RobotBase** | Safety limits and physics constants | ⚠️ Rarely changed |

---

## Configuring Hardware Names

Hardware names are **not directly in `RobotConfig.java`** — they're passed through `MyRobot.build()`. This separation means different robots can have different hardware names.

### Setting Hardware Names

In `MyRobot.java` (your robot subclass):

```java
public class MyRobot {
    public static CrawlerRobot build(HardwareMap hwMap) {
        return CrawlerRobot.builder(hwMap)
                // --- DRIVE MOTORS ---
                // Match these exactly to your configuration.json device names
                .frontLeftName("frontLeft")     // Check your config!
                .frontRightName("frontRight")
                .backLeftName("backLeft")
                .backRightName("backRight")
                
                // --- ENCODERS (for odometry) ---
                .leftEncoderName("enc_l")
                .rightEncoderName("enc_r")
                .centerEncoderName("enc_c")
                
                // --- IMU ---
                .imuName("imu")
                
                .build();
    }
}
```

### Verifying Your Hardware Names

To see what devices are actually available:

1. **Open Android Studio** → Device File Explorer
2. Navigate to `/data/system/` and look for `BuiltinDeviceManager.json`
3. Or use **Rev Hardware Client** to list all configured devices
4. Match device names exactly

---

## Odometry Constants

These require **careful measurement** before autonomous runs.

### WHEEL_DIAMETER

The diameter of your encoder wheels.

**goBILDA Standard:** 1.37795 inches (35mm wheels)

**To measure:**
1. Use calipers to measure your wheel diameter
2. Record in `RobotConfig.Odometry.WHEEL_DIAMETER`

**Typical values:**
- goBILDA 35mm: 1.37795"
- AM 1.6" Wheel: 1.6"
- Custom: measure with calipers

> **Warning:** Even 0.01" error here causes ~1% odometry error per 100 inches. Measure carefully!

### TRACK_WIDTH

The distance between left and right encoder wheels (side-to-side).

**How to measure:**
1. Place robot on flat surface
2. Measure center-to-center distance of left and right encoder wheels
3. Use ruler or calipers
4. Record in `RobotConfig.Odometry.TRACK_WIDTH`

**Example:** If centers are 13.2 inches apart:
```java
// In RobotConfig.java
@Config
public static class Odometry {
    public static double TRACK_WIDTH = 13.2;  // ← Your measurement
}
```

**Fine-tuning via drive tests:**
- Robot curves **right** when driving straight → **increase** `TRACK_WIDTH`
- Robot curves **left** when driving straight → **decrease** `TRACK_WIDTH`

Adjust in 0.1" increments and re-test until no curve.

### CENTER_WHEEL_OFFSET

The **forward/backward distance** of the center wheel from the plane of the left/right wheels.

**How to measure:**
1. Draw an imaginary line connecting the centers of left and right encoder wheels
2. Measure the perpendicular distance from center encoder wheel to that line
3. Positive = center forward; Negative = center backward
4. Record in `RobotConfig.Odometry.CENTER_WHEEL_OFFSET`

**Example mounting:**
```
    Imaginary line (L-R line)
    ←───────────────────→
       ↑ 3.5 inches
       |
      [C] ← center wheel position

CENTER_WHEEL_OFFSET = +3.5"
```

**Fine-tuning:**
- If robot drifts **right** during straight-line movement → decrease `CENTER_WHEEL_OFFSET`
- If robot drifts **left** → increase `CENTER_WHEEL_OFFSET`

Adjust in 0.2" increments.

### TICKS_PER_REV

Encoder ticks per complete revolution.

**Standard values:**
- REV Through-Bore Encoder: 2000 ticks
- GoBILDA Yellow Encoders: 1440 ticks
- AM Mag Encoder: 8192 ticks

Check your encoder datasheet — rarely needs adjustment.

---

## PID Tuning

PID (Proportional-Integral-Derivative) control is used by:
- **Robot-Oriented Movement** (`drivePID`, `strafePID`, `turnPID`)
- **Pure Pursuit** (implicit — uses velocity targets)

### Understanding PID Gains

| Gain | Effect | Too High | Too Low |
|------|--------|----------|---------|
| **Kp** (Proportional) | How hard to push toward target | Oscillates, overshoot | Slow, never reaches |
| **Ki** (Integral) | Correction for steady-state error | Wild oscillation | No correction over time |
| **Kd** (Derivative) | Damping, reduces oscillation | Sluggish | Overshoot, bouncing |

**Typical tuning priority:** `Kp` first, then `Kd`, rarely need `Ki`.

### Initial PID Values (Defaults)

```java
@Config
public static class RobotOriented {
    // Forward/backward movement
    public static double Kp        = 0.05;   // 5% power per 1 inch error
    public static double Ki        = 0.0;
    public static double Kd        = 0.0;
    
    // Strafing (left/right)
    public static double strafe_Kp = 0.05;
    public static double strafe_Ki = 0.0;
    public static double strafe_Kd = 0.0;
    
    // Rotation (turning)
    public static double STEER_P   = 0.03;
    public static double STEER_I   = 0.0;
    public static double STEER_D   = 0.0;
    
    // Friction compensation
    public static double MIN_POWER = 0.15;
}
```

---

## Setting Up FTC Dashboard

FTC Dashboard allows you to **change `@Config` values in real-time** without recompiling.

### Step 1: Install FTC Dashboard

In your `build.dependencies.gradle`:

```gradle
dependencies {
    implementation 'com.acmerobotics.dashboard:dashboard:0.4.16'
}
```

Sync gradle.

### Step 2: Access Dashboard

1. **Build and upload your code to the robot**
2. Connect via ADB (USB or WiFi):
   ```bash
   adb connect <robot_ip>:5005
   ```
3. **Open web browser** and go to:
   ```
   http://<robot_ip>:8080
   ```
4. You should see FTC Dashboard with a **Configuration** tab

### Step 3: Edit Configuration

In the "Configuration" tab:

1. Look for `RobotConfig` class
2. You should see sections:
   - `Odometry`
   - `RobotOriented`
   - `FieldOriented`
3. Edit values (e.g., `Kp = 0.08`)
4. Click **SAVE** (bottom right)

**Important:** After saving, the next time your OpMode reads these values, it gets the new number. No restart needed.

### Step 4: Viewing Real-Time Telemetry

Run an autonomous OpMode:

```java
@Autonomous(name = "Tuning Test", group = "Tuning")
public class TuningTestAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        
        telemetry.addLine("Loaded config:");
        telemetry.addData("Kp", RobotConfig.RobotOriented.Kp);
        telemetry.addData("Track Width", RobotConfig.Odometry.TRACK_WIDTH);
        telemetry.addLine("Press PLAY to test movement...");
        telemetry.update();
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Your movement test code here
            robot.drive(0.5, 0, 0);
            sleep(1000);
            robot.stop();
        }
    }
}
```

While this runs, **change values in Dashboard** and see them take effect in real-time.

---

## Field-Oriented Tuning

The pure pursuit follower uses these constants from `RobotConfig.FieldOriented`:

### DEFAULT_MOVE_SPEED

How fast the robot moves along waypoints.

- **0.7** = 70% power (good default starting point)
- **0.5** = slower, more controlled
- **0.9** = aggressive, but may overshoot

**Tune based on field:**
- Small practice field → increase to 0.8-0.9
- Large competition field → keep at 0.7
- Dense obstacle course → decrease to 0.5

### DEFAULT_TURN_SPEED

How fast the robot rotates to face waypoints.

- **0.4** = 40% rotation power (good default)
- **0.2** = very smooth turns
- **0.6** = fast but may overshoot heading

### DEFAULT_FOLLOW_DISTANCE

How far ahead of waypoints the robot looks.

- **10.0 inches** = normal, smooth following
- **15.0** = smoother curves (robot anticipates turns earlier)
- **5.0** = tight, jerky paths

**Increase if:** Following is jerky, robot bounces between waypoints  
**Decrease if:** Robot cuts corners or misses waypoints

### ARRIVAL_THRESHOLD

Distance threshold for "waypoint reached."

- **2.0 inches** = must get within 2" of waypoint before moving to next
- **1.0** = tighter tolerance, slower progress
- **3.0** = faster progress but less accurate

> **Recommendation:** Leave at **2.0 inches** for most cases.

### ORBIT_THRESHOLD

Distance at which robot stops rotating and orbits to waypoint.

- **10.0 inches** = at 10" away, heading errors fade and robot just moves toward waypoint
- Makes final approach smoother, doesn't get stuck fighting heading

---

## Advanced: Custom Robot Subclass with Modified Config

If you have multiple robots or want per-robot tuning:

```java
/**
 * Competition robot with production tuning
 */
public class ProductionRobot {
    public static CrawlerRobot build(HardwareMap hwMap) {
        CrawlerRobot robot = new CrawlerRobot.Builder(hwMap)
            .frontLeftName("frontLeft")
            .frontRightName("frontRight")
            // ... other config ...
            .build();
        
        // Override defaults before first autonomous
        RobotConfig.RobotOriented.Kp = 0.07;   // Tuned for this robot
        RobotConfig.Odometry.TRACK_WIDTH = 12.8;
        
        return robot;
    }
}

/**
 * Practice/testing robot with looser tuning
 */
public class PracticeRobot {
    public static CrawlerRobot build(HardwareMap hwMap) {
        CrawlerRobot robot = new CrawlerRobot.Builder(hwMap)
            .frontLeftName("practiceFront_L")
            // ... different hardware names ...
            .build();
        
        RobotConfig.RobotOriented.Kp = 0.05;   // More conservative
        
        return robot;
    }
}
```

---

## Troubleshooting Configuration

### "Device not found: frontLeft"

```
Exception: Device not found: frontLeft
    at com.qualcomm.robotcore.hardware.HardwareFactory.getInstance
```

**Cause:** Hardware name in `MyRobot.java` doesn't match configuration.json

**Fix:**
1. Open Rev Hardware Client
2. Check exact device names
3. Update `MyRobot.java` to match exactly (case-sensitive)

### "Odometry doesn't update"

**Cause:** Encoder names wrong or not configured

**Fix:**
```java
// In MyRobot.build():
.leftEncoderName("enc_l")    // ← Verify these exist in config
.rightEncoderName("enc_r")
.centerEncoderName("enc_c")
```

### "Dashboard shows config but values don't change during run"

**Cause:** OpMode caches values at startup

**Fix:** Read values dynamically:
```java
// Inside your movement loop:
double kp = RobotConfig.RobotOriented.Kp;  // ← Reads fresh each iteration
// Use kp for PID calculation
```

Not:
```java
// At startup (wrong):
double kp = RobotConfig.RobotOriented.Kp;  // ← Cached once
// Later, even if Dashboard changes value, this 'kp' stays same
```

### "Changing Dashboard values doesn't affect movement"

**Cause:** Movement system not reading from `RobotConfig` after each change

**Fix:**
1. Make sure your `OpMode` is still running (not stopped)
2. Give Dashboard **3-5 seconds** after clicking SAVE
3. Check that telemetry shows the new value: `telemetry.addData("Kp", RobotConfig.RobotOriented.Kp);`

---

## Configuration Checklist

Before autonomous:

- [ ] Hardware names verified in Android Studio Device File Explorer
- [ ] `MyRobot.java` created with correct device names
- [ ] `WHEEL_DIAMETER` measured (goBILDA = 1.37795")
- [ ] `TRACK_WIDTH` measured (center-to-center of perpendicular wheels)
- [ ] `CENTER_WHEEL_OFFSET` measured (forward distance of center wheel)
- [ ] Odometry calibration completed (tuning OpMode run and tested)
- [ ] FTC Dashboard installed and accessible
- [ ] Dashboard can read/write `RobotConfig` values
- [ ] `Kp`, `Ki`, `Kd` set to reasonable starting values
- [ ] First autonomous test run and tuned via Dashboard

---

## Next Steps

- **Tune PID in more detail** → [Robot-Oriented Movement Commands](04-robot-oriented-movement.md#pid-tuning)
- **Test movement with configuration** → [Robot-Oriented Movement Commands](04-robot-oriented-movement.md)
- **Follow pre-planned paths** → [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md)

Your configuration is complete when autonomous drives straight, stops accurately, and follows paths smoothly.
