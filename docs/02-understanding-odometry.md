# Understanding Odometry: 3-Dead-Wheel System

**Objective:** Understand how the 3-dead-wheel odometry system tracks the robot's position, implement accurate track width and center wheel offset calibration, and troubleshoot common odometry issues.

---

## What is Dead Wheel Odometry?

**Dead wheels** (also called "odometry wheels" or "tracking wheels") are passive, unpowered encoder wheels that measure robot movement without driving the robot. Unlike drive motor encoders, dead wheels are immune to wheel slip caused by acceleration or uneven field conditions.

A **3-dead-wheel system** uses three wheels positioned strategically:
- **Left wheel** — measures leftward/rightward movement (perpendicular to forward motion)
- **Right wheel** — also measures leftward/rightward movement (parallel to left wheel)
- **Center wheel** — measures forward/backward movement (perpendicular to left/right wheels)

By combining these three measurements with the **robot's heading** (from the IMU), Crawler calculates an accurate 2D position: `(x, y, heading)`.

> **Key insight:** Dead wheels are **physically separate from drive wheels**, so field friction and motor acceleration don't affect odometry accuracy.

---

## Hardware Setup

### Mechanical Layout (Top-Down View)

```
        FRONT
          ↑
          |
    [C]   |   [↑ Heading]
     ↑    |    ↑
     |    |    |
  [L]←----+----→[R]
     ←(trackWidth)→

[L] = Left encoder wheel (measures perpendicular motion)
[R] = Right encoder wheel (measures perpendicular motion)  
[C] = Center encoder wheel (measures forward/backward motion)
     positioned offset forward from the L-R line
```

### Common goBILDA Configuration

Most FTC teams use **goBILDA odometry pods** (35mm wheels with encoders):

| Component | Typical Value |
|-----------|--------------|
| Wheel diameter | 1.37795 inches (35mm) |
| Encoder type | REV Through-Bore Encoder |
| Encoder ticks/revolution | 2000 ticks |
| Pod mounting | Side-mounted on chassis |

---

## Physics: How Odometry Calculates Position

The **HolonomicOdometry** system (from FTCLib) tracks position using:

1. **Encoder displacements** — each encoder measures how far it has rolled since last update
2. **Track width** — distance between left and right encoder wheels
3. **Center wheel offset** — forward/backward position of center wheel relative to left/right line
4. **IMU heading** — robot's absolute rotation angle

### The Math (Simplified)

For a holonomic drivetrain:

$$\Delta x = \frac{(\Delta r + \Delta l)}{2} \cdot \cos(\theta) - \Delta c \cdot \sin(\theta)$$

$$\Delta y = \frac{(\Delta r + \Delta l)}{2} \cdot \sin(\theta) + \Delta c \cdot \cos(\theta)$$

Where:
- $\Delta l$, $\Delta r$, $\Delta c$ = displacements from left, right, and center encoders (inches)
- $\theta$ = robot heading (radians) from IMU
- $\Delta x, \Delta y$ = change in field position

**Key takeaway:** Odometry is a **composition of three independent measurements**:
1. How far do the perpendicular wheels move (left + right)?
2. How far does the forward wheel move (center)?
3. How much did the robot rotate (IMU)?

If any of these three are **wrong**, position tracking fails.

---

## Implementing 3-Dead-Wheel Odometry

### Step 1: Wire Your Encoders

Each dead wheel connects to a **REV Through-Bore Encoder**. These typically plug into the expansion hub's I2C connector:

```
Left Encoder   → Expansion Hub I2C (port 0)
Right Encoder  → Expansion Hub I2C (port 1)
Center Encoder → Expansion Hub I2C (port 2)
```

Or use motor encoder ports if your pod is connected to a `MotorEx` auxilary encoder port.

### Step 2: Configure Hardware Names

In your `configuration.json` (in the Rev Hardware Client):

```json
{
  "devices": [
    {
      "name": "enc_l",
      "type": "REV Through-Bore Encoder",
      "port": 0
    },
    {
      "name": "enc_r",
      "type": "REV Through-Bore Encoder",
      "port": 1
    },
    {
      "name": "enc_c",
      "type": "REV Through-Bore Encoder",
      "port": 2
    }
  ]
}
```

