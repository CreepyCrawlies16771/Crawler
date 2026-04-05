# Crawler Build Specification

You are helping build **Crawler** — an open source FTC (FIRST Tech Challenge) robotics library targeting Gauteng teams and nonprofit recipient teams. The goal is autonomous path following setup in under 30 minutes compared to Road Runner or Pedro Pathing.

## Project Context

**Package:** `org.firstinspires.ftc.teamcode.Crawler`
**Repo:** `https://github.com/CreepyCrawlies16771/Crawler` (dev branch)
**Target:** Android/Java, FTC SDK, FTCLib core dependency

## Architecture Already Built

```
CrawlerRobot          — hardware abstraction, staged builder, motor control,
                        localiser factory, drive() driveFieldRelative() stop()
CrawlerLocaliser      — interface: update(), getPose(), resetPose(Pose2d)
ThreeDeadWheelLocaliser — wraps FTCLib HolonomicOdometry
TwoWheelLocaliser     — wraps FTCLib HolonomicOdometry (2 wheel variant)
MotorEncoderLocaliser — uses drive motors as encoders
PinpointLocaliser     — wraps GoBilda Pinpoint SDK
RobotMovement         — pure pursuit core: follow(), getFollowPointPath(),
                        goToPosition(), path extension, dynamic lookahead,
                        turn fix, orbit fade
Waypoint              — chained builder: at(x,y).at(x,y).speed().heading()
                        .onReach(Runnable).slow().buildAll()
Vector2d              — 2D vector with distanceTo, normalized, dot, angleTo
CrawlerMath           — wrapAngle(), clamp(), lineCircleIntersection()
RobotConfig           — @Config inner classes: Odometry, RobotOriented,
                        FieldOriented, RobotBase. NO hardware names stored here.
CrawlerAuto<R>        — abstract base: robot, follower, ro fields.
                        buildRobot(HardwareMap) and runPath() are abstract.
                        Both FOFollower and ROMovementEngine available.
CrawlerTeleOp<R>      — abstract base: robot field, buildRobot() abstract,
                        driveFieldRelative(Gamepad) and driveRobotRelative(Gamepad)
                        helpers provided.
TuneRobotOrientPID    — tuning OpMode, persists values to /sdcard/Crawler/tuned_pid.txt
```

## CrawlerRobot Builder API (for reference)

```java
// Hardware names live in MyRobot — CrawlerRobot reads them via the builder
// Teams call: new MyRobot(hardwareMap) — builder is hidden inside super()

protected CrawlerRobot(Builder builder) { ... }
protected static Builder builder(HardwareMap hwMap) { ... }

// Staged builder chain:
builder(hwMap)
    .frontLeft("fl").frontRight("fr").backLeft("bl").backRight("br")
    .motors()
    .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
        .setTrackWidth(13.0)
        .setCenterWheelOffset(3.5)
    // OR .withTwoDeadWheels("l", "c").setTrackWidth(13.0)
    // OR .withMotorEncoders()
    // OR .withPinpoint("pinpoint").offsets(3.0, -2.5)
```

## MyRobot Pattern (for reference)

```java
public class MyRobot extends CrawlerRobot {
    public final Servo clawServo;
    public final DcMotor liftMotor;

    public MyRobot(HardwareMap hwMap) {
        super(
            CrawlerRobot.builder(hwMap)
                .frontLeft("fl").frontRight("fr")
                .backLeft("bl").backRight("br")
                .motors()
                .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
                .setTrackWidth(13.0)
                .setCenterWheelOffset(3.5)
        );
        clawServo = hwMap.get(Servo.class, "claw");
        liftMotor = hwMap.get(DcMotor.class, "lift");
    }

    public void openClaw()  { clawServo.setPosition(0.8); }
    public void closeClaw() { clawServo.setPosition(0.2); }
    public void scoreHighBasket() { setLift(800); openClaw(); }
}
```

## OpMode DX Target

```java
// Auto
@Autonomous(name = "Red Auto")
public class RedAuto extends CrawlerAuto<MyRobot> {
    @Override protected MyRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }
    @Override protected void runPath() throws InterruptedException {
        follower.follow(
            Waypoint.at(0, 0)
                    .at(24, 0).speed(0.8).onReach(() -> robot.openClaw())
                    .at(24, 24).slow()
                    .buildAll()
        );
        ro.drivePID(5, 0);
        ro.turnPID(90);
    }
}

// TeleOp
@TeleOp(name = "Driver")
public class Driver extends CrawlerTeleOp<MyRobot> {
    @Override protected MyRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }
    @Override public void loop() {
        driveFieldRelative(gamepad1);
        if (gamepad2.a) robot.openClaw();
        if (gamepad2.b) robot.scoreHighBasket();
    }
}
```

## What Needs To Be Built — In Priority Order

### 1. `DevLocaliser`
Returns a static zero pose. No hardware. Used as fallback in `buildLocaliser()` when no localiser is configured, and during tuning OpModes that don't need real odometry.

```java
public class DevLocaliser implements CrawlerLocaliser {
    // always returns Pose2d(0, 0, 0)
    // update() does nothing
    // resetPose() does nothing
}
```

