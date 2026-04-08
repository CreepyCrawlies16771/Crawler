# Field-Oriented Pure Pursuit

**Objective:** Understand pure pursuit path following, build waypoint-based autonomous routines with event callbacks, and optimize path execution for your field geometry.

---

## What is Field-Oriented Pure Pursuit?

**Pure Pursuit** is a path-following algorithm that:
1. Takes a **list of waypoints** (goal positions on the field)
2. Computes a single "follow point" **ahead of the robot** on the path
3. Commands the robot to **drive toward** that point
4. Updates every ~50ms with new follow point

**Field-Oriented** means waypoints use **absolute field coordinates** (0,0 at starting corner), decoded via odometry. The robot automatically handles rotation to face the path.

### Pure Pursuit Conceptually

```
    FIELD (Bird's-eye view)
    
    Path: (0,0) → (24,24) → (48,0)
    
         ↗ Follow point ahead of robot on path
        /
    [R] ← Robot current position
     ↗
    Start point (0,0)
    
    Algorithm each frame:
    1. Find closest point on path
    2. Look ahead 10" along path (follow point)
    3. Command: "Drive toward follow point"
    4. Repeat
```

**Result:** Robot smoothly follows the curved path without pre-programming each turn.

---

## Why Pure Pursuit?

### Advantages Over Robot-Oriented

| Aspect | RO (drivePID) | FO (Pure Pursuit) |
|--------|---------------|-------------------|
| **Path quality** | Stop-start, jerky | Continuous, smooth |
| **Ease of planning** | Manual waypoints | Visual field planning |
| **Turning efficiency** | Turn, then drive | Simultaneous drive+turn |
| **Field complexity** | 3-4 waypoints OK | 10+ waypoints ideal |
| **Speed** | Slower (discrete steps) | Faster (continuous) |
| **Code readability** | Imperative (easy) | Declarative (concise) |

### When to Use Pure Pursuit

- **Multi-action sequences** — visit 5+ zones with placed samples
- **Smooth field navigation** — avoid obstacles in curved paths
- **High speed autonomous** — trajectory takes 20-30 seconds for full map
- **Score optimization** — multiple deliveries with minimal movement overhead

---

## Core Concepts

### Waypoints: The Path Definition

A **Waypoint** specifies:
- **Position** (x, y in inches on the field)
- **Speed** (move speed at this waypoint)
- **Turn speed** (rotation speed)
- **Follow distance** (how far ahead to look)
- **onReach callback** (code to run when waypoint reached)

### Path: The Waypoint Sequence

A sequence of waypoints forms a **path**:
```
(0, 0) → (24, 0) → (24, 24) → (0, 24)  ← forms a square path
```

The pure pursuit algorithm:
1. Interpolates between waypoints
2. Never pauses at waypoints
3. Fires `onReach` callback when robot enters waypoint neighborhood
4. Continues smoothly to next waypoint

---

## Building Waypoints

### Builder Pattern

Waypoints use a **fluent builder** for clean syntax:

```java
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

// Single waypoint
Waypoint w1 = Waypoint.at(24, 0);

// Chain multiple waypoints
List<Waypoint> path = Waypoint.at(0, 0)
    .at(24, 0)
    .at(24, 24)
    .at(0, 24)
    .buildAll();
```

### Customizing Waypoints

```java
List<Waypoint> path = Waypoint.at(0, 0)
    
    // Waypoint 1: drive forward slowly
    .at(12, 0)
        .speed(0.5)              // 50% speed
        .onReach(() -> robot.openClaw())
    
    // Waypoint 2: faster, default speed
    .at(24, 12)
        .speed(0.8)              // 80% speed
        .turnSpeed(0.5)          // Slower turning
    
    // Waypoint 3: precision placement, slow
    .at(24, 24)
        .slow()                  // Convenience: speed(0.3), turnSpeed(0.2)
        .onReach(() -> {
            robot.depositSpecimen();
            robot.openClaw();
        })
    
    .buildAll();
```

### Convenience Methods

```java
// Shorthand: slow mode (30% speed)
.at(x, y).slow()

// Equivalent to:
.at(x, y).speed(RobotConfig.FieldOriented.SLOW_MOVE_SPEED)
         .turnSpeed(RobotConfig.FieldOriented.SLOW_TURN_SPEED)

// Custom speeds
.at(x, y).speed(0.7).turnSpeed(0.4)

// Event callback
.onReach(() -> {
    telemetry.addLine("Reached waypoint!");
    robot.intakeOn();
})
```

---

## FOFollower: Path Execution

`FOFollower` wraps the pure pursuit algorithm and provides a high-level **blocking interface**.

### Basic Usage

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.Crawler.MyRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