### Step 3: Create ThreeDeadWheelLocaliser

In your autonomous OpMode or robot initialization:

```java
package org.firstinspires.ftc.teamcode.Crawler;

import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizers.ThreeDeadWheelLocaliser;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;

// Inside MyRobot.build(HardwareMap hwMap):
MotorEx leftEncoder = new MotorEx(hwMap, "enc_l");
MotorEx rightEncoder = new MotorEx(hwMap, "enc_r");
MotorEx centerEncoder = new MotorEx(hwMap, "enc_c");

// Create the localiser
CrawlerLocaliser localiser = new ThreeDeadWheelLocaliser(
    leftEncoder, rightEncoder, centerEncoder,
    false, false, false  // inversion flags (adjust if needed)
);

// Now localiser.update() and localiser.getPose() track robot position
```

### Step 4: Integration with CrawlerRobot

You typically **don't** create `ThreeDeadWheelLocaliser` manually. Instead, `CrawlerRobot` handles it:

```java
// In MyRobot.build():
return CrawlerRobot.builder(hwMap)
    .localisation(CrawlerRobot.Localisation.ThreeDeadWheel)
    .leftEncoderName("enc_l")
    .rightEncoderName("enc_r")
    .centerEncoderName("enc_c")
    .build();
```

`CrawlerRobot` internally:
1. Gets the encoder motors from hardware map
2. Creates the `ThreeDeadWheelLocaliser`
3. Stores it in `robot.localiser`
4. Calls `localiser.update()` in your movement loops

---

## Odometry Calibration

### Critical Parameters

Three measurements **must** be accurate:

| Parameter | What It Is | Typical Value | How to Measure |
|-----------|-----------|--------------|-----------------|
| `trackWidth` | Side-to-side distance between left and right encoder wheels | 13.0" | Measure center-to-center of the two perpendicular wheels |
| `centerWheelOffset` | Forward/backward offset of center wheel from left/right line | 3.5" | Measure perpendicular distance from center wheel to the plane of left/right wheels |
| `WHEEL_DIAMETER` | Diameter of encoder wheels | 1.37795" | Measure with calipers (or use pod manual) |

### Calibration Procedure

#### Phase 1: Measure Physical Dimensions

Use a ruler or calipers:

1. **Track Width:**
   - Place the robot upright on a table
   - Measure the **center-to-center distance** of the left and right encoder wheels
   - Record in `RobotConfig.Odometry.TRACK_WIDTH`

   Example: If centers are 13.2" apart, set `TRACK_WIDTH = 13.2`

2. **Center Wheel Offset:**
   - Measure the perpendicular distance from the center wheel to the plane containing the left/right wheels
   - Positive = center wheel forward; Negative = center wheel backward
   - Record in `RobotConfig.Odometry.CENTER_WHEEL_OFFSET`

   Example: If center wheel is 3.4" forward, set `CENTER_WHEEL_OFFSET = 3.4`

#### Phase 2: Tune Via Movement Test

