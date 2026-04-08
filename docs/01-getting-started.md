# Getting Started & Project Setup

**Objective:** Set up Crawler in your FTC project, create your robot subclass, and run your first autonomous routine in under 30 minutes.

---

## Prerequisites

Before you begin, ensure you have:

- **Android Studio 2024.2 (Ladybug) or later** with FTC SDK imported
- **FtcLib libraries** (included in the FTC SDK dependency)
- **FTC Dashboard 0.4.16 or later** installed via gradle
- **Basic Java knowledge** — inheritance, interfaces, generics
- **A configured FTC robot** with:
  - **4 drive motors** (front-left, front-right, back-left, back-right) — any orientation
  - **IMU** (Rev Robotics Expansion Hub IMU)
  - **3 dead wheel encoders** (left, right, center) — or alternative localizer (see end of this page)

> **Note:** The primary examples in this documentation assume a **3-dead-wheel odometry system** with **goBILDA odometry pods**. For other localizers, see [Appendix: Alternative Localizers](#appendix-alternative-localizers).

---

## Step 1: Verify Your Dependencies

Ensure your project's `build.dependencies.gradle` includes:

```gradle
dependencies {
    // FTC SDK (already included)
    implementation 'org.firstinspires.ftc:robobase:9.1.0'
    implementation 'org.firstinspires.ftc:ftclib:2.1.0'

    // FTC Dashboard for real-time tuning
    implementation 'com.acmerobotics.dashboard:dashboard:0.4.16'

    // Optional: GoBilda Pinpoint for alternative localization
    // implementation 'com.goBILDA:Pinpoint:1.0'
}
```

If any are missing, add them and sync gradle.

---

## Step 2: Understanding Project Structure

The Crawler library is located in your `TeamCode/` directory:

```
TeamCode/
└── src/main/java/org/firstinspires/ftc/teamcode/Crawler/
    ├── core/
    │   ├── Robot/CrawlerRobot.java          ← Core hardware abstraction
    │   ├── Localizers/                      ← Odometry & localization
    │   ├── RobotConfig.java                 ← ALL constants (tunable via Dashboard)
    │   └── utils/                           ← Math helpers, Waypoint builders
    ├── RobotOrient/                         ← Imperative movement (drivePID, etc.)
    ├── FieldOrient/                         ← Pure pursuit path follower
    ├── Tuning/                              ← Calibration OpModes (run these first!)
    └── MyRobot.java                         ← YOUR robot subclass (you create this)
```

**Key insight:** Most code is in `CrawlerRobot` and the movement engines. Your job is to:
1. Create `MyRobot.java` (hardware configuration)
2. Create autonomous OpModes that use your `MyRobot` instance
3. Run the tuning OpModes in `Tuning/` to calibrate

---

## Step 3: Create Your Robot Class (MyRobot.java)

The `CrawlerRobot` class uses a **builder pattern** to initialize hardware. You must create a concrete subclass that implements the `Builder`:

```java
package org.firstinspires.ftc.teamcode.Crawler;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Concrete robot implementation for your team's hardware.
 * 
 * This class knows the names of all hardware devices in your configuration,
 * and builds a CrawlerRobot with those names. It also applies any team-specific
 * inversions or calibrations.
 */
public class MyRobot {

    public static CrawlerRobot build(HardwareMap hwMap) {
        return CrawlerRobot.builder(hwMap)
                // Drive motors — match your hardware configuration names
                .frontLeftName("frontLeft")
                .frontRightName("frontRight")
                .backLeftName("backLeft")
                .backRightName("backRight")
                
                // Motor inversions — adjust if your direction is backwards
                .frontLeftInverted(false)
                .frontRightInverted(true)    // Often one side needs inversion
                .backLeftInverted(false)
                .backRightInverted(true)
                
                // Localization: 3-dead-wheel encoders
                .localisation(CrawlerRobot.Localisation.ThreeDeadWheel)
                .leftEncoderName("enc_l")
                .rightEncoderName("enc_r")
                .centerEncoderName("enc_c")
                .leftEncoderInverted(false)
                .rightEncoderInverted(false)
                .centerEncoderInverted(false)
                
                // IMU
                .imuName("imu")
                
                // Physical constants — MUST measure these!
                .trackWidth(13.0)          // inches, side-to-side encoder spacing
                .centerWheelOffset(3.5)    // inches, center wheel offset from drive wheels's center
                
                .build();
    }
}
```

### Important Configuration Values

These **must be accurate** for odometry to work:

| Parameter | Example | What It Is |
|-----------|---------|-----------|
| `trackWidth` | 13.0" | Distance between left and right encoder wheels (side-to-side) |
| `centerWheelOffset` | 3.5" | Forward/backward distance of center encoder from the line connecting left/right encoders |
| Hardware names | "frontLeft" | Device names **exactly as they appear in your configuration** |
| Motor inversions | `true/false` | Does the motor spin backwards? Observe and correct. |
| Encoder inversions | `true/false` | Do positive encoder ticks go backward? If so, set `true`. |

> **Warning:** Starting with incorrect `trackWidth` or `centerWheelOffset` values is the #1 cause of "odometry doesn't track straight" complaints. We'll tune these in a dedicated tuning step.

---

## Step 4: Create Your First Autonomous OpMode

Create a new file in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`:

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Crawler.MyRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

/**
 * Simple autonomous routine: move forward 24 inches, turn 90 degrees.
 */
@Autonomous(name = "Simple Path", group = "Crawler")
public class SimplePathAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Create and initialize robot
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

        telemetry.addLine("Robot initialized. Press PLAY to start.");
        telemetry.update();
        
        waitForStart();

        if (opModeIsActive()) {
            // 2. Build waypoint path: (x=24", y=0") with default speed
            var path = Waypoint.at(0, 0)
                    .at(24, 0)
                    .buildAll();

            // 3. Follow the path
            telemetry.addLine("Following path...");
            telemetry.update();
            follower.follow(path, 0);  // followAngle = 0 radians (pointing down field)

            telemetry.addLine("Path complete!");
            telemetry.update();
        }
    }
}
```

**What's happening:**
1. **Line 13:** We build the robot using `MyRobot.build()` — this initializes all hardware, encoders, IMU, and localization
2. **Line 14:** We create a `FOFollower` (field-oriented follower) that will guide the robot along waypoints
3. **Line 18:** We build a simple 2-waypoint path: start at (0,0), end at (24,0) — a straight line forward
4. **Line 22:** We call `follower.follow()` to execute the path — this **blocks** until complete

---

## Step 5: First Run — Hardware Verification

**Before tuning, verify hardware communication:**

1. **Create a diagnostic OpMode:**

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Crawler.MyRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@Autonomous(name = "Hardware Check", group = "Diagnostics")
public class HardwareCheckAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        try {
            CrawlerRobot robot = MyRobot.build(hardwareMap);
            
            telemetry.addLine("✓ Robot initialized successfully");
            telemetry.addLine("Motors: frontLeft, frontRight, backLeft, backRight");
            telemetry.addLine("Encoders: enc_l, enc_r, enc_c");
            telemetry.addLine("IMU: imu");
            telemetry.addLine("\nPress PLAY to test motors...");
            telemetry.update();
            
            waitForStart();
            
            if (opModeIsActive()) {
                // Test each motor at 0.3 power for 0.5 seconds
                telemetry.addLine("Testing drive motors...");
                telemetry.update();
                
                robot.drive(0.3, 0, 0); // forward
                sleep(500);
                robot.stop();
                sleep(200);
                
                robot.drive(0, 0.3, 0); // strafe right
                sleep(500);
                robot.stop();
                sleep(200);
                
                robot.drive(0, 0, 0.3); // rotate
                sleep(500);
                robot.stop();
                
                telemetry.addLine("✓ All motors responsive");
                telemetry.addLine("Odometry pose: " + robot.localiser.getPose());
                telemetry.update();
            }
        } catch (Exception e) {
            telemetry.addLine("ERROR: " + e.getMessage());
            telemetry.update();
        }
    }
}
```

2. **Upload and run this OpMode** — you should see the robot twitch forward, strafe, and rotate
3. **Check logcat** for errors like "Device not found: enc_l"
4. **If hardware names are wrong**, return to `MyRobot.java` and fix them to match your configuration

---

## Step 6: Odometry Calibration (5 minutes)

Once hardware is verified, you **must** calibrate odometry before running autonomous:

1. **Open FTC Dashboard** (see [Robot Configuration & FTC Dashboard](03-robot-configuration.md#setting-up-ftc-dashboard))
2. **Run the tuning OpMode** `TuneOdometry` from the `Tuning/` package
3. **Drive forward 12 inches exactly**, measuring with a ruler
4. **Adjust `Odometry.WHEEL_DIAMETER` in RobotConfig** until reported distance matches

See [Odometry Tuning](02-understanding-odometry.md#odometry-tuning) for step-by-step calibration.

---

## Step 7: First Autonomous Run

Once hardware and odometry are verified:

1. **Upload your `SimplePathAuto` OpMode**
2. **Place the robot on the field** at position (0,0)
3. **Press PLAY** — the robot should drive forward ~24 inches
4. **Observe the path** on FTC Dashboard — it should be a straight line

**If the robot curves or drifts:**
- See [Troubleshooting Odometry](02-understanding-odometry.md#common-pitfalls)
- See [PID Tuning](03-robot-configuration.md#pid-tuning)

---

## Appendix: Alternative Localizers

Crawler supports multiple localization backends. If you don't have 3 dead wheels:

### Option 1: 2-Dead-Wheel (Center + One Side)

```java
// In MyRobot.build():
.localisation(CrawlerRobot.Localisation.TwoDeadWheel)
.leftEncoderName("enc_l")        // Only left and center needed
.centerEncoderName("enc_c")
```

Less accurate than 3-wheel but simpler mechanically. Requires careful `centerWheelOffset` tuning.

### Option 2: GoBilda Pinpoint (Absolute Position Sensor)

```java
// In MyRobot.build():
.localisation(CrawlerRobot.Localisation.Pinpoint)
.pinpointDeviceName("pinpoint")
.pinpointXOffset(3.0)    // inches from robot center
.pinpointYOffset(0.0)
```

No need to tune `trackWidth` or `centerWheelOffset` — Pinpoint knows absolute position. See [Pinpoint Integration](02-understanding-odometry.md#alternative-localizers-pinpoint) for details.

### Option 3: Motor Encoders (Fallback)

```java
// In MyRobot.build():
.localisation(CrawlerRobot.Localisation.MotorEncoder)
```

Uses drive motor encoders for odometry. Least accurate (due to wheel slip) but always available. No extra hardware needed.

### Option 4: Static Pose (Development Only)

```java
// In MyRobot.build():
.localisation(CrawlerRobot.Localisation.DevLocaliser)
```

Robot reports a fixed position (0,0,0). Use for testing without real localization.

---

## Next Steps

Once hardware and odometry are verified:

1. **Choose your autonomous strategy:**
   - Simple straight-line moves → [Robot-Oriented Movement Commands](04-robot-oriented-movement.md)
   - Complex path-following → [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md)

2. **Understand configuration constants** → [Robot Configuration & FTC Dashboard](03-robot-configuration.md)

3. **Deep dive into odometry mechanics** → [Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md)

---

## Common First-Time Mistakes

| Problem | Cause | Fix |
|---------|-------|-----|
| "Device not found: frontLeft" | Hardware name mismatch | Match device names in `MyRobot.java` to your configuration |
| Robot doesn't move | Motor inversions incorrect | Test each motor, set inversions in `MyRobot.build()` |
| Odometry reports 0,0,0 always | Encoder not configured | Verify encoder names in `MyRobot.build()` |
| Path is curved, not straight | Track width incorrect | Run `TuneOdometry` OpMode, adjust `Odometry.TRACK_WIDTH` |
| OpMode doesn't start | `waitForStart()` never returns | Check that you pressed PLAY correctly |

---

## Getting Help

- **Hardware issues** → Check your device names in Android Studio's "Run" → "Device File Explorer" → `/sdcard/FIRST/` 
- **Encoder issues** → Enable telemetry: `telemetry.addData("Pose", robot.localiser.getPose())` and observe raw values
- **Path doesn't execute** → Check FTC Dashboard real-time telemetry — is the robot reaching waypoints?

Move to [Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md) when ready to dive deeper.
