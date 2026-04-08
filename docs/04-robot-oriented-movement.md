# Robot-Oriented Movement Commands

**Objective:** Execute imperative movement commands (`drivePID`, `strafePID`, `turnPID`, `arc`) with closed-loop feedback, understand PID tuning, and build simple autonomous sequences.

---

## What is Robot-Oriented Movement?

**Robot-Oriented (RO) movement** uses **imperative commands** that block until complete. The robot executes one command at a time with PID feedback correction.

### RO vs FO (Quick Comparison)

| Aspect | Robot-Oriented | Field-Oriented |
|--------|-----------------|-----------------|
| **Command style** | Imperative: `robot.drivePID(12)` | Declarative: `waypoints.buildAll()` |
| **Coordinate frame** | Robot's perspective (forward/left/turn) | Field's perspective (absolute x/y) |
| **Best for** | Simple sequences, scored actions | Complex pre-planned paths |
| **Complexity** | Low — few lines of code | Higher — needs waypoint planning |
| **Accuracy** | Good (odometry-based PID) | Better (pure pursuit + odometry) |

### When to Use RO

- **Pick up from starting location** → drive forward 24", strafe left 12", turn 45°
- **Interleaved autonomous + manual actions** → drive, then actuate arm, then drive again
- **Simple scoring routines** → drive, place, retreat

### When to Use FO

- **Complex field geometry** → multiple targets in different directions
- **Precise path following** → must avoid obstacles in specific pattern
- **High speed autonomous** → pure pursuit is smoother than stop-start PID

---

## Core RO Commands

All commands exist in `ROMovementEngine` class and are called on the robot object:

### 1. drivePID: Move Forward/Backward

```java
robot.drivePID(double distance)  // distance in inches, positive = forward
```

**Example:**
```java
robot.drivePID(12);   // Drive forward 12 inches
robot.drivePID(-6);   // Drive backward 6 inches
```

**What happens:**
1. Robot records starting position via odometry
2. Applies PID control to match forward velocity setpoint
3. Blocks until robot position matches target distance ±tolerance
4. Returns and continues to next command

**Behind the scenes:**
```
Error = targetDistance - currentDistance
Power = Kp * Error + Ki * integral(Error) + Kd * dError/dt
robot.drive(Power, 0, 0)  // Forward only
```

### 2. strafePID: Move Left/Right

```java
robot.strafePID(double distance)  // positive = right, negative = left
```

**Example:**
```java
robot.strafePID(8);    // Strafe right 8 inches
robot.strafePID(-12);  // Strafe left 12 inches
```

**Note:** Uses separate PID gains: `strafe_Kp`, `strafe_Ki`, `strafe_Kd` in `RobotConfig`

### 3. turnPID: Rotate in Place

```java
robot.turnPID(double degreesCCW)  // positive = counterclockwise (left turn)
```

**Example:**
```java
robot.turnPID(90);   // Turn 90° CCW (left)
robot.turnPID(-45);  // Turn 45° CW (right)
```

**Uses:** `STEER_P`, `STEER_I`, `STEER_D` from `RobotConfig.RobotOriented`

### 4. arc: Curved Movement

```java
robot.arc(double degreesRotated, double radiusInches)
```

**Example:**
```java
robot.arc(90, 12);   // Rotate 90° CCW while following 12" radius circle
robot.arc(-45, 20);  // Rotate 45° CW with 20" radius
```

**Best for:** Smooth entry into walls, diagonal approaches

---

## Building Simple Autonomous Sequences