1. **Create a tuning OpMode:**

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Crawler.MyRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@Autonomous(name = "Tune Odometry", group = "Tuning")
public class TuneOdometryAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        
        telemetry.addLine("Place robot at (0,0) facing forward");
        telemetry.addLine("Press PLAY to drive forward 12 inches");
        telemetry.update();
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Drive forward using drive() at half power
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 2000 && opModeIsActive()) {
                robot.drive(0.5, 0, 0); // forward only
                robot.localiser.update();
                
                com.arcrobotics.ftclib.geometry.Pose2d pose = robot.localiser.getPose();
                telemetry.addData("X", pose.getX());
                telemetry.addData("Y", pose.getY());
                telemetry.addData("Heading (rad)", pose.getHeading());
                telemetry.update();
            }
            robot.stop();
            
            // Final position
            com.arcrobotics.ftclib.geometry.Pose2d final_pose = robot.localiser.getPose();
            telemetry.addLine("\n=== FINAL MEASUREMENT ===");
            telemetry.addData("Distance driven (X)", final_pose.getX());
            telemetry.addData("Lateral drift (Y)", final_pose.getY());
            telemetry.addLine("\nExpected X ≈ 12 inches");
            telemetry.addLine("If X is wrong, we need to tune TRACK_WIDTH or WHEEL_DIAMETER");
            telemetry.update();
            
            sleep(5000);
        }
    }
}
```

2. **Physically measure** how far the robot actually moved (use a ruler)

3. **Compare odometry reading to actual distance:**
   - If odometry reads `X = 10"` but robot moved `12"` → `WHEEL_DIAMETER` is too small
   - Adjust: `WHEEL_DIAMETER_new = WHEEL_DIAMETER_old × (actual_distance / reported_distance)`

   Example: If reported 10" but moved 12", then:
   ```
   New WHEEL_DIAMETER = 1.37795 × (12 / 10) = 1.6536 inches
   ```

4. **Repeat** until odometry reading matches actual movement within 0.5 inches

#### Phase 3: Test Straight-Line Drift

Once forward movement is accurate, test sideways drift:

```java
// In your tuning OpMode, after driving forward:
// Don't rotate — just drive forward and measure Y
robot.drive(0.5, 0, 0); // forward only
// Wait 2 seconds, measure final Y

// Ideal: Y ≈ 0" (no sideways drift)
// If Y ≠ 0 significantly, TRACK_WIDTH or CENTER_WHEEL_OFFSET needs adjustment
```

**If robot drifts right (positive Y):**
- Increase `TRACK_WIDTH` slightly (farther apart = less turning)

**If robot drifts left (negative Y):**
- Decrease `TRACK_WIDTH` slightly

Fine-tune in 0.1" increments until drift < 0.2" over 12" movement.

---

## Using Odometry in Your Code

### Reading Current Position

```java
CrawlerRobot robot = MyRobot.build(hardwareMap);
robot.localiser.update();  // Poll latest encoder/IMU values

Pose2d pose = robot.localiser.getPose();
double x = pose.getX();           // inches
double y = pose.getY();           // inches
double heading = pose.getHeading(); // radians, 0 = forward
```

### Resetting Position

```java
// Reset to a known position (e.g., at start of autonomous)
robot.localiser.resetPose(new Pose2d(0, 0, 0));

// Or at a specific position after AprilTag detection
robot.localiser.resetPose(new Pose2d(48, 48, Math.PI/2));
```

### In Your Autonomous Loop

**Pattern 1: Update before movement**
```java
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

while (opModeIsActive()) {
    robot.localiser.update(); // ← Call this EVERY loop iteration
    var pose = robot.localiser.getPose();
    
    // Use pose to decide next move
    if (pose.getX() < 24) {
        follower.follow(path1);
    }
}
```

**Pattern 2: Automatic update (via movement system)**
```java
// FOFollower and ROMovementEngine call localiser.update() internally
// You don't need to manually update in most cases
follower.follow(waypoints); // ← Handles localiser.update() internally
```

---

## Encoder Inversion & Direction

Dead wheel encoders can be mounted either direction. If your odometry reads values backwards:

### Symptom: Negative Values When Moving Forward

```
Expected: robot drives forward (+X direction), odometry reads +12"
Actual: robot drives forward, odometry reads -12"
```

**Fix in MyRobot.java:**

```java
.leftEncoderInverted(true)   // Flip this flag
.rightEncoderInverted(true)
.centerEncoderInverted(false)
```

### Symptom: Sideways Drift When Driving Straight

If the robot drives **forward** but odometry shows **sideways movement** (+Y or -Y):

**Possible causes:**
1. Center wheel mounted backwards → set `centerEncoderInverted(true)`
2. Unequal track width on left/right → measure again, one wheel might be mounted further back
3. Center wheel offset is wrong → remeasure perpendicular distance

---

## Common Pitfalls

### 1. "Odometry reports zero, not updating"

**Symptom:** `getPose()` always returns `(0, 0, 0)` even after driving

**Cause:** `localiser.update()` never called

**Fix:**
```java
while (opModeIsActive()) {
    robot.localiser.update(); // ← Add this!
    Pose2d pose = robot.localiser.getPose();
    telemetry.addData("Pose", pose);
    telemetry.update();
}
```

### 2. "Odometry is accurate at first, then drifts"

**Symptom:** First 12" movement is accurate, but if robot continues moving, error accumulates

**Cause:** IMU heading drift or loose mechanical tolerances

**Fix:**
- Calibrate IMU: Run IMU calibration OpMode before autonomous
- Tighten encoder wheel mounts
- If odometry is consistently off by same factor, recalibrate `WHEEL_DIAMETER`

### 3. "Robot rotates but odometry heading doesn't change much"

**Symptom:** Robot spins 90°, but `pose.getHeading()` changes by only 45°

**Cause:** IMU not configured or heading computation wrong

**Fix:**
```java
// Verify IMU initialization
robot.imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
    RevHubOrientationOnRobot.LogoFacingDirection.FORWARD,
    RevHubOrientationOnRobot.UsbFacingDirection.UP)));
