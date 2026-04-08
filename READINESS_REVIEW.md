# CRAWLER: PRE-TESTING READINESS REVIEW

**Date:** April 8, 2026  
**Reviewer:** Senior Java & FTC Robotics Engineer  
**Assessment:** NOT READY FOR ANY TESTING

---

## EXECUTIVE SUMMARY

Crawler has significant **blocking issues** that prevent both compilation and runtime execution. The library architecture is sound, but critical components are **incomplete or incorrectly wired**, and three essential base classes are **entirely missing**.

**Do not attempt deployment to robot until all Section 1 and Section 2 blocking issues are resolved.**

---

## SECTION 1: COMPILE-TIME ISSUES

### ❌ **BLOCKING: Missing Base Classes**

Three essential classes described in architecture do not exist:

| Class | Status | Impact | Fix |
|-------|--------|--------|-----|
| `CrawlerAuto<R>` | Missing | Cannot create autonomous OpModes | Create abstract class extending LinearOpMode |
| `CrawlerTeleOp<R>` | Missing | Cannot create teleop OpModes | Create abstract class extending OpMode |
| `Waypoint.buildAll()` | Missing method | Path building workflow broken | Add static method or implement in Builder |

#### Fix 1.1: Create `CrawlerAuto.java`

**Status:** `package org.firstinspires.ftc.teamcode.Crawler.core`

```java
public abstract class CrawlerAuto<R extends CrawlerRobot> extends LinearOpMode {
    protected R robot;
    protected FOFollower follower;
    protected ROMovementEngine ro;

    protected abstract R buildRobot(HardwareMap hwMap);
    protected abstract void runPath() throws InterruptedException;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = buildRobot(hardwareMap);
        follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        ro = new ROMovementEngine(robot, this);

        telemetry.addLine("Autonomous ready. Press PLAY.");
        telemetry.update();

        waitForStart();

        if (opModeIsActive()) {
            runPath();
        }

        robot.stop();
    }
}
```

#### Fix 1.2: Create `CrawlerTeleOp.java`

**Status:** `package org.firstinspires.ftc.teamcode.Crawler.core`

```java
public abstract class CrawlerTeleOp<R extends CrawlerRobot> extends OpMode {
    protected R robot;

    protected abstract R buildRobot(HardwareMap hwMap);
    public abstract void loopBody();

    @Override
    public void init() {
        robot = buildRobot(hardwareMap);
    }

    @Override
    public void loop() {
        if (robot != null) {
            loopBody();
        }
    }

    @Override
    public void stop() {
        if (robot != null) {
            robot.stop();
        }
    }

    // Helper: field-relative driving from gamepad
    protected void driveFieldRelative(Gamepad pad) {
        double forward = -pad.left_stick_y;
        double strafe  = pad.left_stick_x;
        double rotate  = pad.right_stick_x;
        robot.driveFieldRelative(forward, strafe, rotate);
    }

    // Helper: robot-relative driving
    protected void driveRobotRelative(Gamepad pad) {
        double forward = -pad.left_stick_y;
        double strafe  = pad.left_stick_x;
        double rotate  = pad.right_stick_x;
        robot.drive(forward, strafe, rotate);
    }
}
```

#### Fix 1.3: Add `Waypoint.buildAll()` Method

**Location:** `org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint`  
**Current Status:** Builder has `.build()` but not `.buildAll()`

Add to `Builder` class:

```java
// List for chaining multiple waypoints
private static List<Waypoint> waypoints = new ArrayList<>();

public Builder at(double x, double y) {
    waypoints.add(new Waypoint(this));
    return new Builder(x, y); // New builder for next waypoint
}

public List<Waypoint> buildAll() {
    waypoints.add(new Waypoint(this));
    List<Waypoint> result = new ArrayList<>(waypoints);
    waypoints.clear(); // Reset for next path
    return result;
}
```

**Alternative (Cleaner):** Implement chaining via static list pattern:

```java
private static List<Waypoint> currentPath = new ArrayList<>();

public static void startPath(double x, double y) {
    currentPath.clear();
    currentPath.add(Waypoint.at(x, y).build());
}

public Builder at(double x, double y) {
    currentPath.add(new Waypoint(this));
    return new Builder(x, y);
}

public List<Waypoint> buildAll() {
    currentPath.add(new Waypoint(this));
    return new ArrayList<>(currentPath);
}
```

---

### ❌ **BLOCKING: goToPosition() Incomplete**

**Class:** `RobotMovement.java` line 166-211  
**Method:** `goToPosition(double x, double y, double moveSpeed, double preferredAngle, double turnSpeed)`

**Problem:** Method computes motor powers but NEVER calls `robot.drive()`. Ends abruptly.

```java
// Lines 197-208 compute powers but:
double movementXPower = (scale > 1e-6) ? (relativeX / scale) * moveSpeed : 0;
double movementYPower = (scale > 1e-6) ? (relativeY / scale) * moveSpeed : 0;
double turnPower = CrawlerMath.clamp(...) * orbitScale;

// ...then nothing! No robot.drive() call! Method just ends!
}
```

**Impact:** Pure pursuit movement produces NO motor output. Robot will not move along paths.

**Fix:** Add motor command before closing brace:

```java
// At the end of goToPosition(), before closing brace:
robot.drive(movementYPower, movementXPower, turnPower);
```

**Note:** Order is (forward, strafe, rotate) so Y→forward, X→strafe per CrawlerRobot.drive() semantics.

---

