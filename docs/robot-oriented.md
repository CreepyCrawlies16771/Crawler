---
title: Robot-Oriented Movement
description: Simple step-by-step commands for precise motion
---

# Robot-Oriented Movement

*Telling your robot exactly what to do, step by step*

## What Is Robot-Oriented Movement?

Instead of giving waypoints, you give simple commands:

```java
driveForward(24);           // Drive 24 inches forward
strafeLeft(12);             // Slide 12 inches left
turnClockwise(90);          // Turn 90 degrees right
```

This is more predictable and easier to debug than pure pursuit. It's great for learning, for very precise end-game moves, and for when your path is simple.

## When to Use It

- **End-game parking** — park exactly in the right spot
- **Picking up game elements** — drive to a specific location
- **Fine adjustments** — get your robot lined up perfectly
- **Learning** — understand what your robot is doing

Save pure pursuit for the long, smooth drives. Use robot-oriented for the precise final moves.

## The Commands

Here are the three main commands:

### `drivePID(distance, heading)`

Drive in a straight line while maintaining a specific heading (rotation angle).

```java
ro.drivePID(24, 0);  // Drive 24 inches forward, stay facing forward (0 degrees)
```

**Why have both distance and heading?** If your robot drifts during the drive, this command will correct it automatically.

Parameters:

- `distance` — inches (positive = forward, negative = backward)
- `heading` — degrees (0 = forward, 90 = right, 180 = backward, 270 = left)

### `strafePID(distance, heading)`

Slide sideways while maintaining a heading.

```java
ro.strafePID(12, 0);  // Slide 12 inches right, stay facing forward
```

Same parameters as `drivePID`. Use `positive` for right, `negative` for left.

### `turnPID(degrees)`

Rotate in place to face a new direction.

```java
ro.turnPID(90);  // Turn 90 degrees clockwise
```

## Example Autonomous

Combine commands to build a complete autonomous:

```java
@Override
public void runPath() throws InterruptedException {
    // Reach the goal
    ro.drivePID(24, 0);      // Drive forward 24 inches
    ro.strafePID(12, 0);     // Strafe right 12 inches
    
    // Turn to face the basket
    ro.turnPID(45);
    
    // Back up to line up with target
    ro.drivePID(-6, 45);     // Reverse 6 inches
    
    // Open the claw to drop the specimen
    robot.openClaw();
    sleep(500);              // Wait 0.5 seconds
    
    // Retreat
    ro.drivePID(-24, 45);
}
```

Each command waits for completion before running the next one. That's why this is so predictable.

## Mixing Pure Pursuit and Robot-Oriented

You don't have to choose. Use both in the same autonomous:

```java
@Override
public void runPath() throws InterruptedException {
    // Long smooth drive using pure pursuit
    follower.follow(
        Waypoint.at(0, 12).heading(0).buildAll(),
        Waypoint.at(24, 36).heading(45).buildAll(),
        Waypoint.at(48, 36).heading(45).slow().buildAll()
    );
    
    // Final precise adjustment using robot-oriented
    ro.turnPID(0);           // Face forward exactly
    ro.drivePID(6, 0);       // Move forward 6 more inches
    robot.openClaw();
}
```

> 💡 **Best practice:** Use pure pursuit for the long drives, robot-oriented for the precise final alignments.

## Tips for Accuracy

**Always realign heading after a path.**

When you finish a pure pursuit path, your robot might not be facing exactly the direction you want. Lock it in with a turn:

```java
follower.follow(/* your path */);
ro.turnPID(0);  // Face forward exactly
```

**Keep robot-oriented moves short.**

Moves under 24 inches work best. For longer distances, use pure pursuit.

**If movement seems jerky,** increase the motion profile time in your configuration. Your coach can help with this.

---

## Next Steps

**[Pure Pursuit →](pure-pursuit.md)** Learn to build smooth, flowing paths