```

### 4. "Track width seems wrong but I measured it correctly"

**Symptom:** Robot curves left even with equal weight on both wheels; track width doesn't help

**Cause:** Actual wheel separation changes during motion (loose mount) or center wheel not perpendicular

**Fix:**
- Tighten all encoder wheel bolts
- Use a straight edge to verify center wheel is perpendicular to left/right wheels
- Re-measure with robot in actual field position

### 5. "Encoder values jump or flicker"

**Symptom:** Telemetry shows occasional large spikes in `getPose()` values

**Cause:** I2C bus noise or loose encoder cable

**Fix:**
- Secure all I2C cables with cable ties
- Reduce cable length near motor power lines
- Check all connector seats in expansion hub
- Try slow I2C bus speed (if possible in FTC SDK)

---

## Alternative Localizers: Pinpoint

If you prefer **absolute position sensing** over accumulated odometry:

### GoBilda Pinpoint Integration

Pinpoint is a field-relative position sensor that **doesn't accumulate error**:

```java
// In MyRobot.build():
.localisation(CrawlerRobot.Localisation.Pinpoint)
.pinpointDeviceName("pinpoint")
.pinpointXOffset(3.0)    // inches from robot center
.pinpointYOffset(0.0)    // inches from robot center
.pinpointUnit(DistanceUnit.INCH)
```

**Advantages:**
- No need to tune `trackWidth` or `centerWheelOffset`
- Heading always accurate (no IMU drift)
- No accumulated error

**Disadvantages:**
- Requires GoBilda Pinpoint hardware (~$40)
- Works indoors only (reflective dots on field)
- Slower update rate than dead wheels

See [Pinpoint Guide](03-robot-configuration.md#pinpoint-integration) for full setup.

---

## Debugging Odometry Issues

### Enable Telemetry Logging

Add to your autonomous loop:

```java
while (opModeIsActive()) {
    robot.localiser.update();
    Pose2d pose = robot.localiser.getPose();
    
    telemetry.addData("X", pose.getX());
    telemetry.addData("Y", pose.getY());
    telemetry.addData("Heading", Math.toDegrees(pose.getHeading()));
    telemetry.addData("Heading (IMU)", robot.imu.getRobotYawPitchRollAngles());
    telemetry.update();
}
```

### Use FTC Dashboard

Real-time graphing of odometry during autonomous:

```gradle
// In build.dependencies.gradle
implementation 'com.acmerobotics.dashboard:dashboard:0.4.16'
```

Then add to telemetry:
```java
TelemetryPacket packet = new TelemetryPacket();
packet.putAll(telemetry.getProperties());
packet.fieldOverlay().setStroke("blue", 2);
packet.fieldOverlay().drawRobotPose(pose);
DASHBOARD.sendTelemetryPacket(packet);
```

---

## Next Steps

- **Implement Robot-Oriented Movement** → [Robot-Oriented Movement Commands](04-robot-oriented-movement.md)
- **Setup FTC Dashboard for real-time tuning** → [Robot Configuration & FTC Dashboard](03-robot-configuration.md)
- **Follow field-relative waypoint paths** → [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md)

Your odometry is the **foundation** of autonomous. Take 15 minutes to calibrate it properly — the rest becomes trivial.