### ❌ **BLOCKING: Robot.java Constructor Bug**

**Class:** `Robot.java` lines 1-17  
**Issue:** `userRobot` is used before initialization

```java
public Robot(HardwareMap hwMap) {
    // userRobot is NEVER assigned!
    frontRight = (DcMotorEx) userRobot.frontRight;  // ← NullPointerException here
    frontLeft  = (DcMotorEx) userRobot.frontLeft;
    ...
}
```

**Impact:** `Robot.java` is unusable in current form. Throws NullPointerException on construction.

**Fix:** Either:

1. **Remove Robot.java entirely** (it's not used by new architecture)
   - CrawlerRobot replaces it
   - ROMovementEngine should take CrawlerRobot, not Robot

2. **Or rewrite Robot.java to accept CrawlerRobot:**

```java
public class Robot {
    private CrawlerRobot robot;

    public Robot(CrawlerRobot robot) {
        this.robot = robot; // Accept as parameter
    }

    public static void simpleDriveTrainPower(...) {
        // Use this.robot instead of statics
    }
}
```

**Status:** ROMovementEngine imports `Robot` but never uses it. Safe to DELETE.

---

### ❌ **HIGH PRIORITY: Waypoint.build() Missing**

**Class:** `Waypoint.java` line 99  
**Method:** `Builder.build()`

**Current Status:** Shows `.slow()` and `.onReach()` but **`.build()` not shown in provided code**.

**Assumed implementation:**
```java
public Waypoint build() {
    return new Waypoint(this);
}
```

**Issue:** If `.build()` is missing, compiler error: "method build() not found"

**Verify:** Ensure `Waypoint.Builder.build()` exists.

---

### ⚠️ **HIGH PRIORITY: FOFollower.getWorldHeading() Call**

**File:** `FOFollower.java` line 158  
**Code:**
```java
movement.follow(Arrays.asList(waypoint), movement.getWorldHeading());
```

**Issue:** Recursive call to `movement.follow()` from inside path following loop

**Analysis:** 
- `RobotMovement.follow()` calls `goToPosition()` on each waypoint
- `FOFollower.followToWaypoint()` calls `movement.follow()` again
- This creates circular path following at a single waypoint
- **This will work but is algorithmically wrong** — should call `goToPosition()` directly or simplify

**Fix:** Replace with:

```java
// Instead of: movement.follow(...);
// Use: movement.goToPosition(waypoint.x, waypoint.y, waypoint.moveSpeed, movement.getWorldHeading(), waypoint.turnSpeed);
movement.goToPosition(waypoint.x, waypoint.y, waypoint.moveSpeed, 
                      movement.getWorldHeading(), waypoint.turnSpeed);
```

---

### ⚠️ **MEDIUM PRIORITY: CrawlerTuner Imports Custom Pose2d**

**File:** `CrawlerTuner.java` line 12  
**Code:**
```java
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Pose2d;
```

**Issue:** Imports local `Pose2d` but architecture uses FTCLib `Pose2d`:

```java
import com.arcrobotics.ftclib.geometry.Pose2d;
```

**Impact:** Type mismatch if `Pose2d` class doesn't exist in local utils. Compiler error.

**Fix:** Verify `org.firstinspires.ftc.teamcode.Crawler.core.utils.Pose2d` exists, OR change to FTCLib:

```java
import com.arcrobotics.ftclib.geometry.Pose2d;
```

---

### ⚠️ **MEDIUM PRIORITY: ROMovementEngine Imports Old Dependencies**

**File:** `ROMovementEngine.java` lines 8-14  
**Imports:**
```java
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.Robot;
import org.firstinspires.ftc.teamcode.Crawler.Vision.AprilTagWebcam;
import org.firstinspires.ftc.teamcode.Crawler.Vision.Rotation;
```

**Issues:**
1. `Robot.java` is broken (NullPointerException) — should import `CrawlerRobot` instead
2. `AprilTagWebcam` and `Rotation` may not exist (not shown in architecture)
3. Class initializes `aprilTagWebcam` but never uses it:

```java
protected AprilTagWebcam aprilTagWebcam = new AprilTagWebcam();  // Created but unused
```

**Fix:** 
- Remove `Robot` import, use `CrawlerRobot`
- Comment out or remove unused `aprilTagWebcam`
- Verify `AprilTagWebcam` class exists before use

---

### ⚠️ **MEDIUM PRIORITY: CrawlerMath.lineCircleIntersection Uses Mutable Point**

**File:** `CrawlerMath.java` lines 32-35  
**Code:**
```java
if (Math.abs(linePointA.y - linePointB.y) < 0.003) {
    linePointA.y = linePointB.y + 0.003;  // ← Mutates input parameter!
}
```

**Issue:** Method modifies input `Point` objects. This violates encapsulation and can cause bugs if caller doesn't expect mutation.

**Fix:** Create local copies:

```java
Point a = new Point(linePointA.x, linePointA.y);
Point b = new Point(linePointB.x, linePointB.y);

if (Math.abs(a.y - b.y) < 0.003) {
    a.y = b.y + 0.003;
}
```

---

## SECTION 2: RUNTIME CRASHES

### ❌ **BLOCKING: goToPosition() Never Calls robot.drive()**

**Already documented in Section 1 above.** This is both a compile and runtime issue (incomplete method body).

---

### ❌ **BLOCKING: FOFollower.getWorldHeading() Called on Moving Robot**

**File:** `FOFollower.java` line 158  
**Context:** `followToWaypoint()` loop

**Problem:**
```java
robot.localiser.update();  // Updates pose
// ...distance check...
movement.follow(Arrays.asList(waypoint), movement.getWorldHeading());
```

**Issue:** `movement.getWorldHeading()` returns heading from **before localiser.update()**. Pose is stale by one cycle.

**Impact:** Turn commands use old heading → oscillation or incorrect path following

**Fix:** Update pose inside `RobotMovement.follow()` at start:

```java
public void follow(List<Waypoint> allPoints, double followAngle) {
    updatePose();  // ← Add this, not called before

    // ... rest of method
}
```

---

### ❌ **BLOCKING: CrawlerTuner Incomplete**

**File:** `CrawlerTuner.java` line 1-100  
**Issue:** File appears to be truncated. Only shows class header and step definitions, no method bodies.

**Impact:** CrawlerTuner OpMode will not compile. Missing:
- `runOpMode()` implementation
- Step handlers
- Gamepad input loops
- File I/O for saving tuning

**Fix:** Complete the class or verify full file was submitted.

---

### ⚠️ **BLOCKING: FOFollower Recursive Movement Call**

**File:** `FOFollower.java` line 158  
**Code:**
```java
// Inside followToWaypoint():
movement.follow(Arrays.asList(waypoint), movement.getWorldHeading());
```

**Issue:** Calls `movement.follow()` which calls `goToPosition()` which doesn't have `robot.drive()` yet (Section 1 issue).

**Analysis of Control Flow:**
1. `FOFollower.follow(List<Waypoint>)` iterates waypoints
2. For each waypoint, calls `followToWaypoint(waypoint)`  
3. `followToWaypoint()` loops, calling `movement.follow(single waypoint)` each iteration
4. This is **double-wrapping** the pure pursuit algorithm

**Impact:** Inefficient and confusing control flow. Motor commands twice?

**Fix:** Replace with direct goToPosition call:

```java
movement.goToPosition(waypoint.x, waypoint.y, waypoint.moveSpeed, 
                      movement.getWorldHeading(), waypoint.turnSpeed);
```

---

### ⚠️ **MEDIUM: CrawlerRobot.buildLocaliser() Missing Null Checks**

**File:** `CrawlerRobot.java` lines 65-130  
**Issue:** No validation that encoders are available when constructing localizers

**Example:**
```java
case ThreeDeadWheel:
    return new ThreeDeadWheelLocaliser(
            builder.leftEncoder,      // ← Could be null!
            builder.rightEncoder,     
            builder.centerEncoder,
            ...
    );
```

**Impact:** NullPointerException if encoder names wrong or encoder not added to builder

**Fix:** Add validation:

```java
if (builder.localisation == Localisation.ThreeDeadWheel) {
    if (builder.leftEncoder == null || builder.rightEncoder == null || builder.centerEncoder == null) {
        throw new IllegalStateException(
            "ThreeDeadWheel localizer requires all three encoders. Check hardware names.");
    }
    return new ThreeDeadWheelLocaliser(...);
}
```

---

### ⚠️ **MEDIUM: FOFollower Silently Ignores Empty Waypoint List**

**File:** `FOFollower.java` line 108  
**Code:**
```java
public void follow(List<Waypoint> waypoints) throws InterruptedException {
    if (waypoints == null || waypoints.isEmpty()) {
        return;  // ← Silent return, no warning
    }
    ...
}
```

**Issue:** Teams might pass empty list by mistake. Should at least log warning.

**Fix:**

```java
if (waypoints == null || waypoints.isEmpty()) {
    telemetry.addWarning("WARNING: Empty waypoint list passed to FOFollower.follow()");
    telemetry.update();
    return;
}
```

---

### ⚠️ **MEDIUM: Thread.sleep() vs LinearOpMode.sleep()**

**File:** `FOFollower.java` line 130  
**Code:**
```java
private void followToWaypoint(Waypoint waypoint) throws InterruptedException {
    ...
    Thread.yield();  // ← Should be sleep() instead
}
```

**Issue:** `Thread.yield()` doesn't pause, just yields CPU. Should use `sleep(long ms)` for actual delay OR `this.sleep(long ms)` if FOFollower extends OpMode.

**Analysis:** FOFollower doesn't extend OpMode, so cannot use `this.sleep()`. Must accept OpMode or expose sleep method.

**Options:**
1. Add `sleep(long ms)` to constructor, store as `sleeper` callable
2. Accept OpMode in constructor, call `opMode.sleep()`
3. Keep `Thread.yield()` if intending to loop rapidly (no sleep needed)

**Fix Option 1:**
```java
public FOFollower(CrawlerRobot robot, Telemetry telemetry, OpModeProxy opModeProxy) {
    this.robot = robot;
    this.movement = new RobotMovement(robot);
    this.telemetry = telemetry;
    this.opModeProxy = opModeProxy;
}

// In followToWaypoint:
// Remove Thread.yield(), let loop run at natural speed
// Add: if (waypointTimer.seconds() > timeout) break;
```

---

### ⚠️ **MEDIUM: CrawlerMath.lineCircleIntersection Silent Exception**

**File:** `CrawlerMath.java` lines 60-71  
**Code:**
```java
try {
    // ...math...
} catch (Exception e) {
    // silently ignore (could log if needed)
}
```

**Issue:** Swallows all exceptions, including bugs. NaN from division by zero would be silently handled.

**Fix:** 

```java
try {
    // ... math ...
} catch (Exception e) {
    // Log the error
    System.err.println("Error in lineCircleIntersection: " + e.getMessage());
    e.printStackTrace();
}
```

Or, check before bad math:

```java
double discriminant = Math.pow(quadraticB, 2) - (4.0 * quadraticA * quadraticC);
if (discriminant < 0 || Double.isNaN(discriminant)) {
    return allPoints; // no intersection
}
```

---

## SECTION 3: INTEGRATION FAILURES

### ❌ **CRITICAL: Pure Pursuit Motor Output Missing**

**Flow:** FOFollower → RobotMovement → … → motor not set

**Trace:**
1. `FOFollower.followToWaypoint()` calls `movement.follow(waypoint, angle)`
2. `RobotMovement.follow()` calls `goToPosition(x, y, moveSpeed, angle, turnSpeed)`
3. `goToPosition()` calculates powers (`movementXPower`, `movementYPower`, `turnPower`)
4. **Then does NOTHING** — no `robot.drive()` call!
5. Motors never receive power commands
6. **Robot does not move**

**Fix:** See Section 1 — add `robot.drive()` in `goToPosition()`.

---

### ❌ **CRITICAL: ROMovementEngine Incompatible with CrawlerAuto**

**Architectural Mismatch:**

According to architecture, `CrawlerAuto` should wire:
```java
ro = new ROMovementEngine(robot, this);
```

But `ROMovementEngine` signature is:
```java
public abstract class ROMovementEngine extends LinearOpMode {
    public abstract void runPath() throws InterruptedException;
    
    @Override
    public void runOpMode() { ... }
}
```

**Problem:** ROMovementEngine **extends LinearOpMode** — it can't be used as a helper class. It's designed to BE the OpMode, not be wrapped by it.

**Issue:** Cannot instantiate `new ROMovementEngine(robot, this)` — wrong design pattern.

**Fix Option 1: Make ROMovementEngine Non-Abstract Helper**

```java
public class ROMovementEngine {
    private CrawlerRobot robot;
    
    public ROMovementEngine(CrawlerRobot robot) {
        this.robot = robot;
    }
    
    public void drivePID(double distance) throws InterruptedException { ... }
    public void strafePID(double distance) throws InterruptedException { ... }
    // ...
}
```

Then `CrawlerAuto` can use:
```java
ro = new ROMovementEngine(robot);
ro.drivePID(12);  // Works
```

**Fix Option 2: Keep As Abstract OpMode**

Require teams to extend ROMovementEngine directly:
```java
@Autonomous(name = "My Auto")
public class MyAuto extends ROMovementEngine {
    
    @Override
    public void buildRobot() { ... }
    
    @Override
    public void runPath() { ... }
}
```

But then architecture promise of "CrawlerAuto<R> base class" is broken.

**Recommendation:** Go with Option 1 — refactor ROMovementEngine to be a helper, not an OpMode.

---

### ⚠️ **FOFollower May Not Update Pose Before PID**

**File:** `FOFollower.java` line 113  
**Code:**
```java
robot.localiser.update();  // ← Update here

// Distance check uses updated pose
double distanceToTarget = Math.hypot(...);

// But then:
movement.follow(Arrays.asList(waypoint), movement.getWorldHeading());
```

**Issue:** `movement.getWorldHeading()` might be stale if `RobotMovement.updatePose()` isn't called in its `follow()` method.

**Verify:** Does `RobotMovement.follow()` call `updatePose()` at the start? (Not shown in provided code.)

**Assumed Fix Location:** Add to `RobotMovement.follow()`:

```java
public void follow(List<Waypoint> allPoints, double followAngle) {
    updatePose();  // ← Ensure pose is fresh
    
    TelemetryPacket packet = new TelemetryPacket();
    // ...rest
}
```

---

### ⚠️ **Waypoint Builder onReach Fires at Correct Waypoint?**

**File:** `FOFollower.java` lines 118-121  
**Code:**
```java
for (int i = 0; i < waypoints.size(); i++) {
    Waypoint target = waypoints.get(i);
    followToWaypoint(target);
    
    if (target.onReach != null) {
        target.onReach.run();  // ← Fires AFTER arrival
    }
}
```

**Analysis:** Loop structure is correct:
1. Follow to waypoint i
2. When arrival threshold met, exit `followToWaypoint()`
3. Fire `onReach` callback
4. Move to next waypoint

**Verdict:** Correct. No off-by-one error.

---

### ⚠️ **CrawlerRobot.buildLocaliser() Covers All Cases?**

**File:** `CrawlerRobot.java` lines 65-130  
**Switch statement:**

```java
switch (builder.localisation) {
    case ThreeDeadWheel: return new ThreeDeadWheelLocaliser(...);
    case TwoDeadWheel: return new TwoWheelLocaliser(...);
    case Pinpoint: return new PinpointLocaliser(...);
    case MotorEncoder: return new MotorEncoderLocaliser(...);
    case DevLocaliser:
    default: return new DevLocaliser();
}
```

**Enum values:** `Localisation { MotorEncoder, TwoDeadWheel, ThreeDeadWheel, Pinpoint, DevLocaliser }`

**Verdict:** All enum cases covered. Safe.

---

### ⚠️ **Builder Anonymous Classes Capture self Correctly?**

**File:** `CrawlerRobot.java` lines 275-310  
**Code:**
```java
@Override
public IThreeDeadWheelStage withThreeDeadWheels(String left, String right, String center) {
    this.localisation  = Localisation.ThreeDeadWheel;
    this.leftEncoder   = new MotorEx(hwMap, left);
    this.rightEncoder  = new MotorEx(hwMap, right);
    this.centerEncoder = new MotorEx(hwMap, center);
    
    Builder self = this;
    return new IThreeDeadWheelStage() {
        @Override
        public IReadyStage setCenterWheelOffset(double offset) {
            self.centerWheelOffset = offset;  // Writes back to Builder
            return self;                       // Returns builder
        }
    };
}
```

**Analysis:** Pattern is correct:
1. Save reference to `Builder` as `self`
2. Anonymous class captures `self`
3. Anonymous class methods modify `self.'s fields
4. Returns `self` to continue chain

**Verdict:** Correct fluent builder pattern. No issues.

---

### ⚠️ **Motor Inversion Applied Before Localiser Construction?**

**File:** `CrawlerRobot.java` lines 55-68  
**Code:**
```java
// Line 44-47: Apply motor inversions
if (builder.frontLeftInverted)  this.frontLeft.setInverted(true);
// ...

// Line 55-68: Assign encoders (after)
this.leftEncoder = builder.leftEncoder;
// ...

// Line 70: Build localiser (after)
this.localiser = buildLocaliser(builder);
```

**Issue:** Encoder inversions may not be applied before `buildLocaliser()` is called.

**Check:** Inside `buildLocaliser()` for ThreeDeadWheel:

```java
case ThreeDeadWheel:
    return new ThreeDeadWheelLocaliser(
            builder.leftEncoder,
            builder.rightEncoder,
            builder.centerEncoder,
            builder.leftEncoderInverted,    // ← Passed to constructor
            builder.rightEncoderInverted,
            builder.centerEncoderInverted
    );
```

**Inside ThreeDeadWheelLocaliser:**

```java
if (invertLeft)   leftEncoder.setInverted(true);  // ← Applied here, inside localiser
if (invertRight)  rightEncoder.setInverted(true);
```

**Verdict:** Inversions applied in correct order (inside localiser constructor, after encoder assignment). OK.

---

## SECTION 4: PURE PURSUIT CORRECTNESS

### ⚠️ **Angle Wrapping in Radians?**

**File:** `CrawlerMath.java` lines 12-17  
**Code:**
```java
public static double wrapAngle(double angle) {
    while (angle < -180) angle += 360;
    while (angle > 180) angle -= 360;
    return angle;
}
```

**Issue:** This wraps in **DEGREES** (-180 to 180 degrees), but all calculated angles are in **RADIANS**.

**Example:** If `angle = 2π radians = 360 degrees`:
- Function adds 360: `angle = 2π + 360 = 366.28 radians` (wrong!)
- Should detect radian range: `-π to π`

**Impact:** Heading calculations will be massively wrong. Turns could be 57× too large!

**Fix:**

```java
public static double wrapAngle(double angleRadians) {
    while (angleRadians < -Math.PI) angleRadians += 2 * Math.PI;
    while (angleRadians > Math.PI) angleRadians -= 2 * Math.PI;
    return angleRadians;
}
```

**Critical:** Search entire codebase for other degree/radian mixing.

---

### ⚠️ **Turn Angle Formula Correctness**

**File:** `RobotMovement.java` lines 186-187  
**Code:**
```java
double relativeTurnAngle = CrawlerMath.wrapAngle(preferredAngle - relativeAngle);
```

**Analysis:**
- `preferredAngle` = desired heading (radians)
- `relativeAngle` = angle from robot to target (radians)
- `relativeTurnAngle` = difference

**Is this correct?**

In pure pursuit, robot should face the direction to the target. If target is directly ahead (0 rad relative), robot should turn 0. If target is 45° to the side (π/4 rad relative), robot should command a turn proportional to that.

**Formula appears correct:** turn error = desired heading - current heading to target

**Verdict:** Math is correct IF angles are in consistent units (radians). But Section 4.1 (wrapAngle) bug corrupts this.

---

### ⚠️ **Path Extension Math**

**File:** `RobotMovement.java` lines 67-81  
**Code:**
```java
double dx = last.x - secondLast.x;
double dy = last.y - secondLast.y;
double len = Math.hypot(dx, dy);

if (len < 1e-6) return extended;  // Avoid div by zero

double extendBy = last.followDistance;
double extraX = last.x + (dx / len) * extendBy;
double extraY = last.y + (dy / len) * extendBy;
```

**Analysis:**
- Computes vector from second-to-last to last waypoint: `(dx, dy)`
- Normalizes: `(dx/len, dy/len)`
- Extends in that direction by `followDistance`

**Verdict:** Correct. Extends path to prevent premature stopping.

---

### ❌ **Orbit Fade Scale Could Exceed 1.0**

**File:** `RobotMovement.java` lines 192-195  
**Code:**
```java
double orbitScale = CrawlerMath.clamp(
    distanceToTarget / RobotConfig.FieldOriented.ORBIT_THRESHOLD, 0, 1
);
```

**Analysis:**
- If `distanceToTarget > ORBIT_THRESHOLD`, result could exceed 1.0
- `clamp(value, 0, 1)` ensures it stays in [0, 1]
- At long distance: `100 / 10 = 10` → clamped to 1.0
- At short distance: `2 / 10 = 0.2` → stays 0.2

**Verdict:** Correct. Clamp prevents >1.0.

---

### ⚠️ **getDynamicFollowDistance() Might Pick Wrong Segment**

**File:** `RobotMovement.java` lines 95-113  
**Code:**
```java
private double getDynamicFollowDistance(List<Waypoint> points) {
    double closestDist = Double.MAX_VALUE;
    double followRadius = points.get(0).followDistance;
    
    for (int i = 0; i < points.size() - 1; i++) {
        Waypoint a = points.get(i);
        Waypoint b = points.get(i + 1);
        
        double midX = (a.x + b.x) / 2.0;
        double midY = (a.y + b.y) / 2.0;
        double dist = Math.hypot(midX - worldX, midY - worldY);
        
        if (dist < closestDist) {
            closestDist = dist;
            followRadius = b.followDistance;  // ← Uses END waypoint's followDistance
        }
    }
    
    return followRadius;
}
```

**Analysis:** Finds segment whose midpoint is closest to robot, then uses that segment's END waypoint's followDistance.

**Issue:** If segment AB is closest, should use A's or B's followDistance? Currently uses B (end).

**This is probably intentional** (use the "next" waypoint's parameters), but worth documenting.

**Verdict:** No bug, but design choice is implicit.

---

### ⚠️ **lineCircleIntersection Handles Horizontal/Vertical Lines?**

**File:** `CrawlerMath.java` lines 32-36  
**Code:**
```java
if (Math.abs(linePointA.y - linePointB.y) < 0.003) {
    linePointA.y = linePointB.y + 0.003;  // Near-horizontal line  
}

if (Math.abs(linePointA.x - linePointB.x) < 0.003) {
    linePointA.x = linePointB.x + 0.003;  // Near-vertical line
}
```

**Analysis:** If line is perfectly horizontal or vertical, slope `m` would be 0 or undefined, causing division by zero. This code perturbs the line slightly to avoid singularity.

**Issue:** Perturbation is 0.003 units. If your waypoints are closer than this, could cause geometry errors.

**Verdict:** Pragmatic fix, works but could be more elegant (use parametric line equations instead of slope-intercept).

---

## SECTION 5: ODOMETRY CORRECTNESS

### ✅ **ThreeDeadWheelLocaliser**

**File:** `ThreeDeadWheelLocaliser.java` lines 1-38

**Checklist:**
- ✅ Calls `setDistancePerPulse()` on all encoders
- ✅ Distance formula correct: `π * DIAMETER / TICKS_PER_REV`
- ✅ Encoder inversions applied before HolonomicOdometry construction
- ✅ `update()` and `getPose()` implemented
- ✅ `resetPose()` calls `updatePose(pose)` (correct FTCLib API)

**Verdict:** Correct.

---

### ✅ **TwoWheelLocaliser**

**File:** `TwoWheelLocaliser.java` (not provided but referenced)

**Assumes:** Similar structure to ThreeDeadWheelLocaliser but with 2 encoders

**Potential Issue:** If implementation doesn't exist, buildLocaliser() would crash.

**Verify:** File exists and implements CrawlerLocaliser interface.

---

### ❌ **PinpointLocaliser.resetPose() Wrong**

**File:** `PinpointLocaliser.java` lines 35-37  
**Code:**
```java
@Override
public void resetPose(Pose2d pose) {
    pinpoint.resetPosAndIMU();  // ← Resets to (0,0,0), ignores pose parameter!
}
```

**Issue:** GoBilda Pinpoint's `resetPosAndIMU()` **always resets to (0,0,0)**. Cannot reset to arbitrary `Pose2d`.

**Implication:** Calling `robot.localiser.resetPose(new Pose2d(24, 48, π/2))` will reset to (0,0,0) instead!

**Fix Options:**

1. **Comment out parameter, document limitation:**
   ```java
   @Override
   public void resetPose(Pose2d pose) {
       // GoBilda Pinpoint always resets to (0,0,0), cannot set arbitrary pose
       pinpoint.resetPosAndIMU();
   }
   ```

2. **Use Pinpoint's setPosition() if available:**
   ```java
   @Override
   public void resetPose(Pose2d pose) {
       pinpoint.setPosition(pose.getX(), pose.getY(), pose.getHeading());
   }
   ```

3. **Throw exception if pose is not (0,0,0):**
   ```java
   @Override
   public void resetPose(Pose2d pose) {
       if (Math.abs(pose.getX()) > 0.01 || Math.abs(pose.getY()) > 0.01) {
           throw new UnsupportedOperationException(
               "Pinpoint can only reset to (0,0,0), got (" + pose.getX() + ", " + pose.getY() + ")");
       }
       pinpoint.resetPosAndIMU();
   }
   ```

---

### ✅ **DevLocaliser**

**File:** `DevLocaliser.java` lines 1-50

**Checklist:**
- ✅ Returns non-null Pose2d from getPose()
- ✅ Implements all interface methods
- ✅ Simulates motion (increments X, rotates)

**Verdict:** Correct for testing/development.

---

### ⚠️ **MotorEncoderLocaliser Uses Wrong Constant**

**File:** `MotorEncoderLocaliser.java` lines 17-20  
**Code:**
```java
frontLeft.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
frontRight.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
backLeft.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
backRight.setDistancePerPulse(RobotConfig.RobotBase.TICKS_PER_CM);
```

**Issue:** Calls `setDistancePerPulse()` with `TICKS_PER_CM`, which is a TICKS count, not a distance!

**Check RobotConfig:**
```java
public static double TICKS_PER_METER = 2000.0;
public static double TICKS_PER_CM = TICKS_PER_METER / 100;  // = 20 ticks/cm
```

**Problem:** `setDistancePerPulse()` expects distance per pulse, like:
```
distance/pulse = 0.05 cm/pulse  (if 20 ticks → 1 cm)
or
distance/pulse = π * diameter / ticks_per_rev
```

**Current code sets distance to 20, which FTCLib interprets as "20 cm per tick" — massively wrong!

**Fix:** Invert or use correct constant:

```java
// Option 1: Compute correct distance per pulse
double distancePerPulse = RobotConfig.Odometry.WHEEL_DIAMETER * Math.PI 
                         / RobotConfig.Odometry.TICKS_PER_REV;
frontLeft.setDistancePerPulse(distancePerPulse);
// ...

// Option 2: Use RobotBase constant correctly
// This constant represents "how many ticks in X distance"
// We need the inverse: "what distance per tick"
double distancePerTick = 100.0 / RobotConfig.RobotBase.TICKS_PER_CM;  // cm per tick
frontLeft.setDistancePerPulse(distancePerTick);
```

**Verdict:** Bug. Odometry will report position 100-1000× wrong.

---

## SECTION 6: DX AND SAFETY ISSUES

### ⚠️ **No Error Message if Motor Names Missing**

**File:** `CrawlerRobot.java` lines 268-272  
**Code:**
```java
@Override
public ILocaliserStage motors() {
    if (frontLeftName == null || frontRightName == null
            || backLeftName == null || backRightName == null)
        throw new IllegalStateException(
            "All four drive motor names must be set before calling motors().");
    return this;
}
```

**Problem:** Error message is generic. If hardware name is wrong, `new MotorEx(hwMap, name)` will throw cryptic exception buried in logs.

**Fix:** Add validation inside CrawlerRobot constructor or buildLocaliser():

```java
private CrawlerLocaliser buildLocaliser(Builder builder) {
    // Validate hardware availability before using
    String[] motorNames = { builder.frontLeftName, builder.frontRightName, 
                           builder.backLeftName, builder.backRightName };
    for (String name : motorNames) {
        if (!builder.hwMap.contains(DcMotor.class, name)) {
            throw new IllegalStateException("Motor '" + name + "' not found in hardware map. "
                + "Check your configuration.json and MyRobot.java");
        }
    }
    // ... create localiser
}
```

---

### ⚠️ **CrawlerTuner Incomplete — Cannot Validate Readiness**

**File:** `CrawlerTuner.java` (truncated)

**Issue:** Only class header provided. Cannot verify:
- Gate logic (prevent skipping bad steps)
- Gamepad input handling
- File I/O safety
- JSON/TXT persistence

**Recommendation:** Complete the class or provide full file for review.

---

### ⚠️ **FOFollower.followToWaypoint() No Emergency Stop**

**File:** `FOFollower.java` lines 130-160  
**Code:**
```java
while (opModeProxy.isActive()) {
    if (waypointTimer.seconds() > timeout) {
        telemetry.addData("WARNING", "Waypoint timeout: %.1f seconds", waypointTimer.seconds());
        telemetry.update();
        break;  // ← Just breaks, doesn't stop motors!
    }
    // ...
}
```

**Issue:** If OpMode stops during path following, loop breaks but `robot.stop()` may not be called immediately.

**Check:** After the loop in `follow()`:

```java
robot.stop();  // ← Added after loop in follow()
```

**Verdict:** OK, but could add emergency stop inside timeout:

```java
if (waypointTimer.seconds() > timeout) {
    robot.stop();  // ← Add this
    telemetry.addWarning("Waypoint timeout: %.1f seconds" + waypointTimer.seconds());
    break;
}
```

---

### ⚠️ **CrawlerRobot.stop() Missing OpMode Check**

**File:** `CrawlerRobot.java` lines 156-162  
**Code:**
```java
public void stop() {
    frontLeft.set(0); frontRight.set(0);
    backLeft.set(0);  backRight.set(0);
}
```

**Analysis:** Sets motor power directly. OK in LinearOpMode context, but if called after OpMode stops, FTC SDK will reject the command.

**Verdict:** No risk if called before OpMode ends. OK.

---

## SECTION 7: MISSING COMPONENTS

| Component | Status | Impact | Fix Time |
|-----------|--------|--------|----------|
| `CrawlerAuto<R>` | Missing | Cannot create autonomous OpModes | 30 min |
| `CrawlerTeleOp<R>` | Missing | Cannot create teleop OpModes | 30 min |
| `Waypoint.buildAll()` | Missing | Path construction broken | 15 min |
| `TwoWheelLocaliser` | Assumed exists | May not compile | 15 min if missing |
| `Point` class | Assumed exists | CrawlerMath.lineCircleIntersection fails | 10 min if missing |
| `Vector2d` class | ✅ Exists | OK | - |
| ROMovementEngine refactor | Needed | Cannot wire to CrawlerAuto | 1-2 hours |
| `goToPosition()` completion | Needed | Pure pursuit non-functional | 5 min |

---

## SECTION 8: READINESS VERDICT

```
VERDICT: NOT READY

Blocking issues (must fix before ANY testing):     8
High priority (fix before competition):            5
Low priority (fix before release):                 3
Missing components:                                4

Estimated fix time: 4-6 hours

═════════════════════════════════════════════════════════════════

BLOCKING ISSUES (CANNOT RUN)

1. goToPosition() method incomplete — no robot.drive() call
   Impact: Pure pursuit produces zero motor output
   Fix: Add robot.drive(movementYPower, movementXPower, turnPower);
   Time: 5 minutes

2. CrawlerAuto<R> class missing
   Impact: Teams cannot extend autonomous base class
   Fix: Create class (template provided in Section 1)
   Time: 30 minutes

3. CrawlerTeleOp<R> class missing  
   Impact: Teams cannot extend teleop base class
   Fix: Create class (template provided in Section 1)
   Time: 30 minutes

4. Waypoint.buildAll() method missing
   Impact: Path construction syntax broken
   Fix: Add method to Builder (template provided in Section 1)
   Time: 15 minutes

5. Robot.java NullPointerException on init
   Impact: Robot.java cannot be instantiated
   Fix: Delete Robot.java or rewrite (not needed, use CrawlerRobot)
   Time: 5 minutes

6. ROMovementEngine extends LinearOpMode (wrong pattern)
   Impact: Cannot wire into CrawlerAuto as helper
   Fix: Refactor to non-extending helper class
   Time: 1-2 hours

7. PinpointLocaliser.resetPose() ignores pose parameter
   Impact: Cannot reset to arbitrary position with Pinpoint
   Fix: Document limitation or use Pinpoint.setPosition()
   Time: 15 minutes

8. wrapAngle() uses degrees, not radians
   Impact: All turn calculations wrong by 57×
   Fix: Change to radian wrapper
   Time: 10 minutes

═════════════════════════════════════════════════════════════════

HIGH PRIORITY (FIX BEFORE TESTING)

1. MotorEncoderLocaliser uses wrong distance constant
   Impact: Odometry reports pos 100× wrong with motor encoders
   Fix: Use π*diameter/ticks formula
   Time: 15 minutes

2. FOFollower recursive movement call awkward
   Impact: Inefficient path following logic
   Fix: Simplify to direct goToPosition call
   Time: 30 minutes

3. Missing null checks in buildLocaliser()
   Impact: Cryptic exceptions if encoder names wrong
   Fix: Add validation before construction
   Time: 20 minutes

4. CrawlerTuner incomplete (truncated?)
   Impact: Cannot tune odometry/PID
   Fix: Provide or implement full tuning system
   Time: 2-4 hours

5. CrawlerMath.lineCircleIntersection mutates input
   Impact: Can cause subtle bugs in path following
   Fix: Use local copies instead of mutating
   Time: 10 minutes

═════════════════════════════════════════════════════════════════

LOW PRIORITY (BEFORE RELEASE)

1. Silent exception catching in lineCircleIntersection
   Impact: Bugs hidden in math operations
   Fix: Log exceptions or check for NaN
   Time: 10 minutes

2. FOFollower silently ignores empty waypoint list
   Impact: Teams confused when path doesn't execute
   Fix: Add telemetry warning
   Time: 5 minutes

3. CrawlerTuner imports custom Pose2d
   Impact: Potential type mismatch with FTCLib Pose2d
   Fix: Verify or use FTCLib version
   Time: 5 minutes

═════════════════════════════════════════════════════════════════

TOP 3 FIXES (DO FIRST)

1. Complete goToPosition() method — add robot.drive() call
   Reason: Pure pursuit completely non-functional without this
   Time: 5 minutes
   Severity: CRITICAL

2. Fix wrapAngle() to use radians
   Reason: All heading calculations wrong by 57×
   Time: 10 minutes
   Severity: CRITICAL

3. Create CrawlerAuto<R> and CrawlerTeleOp<R> base classes
   Reason: Teams cannot write autonomous without these
   Time: 1 hour
   Severity: CRITICAL

═════════════════════════════════════════════════════════════════
```

---

## RECOMMENDATIONS

### Immediate Actions (Before Any Test)

1. **Test Compilation:**
   ```bash
   ./gradlew compileReleaseJava
   ```
   Fix all compile errors in Section 1.

2. **Add Unit Tests:**
   - Test `CrawlerMath.wrapAngle()` with radians
   - Test `RobotMovement.goToPosition()` returns correct motor powers
   - Test `Waypoint` builder produces correct list

3. **Integration Test:**
   - Create minimal OpMode using CrawlerAuto
   - Verify pure pursuit produces motor output
   - Verify odometry updates each cycle

### Before Robot Testing

4. **Hardware Validation:**
   - Encoder names match configuration.json
   - Motor powers go to correct physical motors
   - Odometry reads match actual robot movement

5. **Field Testing Protocol:**
   - Start with DevLocaliser (no hardware needed)
   - Verify motor output with power meter
   - Test pure pursuit with one 12" straight line
   - Verify arrival detection works
   - Test with 3-point L-shape path

### Before Competition

6. **Full System Integration:**
   - Multi-path autonomous sequences
   - Real odometry (3-dead-wheel or Pinpoint)
   - PID tuning via FTC Dashboard
   - Stress testing (10+ waypoint paths, tight loops, high speeds)

---

## SUMMARY

Crawler has a **sound architecture** but **incomplete implementation**. Most issues are minor fixes, but three are blocking:

1. **Incomplete `goToPosition()` method** — pure pursuit produces no motor output
2. **Missing `CrawlerAuto<R>` and `CrawlerTeleOp<R>`** — teams cannot build autonomous/teleop
3. **`wrapAngle()` in degrees not radians** — all turn calculations wrong by 57×

**Estimated time to production readiness: 4-6 hours** with experienced developer.

**Do not send to robot until all Section 1 issues resolved.**