### Example 1: T-Junction Placement

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Crawler.MyRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@Autonomous(name = "T-Junction Placement", group = "RO Examples")
public class TJunctionPlacementAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        
        telemetry.addLine("T-Junction autonomous:");
        telemetry.addLine("1. Drive forward to T-junction (12\")");
        telemetry.addLine("2. Strafe left to center (6\")");
        telemetry.addLine("3. Place specimen");
        telemetry.addLine("Press PLAY");
        telemetry.update();
        
        waitForStart();
        
        if (opModeIsActive()) {
            try {
                // Sequence 1: Drive forward to junction
                telemetry.addLine("Driving forward...");
                telemetry.update();
                robot.drivePID(12);  // Blocks until complete
                
                telemetry.addLine("Driving forward complete. Strafing...");
                telemetry.update();
                
                // Sequence 2: Strafe to center
                robot.strafePID(-6);  // Strafe left
                
                // Sequence 3: Place specimen (external mechanism)
                robot.openSpecimenClaw();  // Your subsystem method
                sleep(500);
                
                // Retreat
                robot.drivePID(-8);
                
                telemetry.addLine("Autonomous complete!");
                telemetry.update();
                
            } catch (Exception e) {
                telemetry.addLine("ERROR: " + e.getMessage());
                telemetry.update();
            }
        }
    }
}
```

### Example 2: Delivery with Rotation

```java
@Autonomous(name = "Deliver and Return", group = "RO Examples")
public class DeliverAndReturnAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Go to delivery zone 1
            robot.drivePID(18);      // Forward
            robot.strafePID(12);     // Right
            
            // Delivery action
            robot.depositSample();
            sleep(1000);
            
            // Go to delivery zone 2
            robot.drivePID(12);      // Further forward
            robot.turnPID(90);       // Turn to face zone 2
            
            // Deliver again
            robot.depositSample();
            
            // Return to start
            robot.turnPID(-90);
            robot.drivePID(-30);
        }
    }
}
```

---

## Implementation Details

> **Note:** The actual `ROMovementEngine` implementation is in `TeamCode/src/main/java/.../RobotOrient/ROMovementEngine.java`. Here we show the conceptual interface.

### drivePID Inner Loop

Conceptually, `drivePID(12)` does:

```java
public void drivePID(double distanceInches) throws InterruptedException {
    double targetDistance = robotPosition.x + distanceInches;
    long startTime = System.currentTimeMillis();
    double tolerance = 0.5; // inches
    
    while (Math.abs(robotPosition.x - targetDistance) > tolerance) {
        // Update position
        localiser.update();
        robotPosition = localiser.getPose();
        
        // PID calculation
        double error = targetDistance - robotPosition.x;
        integral += error * dt;
        double derivative = (error - lastError) / dt;
        
        double power = Kp * error 
                     + Ki * integral 
                     + Kd * derivative;
        power = Math.max(-1.0, Math.min(1.0, power)); // Clamp
        
        // Apply drive command
        robot.drive(power, 0, 0);
        
        // Safety timeout
        if (System.currentTimeMillis() - startTime > 5000) {
            throw new InterruptedException("drivePID timeout");
        }
        
        lastError = error;
        Thread.sleep(10); // ~100 Hz loop
    }
    
    robot.stop();
}
```

### Why PID Works

**Scenario:** Robot is 2 inches from target.
```
Error = 2 inches
Power = 0.05 * 2 = 0.1 (10% power)
Robot applies gentle forward force
Robot slows as it approaches target
Gentle deceleration prevents overshoot
```

**Scenario:** Robot overshot by 1 inch (position is NOW +1 past target).
```
Error = -1 inch (negative!)
Power = 0.05 * (-1) = -0.05 (reverse power)
Robot backs up slightly
Settles at target
```

---

## PID Tuning for RO Commands

### Starting Configuration

In `RobotConfig.RobotOriented`:

```java
public static double Kp        = 0.05;   // Start conservative
public static double Ki        = 0.0;    // Start at zero
public static double Kd        = 0.0;    // Start at zero

public static double strafe_Kp = 0.05;
public static double strafe_Ki = 0.0;
public static double strafe_Kd = 0.0;

public static double STEER_P   = 0.03;
public static double STEER_I   = 0.0;
public static double STEER_D   = 0.0;