@Autonomous(name = "Simple Field Path", group = "FO Examples")
public class SimpleFieldPath extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        
        telemetry.addLine("Ready to follow path.");
        telemetry.addLine("Press PLAY.");
        telemetry.update();
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Define path: move to (24, 0), then (24, 24)
            var path = Waypoint.at(0, 0)
                .at(24, 0)
                .at(24, 24)
                .buildAll();
            
            // Follow path — blocks until complete or OpMode inactive
            follower.follow(path, 0);  // followAngle = 0 (pointing down field)
            
            telemetry.addLine("Path complete!");
            telemetry.update();
        }
    }
}
```

### FOFollower Constructor

```java
FOFollower follower = new FOFollower(
    robot,                      // CrawlerRobot instance
    telemetry,                  // For debug output
    this::opModeIsActive        // Callback to check if OpMode running
);
```

### follow() Method

```java
follower.follow(List<Waypoint> path, double followAngle);
```

**Parameters:**
- **`path`** — list of waypoints in order (start with current position)
- **`followAngle`** — desired heading in radians
  - `0` = facing away from starting position (forward)
  - `Math.PI / 2` = facing left
  - `Math.PI` = facing backward

**Behavior:**
1. Blocks until all waypoints visited or OpMode stops
2. Fires `onReach()` callbacks for each waypoint
3. Handles heading control automatically
4. Returns when path complete

---

## Real-World Autonomous Sequences

### Scenario 1: Sample Delivery (3-Zone Pickup)

```java
@Autonomous(name = "3-Zone Delivery", group = "FO Examples")
public class ThreeZoneDelivery extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Autonomous sequence:
            // 1. Pick up samples at (0, 36)
            // 2. Deliver to zone A (36, 12)
            // 3. Deliver to zone B (36, 48)
            // 4. Return to start
            
            var path = Waypoint.at(0, 0)
                
                // Drive to picking zone
                .at(0, 36)
                    .slow()
                    .onReach(() -> {
                        robot.intakeOn();
                        sleep(1500);  // Collect sample
                    })
                
                // Drive to delivery zone A
                .at(36, 24)
                    .speed(0.9)  // Fast transit
                
                .at(36, 12)
                    .slow()
                    .onReach(() -> {
                        robot.depositSample();
                        sleep(500);
                    })
                
                // Transit to zone B (if sample remains)
                .at(36, 48)
                    .slow()
                    .onReach(() -> {
                        if (robot.hasSecondSample()) {
                            robot.depositSample();
                            sleep(500);
                        }
                    })
                
                // Return to start
                .at(0, 0)
                    .speed(0.8)
                
                .buildAll();
            
            follower.follow(path, 0);
            
            telemetry.addLine("Autonomous complete!");
            telemetry.update();
        }
    }
}
```

### Scenario 2: AprilTag-Guided Path

```java
@Autonomous(name = "Vision-Guided Delivery", group = "FO Examples")
public class VisionGuidedDelivery extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        AprilTagWebcam camera = new AprilTagWebcam();
        camera.init(hardwareMap, telemetry);
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Phase 1: Drive to observation zone to detect tags
            camera.update();
            AprilTagDetection blueTag = camera.findTag(BLUE_TAG_ID);
            
            if (blueTag != null) {
                // Adjust path based on tag position
                double deliveryX = blueTag.x + 6;  // Offset from tag
                double deliveryY = blueTag.y;
                
                var path = Waypoint.at(0, 0)
                    .at(12, 6)
                        .speed(0.5)
                        .onReach(() -> camera.update())
                    .at(deliveryX, deliveryY)
                        .slow()
                        .onReach(() -> robot.depositSpecimen())
                    .at(0, 0)
                    .buildAll();
                
                follower.follow(path, 0);
            } else {
                telemetry.addLine("No April Tag detected!");
                telemetry.update();
            }
        }
    }
}
```

### Scenario 3: Multi-Mission Autonomous

```java
@Autonomous(name = "Full Mission", group = "FO Examples")
public class FullMissionAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = MyRobot.build(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        
        waitForStart();
        
        if (opModeIsActive()) {
            // Mission 1: Park in zone 1
            var mission1 = Waypoint.at(0, 0)
                .at(24, 0)
                .at(24, 24)
                .buildAll();
            
            follower.follow(mission1, 0);
            telemetry.addLine("Mission 1 complete");
            telemetry.update();
            sleep(1000);
            
            // Mission 2: Return and deliver
            var mission2 = Waypoint.at(24, 24)
                .at(0, 0)
                .buildAll();
            
            follower.follow(mission2, Math.PI);  // Face backward
            telemetry.addLine("Mission 2 complete");
            telemetry.update();
        }
    }
}
```

---

## Pure Pursuit Algorithm Deep Dive

### How the Algorithm Works

Each cycle (~50ms):

1. **Update pose** from odometry
2. **Find current segment** — which line is robot closest to?
3. **Look ahead** — find point 10" further along path
4. **Calculate vector** from robot to follow point
5. **Extract heading** from vector direction
6. **Apply PID** to move toward follow point

### The Follow Point

The **follow distance** (default 10") is crucial:

```
Too small    (3"):     Jerky, oscillates on path
Just right  (10"):     Smooth, stable
Too large  (20"):      Overshoots waypoints, misses turns
```

**Adjust via RobotConfig:**
```java
@Config
public static class FieldOriented {
    public static double DEFAULT_FOLLOW_DISTANCE = 10.0;  // inches
    public static double SLOW_FOLLOW_DISTANCE = 5.0;      // For slow mode
}
```

### Speed Control

Robot speed changes at each waypoint:

```java
// Slow down for precision
.at(24, 24)
    .speed(0.3)      // 30% power
    .turnSpeed(0.2)
    .onReach(() -> robot.place())

// Normal speed for transit
.at(48, 0)
    .speed(0.7)      // 70% power
```

---

## Path Planning Tips

### 1. Always Start at Current Position

```java
// Wrong: starts from (0,0) even if robot elsewhere
var path = Waypoint.at(6, 6).at(12, 12).buildAll();

// Right: start from where robot actually is
var path = Waypoint.at(0, 0)  // Current position
    .at(6, 6)
    .at(12, 12)
    .buildAll();
```

### 2. Use Field Coordinates Consistently

```
FIELD (standard FTC 144" × 144" setup):

    (0, 144)  Team Blue --- (144, 144)
        |                       |
        |                       |
        |                       |
    (0, 0)  Red Corner  --- (144, 0)

All waypoints relative to field origin (0, 0)
```

### 3. Avoid Sharp Angles

Instead of:
```java
.at(0, 0)
.at(24, 0)
.at(24, 24)    // ← 90° sharp turn
```

Use curved path:
```java
.at(0, 0)
.at(24, 0)
.at(28, 4)     // ← Intermediate waypoint smooths curve
.at(24, 24)
```

### 4. Group Actions Logically

```java
// Group pickup and delivery together
var path = Waypoint.at(0, 0)
    
    // Pickup zone
    .at(12, 12)
        .onReach(() -> robot.intakeOn())
    .at(12, 18)
        .slow()
        .onReach(() -> {
            robot.intakeOff();
            sleep(500);
        })
    
    // Delivery zone
    .at(36, 6)
        .slow()
        .onReach(() -> {
            robot.depositSample();
            sleep(500);
        })
    
    .buildAll();
```

---

## Common Pitfalls

### 1. "Robot doesn't reach waypoint, overshoots instead"

**Symptom:** Robot drives past waypoint(s), or misses turns

**Cause:** `DEFAULT_FOLLOW_DISTANCE` too large

**Fix:**
```java
// In RobotConfig.FieldOriented:
public static double DEFAULT_FOLLOW_DISTANCE = 8.0;  // Decrease from 10
```

Or per-waypoint:
```java
.at(24, 24)
    .followDistance(5.0)  // ← Smaller for precision
    .slow()
```

### 2. "Path is jerky or wobbles side-to-side"

**Symptom:** Robot follows path but movement is not smooth

**Cause:** 
- Follow distance too small
- PID gains too aggressive
- Odometry drift

**Fix:**
```java
// Increase follow distance
.at(x, y).followDistance(12.0)

// Or check odometry calibration
// Run TuneOdometry from tuning OpModes
```

### 3. "onReach callback never fires"

**Symptom:** `robot.depositSample()` code in callback never executes

**Cause:** 
- Waypoint never reached (robot took different path)
- Callback syntax wrong

**Fix:**
```java
// Make sure callback is actually attached:
.at(24, 24)
    .onReach(() -> robot.depositSample())  // ← Must call .onReach()
    .slow()  // ← Can chain more methods after
```

### 4. "Robot gets stuck in infinite loop near waypoint"

**Symptom:** Robot circles waypoint repeatedly, never proceeds

**Cause:** `ARRIVAL_THRESHOLD` never guaranteed (odometry error)

**Fix:**
```java
// Increase arrival threshold tolerance
public static double ARRIVAL_THRESHOLD = 3.0;  // From 2.0
```

Or verify odometry:
```java
// Debug: What is robot reporting?
telemetry.addData("Robot X", robot.localiser.getPose().getX());
telemetry.addData("Target X", 24);
```

### 5. "Path works in practice, fails in competition"

**Symptoms:** Erratic movement, waypoints not reached consistently

**Causes:**
- Different floor texture (carpet vs tile)
- Different battery voltage
- Odometry calibration off

**Fix:**
- Save two RobotConfig profiles (practice vs competition)
- Add 2-3 second delays after placement actions
- Increase turn speed slightly for margin

---

## Optimization Techniques

### 1. Parallel Actions (Subsystems)

```java
// Intake starts spinning while driving to pickup zone
.at(12, 12)
    .speed(0.8)
    .onReach(() -> robot.intakeOn())
    
.at(12, 18)  // Still spinning while driving here
    .slow()
    .onReach(() -> robot.intakeOff())
```

Result: Intake is already spinning when robot arrives, saves time.

### 2. Calculate Paths Dynamically

```java
// Inspect field at runtime, adjust path
camera.update();
int elementCount = camera.countSamples();

if (elementCount > 2) {
    // Path to zone A
    var path = Waypoint.at(0, 0)
        .at(36, 12)
        .buildAll();
} else {
    // Path to zone B (farther)
    var path = Waypoint.at(0, 0)
        .at(36, 48)
        .buildAll();
}

follower.follow(path, 0);
```

### 3. Pre-Pathfinding

Plan entire autonomous in startup:

```java
waitForStart();

// Build all path segments upfront
var pickup = Waypoint.at(0, 0).at(12, 12).buildAll();
var deliver1 = Waypoint.at(12, 12).at(36, 12).buildAll();
var deliver2 = Waypoint.at(36, 12).at(36, 48).buildAll();
var home = Waypoint.at(36, 48).at(0, 0).buildAll();

if (opModeIsActive()) {
    follower.follow(pickup, 0);
    follower.follow(deliver1, 0);
    follower.follow(deliver2, 0);
    follower.follow(home, 0);
}
```

---

## Field-Oriented vs Robot-Oriented Comparison

| Scenario | RO | FO |
|----------|----|----|
| Pickup sample at (12,12) from (0,0) | `drivePID(12)` then `strafePID(12)` | Single waypoint |
| Deliver at (36,12), then (36,48) | Manual `turnPID`, then `drivePID` | Continuous smooth path |
| Score time (30s autonomous) | ~25s execution + 5s overhead | ~18s execution (smoother path) |
| Precision placement (±1") | Good (PID tuned) | Very good (pure pursuit) |
| Code readability | Imperative, easy | Declarative, concise |

**Recommendation:**
- **< 3 waypoints** → RO is easier
- **3-6 waypoints** → Either works
- **> 6 waypoints** → FO is superior

---

## Testing Checklist

| Test | Expected | Pass? |
|------|----------|-------|
| Single waypoint path | Robot reaches within ±2" | ✓ |
| 3-waypoint L-shaped path | Smooth curve at corner | ✓ |
| onReach callback | Code executes at waypoint | ✓ |
| High speed (0.9) then slow (0.3) | Speed changes visibly | ✓ |
| Dashboard tune follow distance | Path behavior changes | ✓ |
| Long path (10+ waypoints) | Completes without stopping | ✓ |

---

## Debugging With Telemetry

Add this to your autonomous for real-time debugging:

```java
if (opModeIsActive()) {
    var path = Waypoint.at(0, 0)
        .at(24, 0)
        .at(24, 24)
        .buildAll();
    
    // Log path before execution
    telemetry.addLine("Path waypoints:");
    for (var wp : path) {
        telemetry.addData("  (" + wp.x + ", " + wp.y + ")", 
            " speed=" + wp.moveSpeed);
    }
    telemetry.update();
    
    follower.follow(path, 0);
    
    // Log final position
    telemetry.addData("Final X", robot.localiser.getPose().getX());
    telemetry.addData("Final Y", robot.localiser.getPose().getY());
    telemetry.update();
}
```

---

## Next Steps

- **Integrate vision guidance** → Detect AprilTags, modify waypoints dynamically
- **Build full mission** → Combine multiple FOFollower calls for multi-phase autonomous
- **Optimize timing** → Measure autonomous time, reduce delays

Your field-oriented autonomous is production-ready when it:
1. Reaches all waypoints within ±2 inches
2. Executes callbacks at correct waypoints
3. Completes without stopping or overshooting
4. Achieves consistent timing (within ±2 seconds) across test runs

---

## Further Reading

- [Pure Pursuit Algorithm (original paper)](https://www.ri.cmu.edu/pub_files/pub3/coulter_r_craig_1992_1.pdf) — Advanced math
- [FTC Dashboard Guide](https://acmerobotics.org/dashboard/) — Real-time telemetry setup
- [Alternative Path Following: Ramsete, LQR](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/index.html) — Advanced control theory (optional)

Pure pursuit is simple, effective, and production-proven in FTC. Master it, and your autonomous will be competitive.