### 2. `ROMovementEngine`
Imperative PID movement engine. Used inside `CrawlerAuto` alongside `FOFollower`. Must NOT extend `LinearOpMode` — it takes a `CrawlerRobot` and `LinearOpMode` reference in its constructor so it can call `opModeIsActive()` and `telemetry`.

Methods needed:
- `drivePID(double distanceInches, double headingDegrees)` — drive forward/back using encoder distance + heading hold
- `strafePID(double distanceInches, double headingDegrees)` — strafe using encoder distance
- `turnPID(double targetDegrees)` — point turn to absolute heading using IMU
- `arc(double distanceInches, HeadingTimeline timeline)` — drive while following a heading curve

All PID constants come from `RobotConfig.RobotOriented`. Powers go through `robot.drive()`. Uses start-offset approach for encoder distance (not reset). All constants (`STEER_P`, min power, etc.) come from `RobotConfig` — nothing hardcoded.

### 3. `FOFollower`
Blocking wrapper around `RobotMovement`. Called from `CrawlerAuto` as `follower.follow(list)`.

```java
public class FOFollower {
    public FOFollower(CrawlerRobot robot) { ... }
    public void follow(List<Waypoint> waypoints) throws InterruptedException { ... }
    public void follow(Waypoint... waypoints) throws InterruptedException { ... }
}
```

Loop per waypoint segment until `distanceTo(target) < arrivalThreshold`. Call `robot.localiser.update()` each cycle. Fire `onReach` Runnable when arrived. Call `robot.stop()` at end. Use `RobotMovement` internally for the pursuit math.

### 4. `MyRobot` example
Concrete example subclass for teams to copy. Shows: hardware name strings defined here (NOT in RobotConfig), season hardware fields, `super(builder(...))` pattern, high-level action methods.

### 5. Track width tuning OpMode
`TuneTrackWidth` — drives the robot in a full circle, measures heading error, adjusts `RobotConfig.Odometry.TRACK_WIDTH`. Uses FTC Dashboard. Saves result to `/sdcard/Crawler/tuned_track_width.txt`.

### 6. Odometry accuracy tuning OpMode
`TuneOdometry` — drives a square, measures return-to-origin error, helps teams validate localiser accuracy. Displays live pose on FTC Dashboard field view.

### 7. `CrawlerStateMachine<S extends Enum<S>>`
Generic state machine for TeleOp subsystem management.

```java
public class CrawlerStateMachine<S extends Enum<S>> {
    public CrawlerStateMachine(S initialState) { ... }
    public void transition(S newState) { ... }
    public S getState() { ... }
    public boolean inState(S state) { ... }
    public long timeInState() { ... } // ms since last transition
}
```

## Javadoc Requirements

Every class and public method must have a Javadoc comment following these rules:

**Class-level Javadoc:**
```java
/**
 * Brief one-line description.
 *
 * <p>Longer explanation of purpose, design decisions, and how it fits
 * into the Crawler architecture. Mention what layer it belongs to.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // show the typical usage pattern
 * }</pre>
 *
 * @see RelatedClass
 * @author Crawler
 */
```

**Method-level Javadoc:**
```java
/**
 * Brief description of what this method does.
 *
 * <p>Longer explanation if needed — edge cases, preconditions,
 * what happens internally.</p>
 *
 * @param paramName  description of the parameter and its units
 * @param paramName2 description
 * @return           what is returned, and what it means
 * @throws ExceptionType when this is thrown
 */
```

**Rules:**
- Units must always be in the param description — e.g. `@param distanceInches distance to travel, in inches`
- No `@author` tags — Crawler is open source, no individual attribution
- Every `public` and `protected` member gets Javadoc
- Package-private and `private` members get inline `//` comments only
- Javadoc must be accurate — if the method does X, say X, not something vague
- Cross-reference related classes with `@see`
- For `@Config` fields, document the valid range and what happens at extremes

## Style Rules

- No comments inside method bodies — only Javadoc on signatures
- All constants come from `RobotConfig` — nothing hardcoded in logic
- Instance methods only — no `static` on movement logic
- `robot.drive()` is the only way to set motor power — never call `motor.set()` directly outside `CrawlerRobot`
- All angles in radians internally — convert at the API boundary only
- Use `Vector2d` not raw `double x, double y` pairs where possible
- `throws InterruptedException` on any blocking method

## Dependencies Available

```groovy
implementation 'com.arcrobotics.ftclib:core:2.1.1'
implementation 'com.acmerobotics.dashboard:dashboard:0.4.16'
// GoBilda Pinpoint SDK available via hwMap.get()
// Standard FTC SDK — LinearOpMode, HardwareMap, IMU, DcMotor, Servo etc.
```

## Deliver

For each class: complete Java file, correct package declaration, all imports, full Javadoc on every public and protected member, no TODOs left in the code. Deliver in priority order: `DevLocaliser` first, then `ROMovementEngine`, then `FOFollower`, then `MyRobot`, then the two tuning OpModes, then `CrawlerStateMachine`.