public static double MIN_POWER = 0.15;   // Deadband for friction
```

### Tuning Process (via FTC Dashboard)

#### Phase 1: Tune Kp (Proportional)

Create a test OpMode:

```java
@Autonomous(name = "Tune Kp", group = "Tuning")
public class TuneKpAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        
        telemetry.addLine("Tuning Kp: Drive forward 24 inches");
        telemetry.addLine("Observe: smooth approach to target?");
        telemetry.addLine("Press PLAY");
        telemetry.update();
        
        waitForStart();
        
        while (opModeIsActive()) {
            // Read fresh value from Dashboard
            telemetry.addData("Current Kp", RobotConfig.RobotOriented.Kp);
            telemetry.update();
            
            robot.drivePID(24);
            
            // After movement completes, show result
            var pose = robot.localiser.getPose();
            telemetry.addData("Final X", pose.getX());
            telemetry.addData("Kp was", RobotConfig.RobotOriented.Kp);
            telemetry.addLine("Adjust Kp in Dashboard and press PLAY again");
            telemetry.update();
            
            break; // Exit after one iteration
        }
    }
}
```

**Observations:**

| Symptom | Meaning | Action |
|---------|---------|--------|
| Robot approaches target smoothly, stops accurately | Kp is good | Keep it, move to Kd |
| Robot overshoots by 2-3 inches, oscillates | Kp too high | Decrease by 50% |
| Robot creeps slowly, never quite reaches | Kp too low | Increase by 50% |
| Robot stops short of target | Friction issue | Increase `MIN_POWER` |

**Typical progression:**
- Start: `Kp = 0.05`
- Too slow → `Kp = 0.08`
- Overshoots → `Kp = 0.06`
- Settles → done with Kp

#### Phase 2: Add Kd (Derivative for Damping)

Once Kp is good, add damping to reduce overshoot:

```java
// In Dashboard, set:
RobotConfig.RobotOriented.Kd = 0.01;  // Start small
```

**Effect:**
- Smoother deceleration near target
- Less overshoot
- Faster settling

**Typical value:** `Kd = 0.5 * Kp` or `0.5 * 0.07 = 0.035`

#### Phase 3: Ki Only If Steady-State Error

If robot stops 0.5 inches short despite high Kp/Kd:

```java
RobotConfig.RobotOriented.Ki = 0.001;  // Very small!
```

**Warning:** Ki can cause wild oscillation. Increase slowly (0.001 increments).

### Example Calibrated Values

After tuning, you might have:

```java
@Config
public static class RobotOriented {
    // Forward movement (well-tuned)
    public static double Kp        = 0.07;
    public static double Ki        = 0.0;
    public static double Kd        = 0.03;
    
    // Strafing (usually same as forward)
    public static double strafe_Kp = 0.07;
    public static double strafe_Ki = 0.0;
    public static double strafe_Kd = 0.03;
    
    // Rotation (usually lower, more finesse)
    public static double STEER_P   = 0.05;
    public static double STEER_I   = 0.0;
    public static double STEER_D   = 0.02;
    
    public static double MIN_POWER = 0.15;
}
```

---

## Advanced Patterns

### Synchronized Multi-Axis Movement

Move forward AND strafe simultaneously:

```java
// Conceptual — implement if your ROMovementEngine supports it
robot.drivePID(12);    // This blocks...
robot.strafePID(-6);   // This runs after

// Better approach: use FOFollower for diagonal movement
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
follower.follow(Waypoint.at(0, 0).at(12, -6).buildAll());
```

### Conditional Sequences Based on Vision

```java
CrawlerRobot robot = MyRobot.build(hardwareMap);
AprilTagWebcam camera = new AprilTagWebcam();
camera.init(hardwareMap, telemetry);

robot.drivePID(18);  // Drive forward to see targets

camera.update();
if (camera.hasTarget(BLUE_GOAL)) {
    robot.strafePID(-12);  // Strafe left to align
    robot.drivePID(6);
    robot.depositSpecimen();
} else {
    robot.drivePID(-18);   // Retreat if no target
}
```

### Repeated Delivery Cycles

```java
for (int cycle = 0; cycle < 3; cycle++) {
    // Move to delivery zone
    robot.drivePID(12 * (cycle + 1));
    
    // Deliver
    robot.depositSpecimen();
    sleep(500);
    
    // Back up for next
    robot.drivePID(-12);
}

// Return to start
robot.drivePID(-(12 * 3));
```

---

## Common Pitfalls

### 1. "Robot moves but stops short by ~1 inch"

**Symptom:**
```
Expected: robot at X = 24"
Actual: robot at X = 23"
```

**Cause:** `MIN_POWER` is too low, PID can't overcome friction at low speeds

**Fix:**
```java
RobotConfig.RobotOriented.MIN_POWER = 0.2;  // Increase from 0.15
```

### 2. "Robot oscillates around target (bounces)"

**Symptom:**
```
X = 23.8", then 24.2", then 23.9", then 24.1"...
Robot vibrates audibly
```

**Cause:** `Kp` or `Kd` causes instability

**Fix:**
- Decrease `Kp` by 30%
- Increase `Kd` to 0.5× new `Kp`

### 3. "Commands execute out of order or overlap"

**Symptom:**
```
robot.drivePID(12);   // Should block here
robot.drivePID(6);    // But this runs immediately?
```

**Cause:** `drivePID` not blocking (threading issue)

**Fix:** Ensure your `ROMovementEngine` calls aren't async:

```
java
// Wrong (non-blocking in background):
executorService.execute(() -> robot.drivePID(12));
robot.drivePID(6);  // Runs immediately

// Right (blocking):
robot.drivePID(12);  // Waits until complete
robot.drivePID(6);   // Then runs
```

### 4. "Strafing doesn't work, moves forward instead"

**Cause:** Motor layout doesn't support strafe (non-holonomic drivetrain)

**Note:** Crawler assumes **holonomic mecanum or omni wheels**. If you use differential drive, only `drivePID(forward)` and `turnPID(rotation)` work.

### 5. "PID tuning via Dashboard doesn't take effect"

**Cause:** OpMode caches value at startup

**Fix:** Read dynamically inside movement loop:

```java
// Wrong (cached at startup):
double kp = RobotConfig.RobotOriented.Kp;

// Right (read each frame):
// In your inner loop of drivePID():
double kp = RobotConfig.RobotOriented.Kp;  // Fresh read
power = kp * error + ...;
```

---

## Testing Checklist

| Test | Expected Result | Pass? |
|------|-----------------|-------|
| `drivePID(12)` on flat field | Robot stops within ±1" of 12" | ✓ |
| `drivePID(12)` then `drivePID(-12)` | Returns to start ±1" | ✓ |
| `strafePID(8)` | Moves sideways 8" | ✓ |
| `turnPID(90)` | Faces perpendicular to start | ✓ |
| `drivePID` with modified `Kp` in Dashboard | Behavior changes without restart | ✓ |
| Multiple commands in sequence | Execute in order, don't overlap | ✓ |

---

## Comparison: RO vs FO for Common Scenarios

### Scenario: "Go to zone 1, place, go to zone 2, place, return"

**With RO (imperative):**
```java
robot.drivePID(18);
robot.strafePID(12);
robot.place();

robot.drivePID(12);
robot.turnPID(45);
robot.place();

robot.drivePID(-30);
```
✅ Simple, readable, easy to debug  
❌ Doesn't optimize path smoothly

**With FO (declarative):**
```java
follower.follow(
    Waypoint.at(0, 0)
        .at(18, 12).onReach(() -> robot.place())
        .at(30, 18).onReach(() -> robot.place())
        .at(0, 0)
        .buildAll()
);
```
✅ Smoother path, one coherent trajectory  
❌ More setup, harder to debug individual steps

**Recommendation:** Use **RO for< 4 waypoints**, **FO for complex field geometry**.

---

## Next Steps

- **Build more complex sequences** → Study [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md) for comparison
- **Integrate vision** → Add AprilTag detection alongside RO movement
- **Optimize speed** → Increase `DEFAULT_MOVE_SPEED` once tuning is solid

Your RO autonomous is production-ready when all commands reach targets accurately and execute in sequence without delay.
