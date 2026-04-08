---
title: Pure Pursuit
description: Build smooth AI-like paths that your robot follows automatically
---

# Pure Pursuit

*The smooth path-following magic that competitive teams use*

## What Is Pure Pursuit?

Imagine you're walking toward a point on the floor. You don't stop and check a compass every step. You just keep walking toward it and adjust as you go.

That's pure pursuit.

Your robot looks ahead to a point on the path (the "lookahead point"), steers toward it, and constantly recalculates. The result is smooth, natural-looking motion.

## How Crawler Does It

Here's what happens behind the scenes:

1. You give Crawler a list of waypoints (checkpoints on the field)
2. Crawler builds a smooth path connecting them
3. As your robot moves, Crawler constantly:
   - Checks where you are
   - Looks ahead to the next point on the path
   - Calculates which way to steer
   - Updates your motor power

You just watch it work. No math required.

## The Waypoint API

Build a path one waypoint at a time using this method:

```java
Waypoint.at(x, y)           // Start at coordinates (x, y)
    .speed(0.8)             // Drive at 80% power
    .heading(90)            // Face this direction
    .slow()                 // Slow down at this waypoint
    .onReach(() -> action)  // Do something when arrived
    .buildAll()             // Finish and pass to follower
```

### Creating a Path

Here's a complete example:

```java
follower.follow(
    Waypoint.at(0, 24)
        .speed(0.8)                        // Start at 80% power
        .heading(0)                        // Face forward
        .buildAll(),
    
    Waypoint.at(24, 36)
        .speed(0.8)                        // Maintain 80% power
        .heading(45)                       // Face toward upper right
        .buildAll(),
    
    Waypoint.at(48, 48)
        .speed(0.5)                        // Slow to 50% power
        .heading(45)
        .slow()                            // Use slow mode
        .onReach(() -> robot.openClaw())   // Open claw when arrived
        .buildAll()
);
```

### Each Method Explained

**`.at(x, y)`** — Starting coordinates (in inches from your starting point)

**`.speed(power)`** — How fast to drive (0 to 1, where 1 is full speed)

**`.heading(degrees)`** — Which direction the robot should face
- 0° = forward
- 90° = right
- 180° = backward
- 270° = left

**`.slow()`** — A special preset that slows the robot down for tight turns and end waypoints. Perfect for picking up game pieces or parking precisely.

**`.onReach(() -> action)`** — Trigger an action when your robot arrives at this waypoint. Example: open a claw, raise a lift, spin a mechanism.

**`.buildAll()`** — Tells Crawler "I'm done building this waypoint." This is required at the end of every waypoint.

## Understanding Coordinates

Your field is a simple X-Y grid measured in inches:

```
(0,48)  Forward
  ↑
  │
  │
(0,0) ←──→ (48,0)
Starting Point   Right
```

- **X-axis** — Left (negative) and right (positive)
- **Y-axis** — Backward (negative) and forward (positive)
- **X=0, Y=0** — Where your robot starts

Before every autonomous, set your starting pose:

```java
follower.setStartingPose(new PoseData(0, 0, Math.toRadians(0)));
```

This tells Crawler: "My robot starts at the origin, facing forward."

> 📝 **Note:** Coordinates are in inches from your starting "home" position, not field edges. Zero is always where you start.

## Advanced: Multiple Actions

You can do multiple things when a waypoint is reached:

```java
Waypoint.at(24, 48)
    .heading(90)
    .onReach(() -> {
        robot.openClaw();
        robot.raiseLift();
        telemetry.addData("Reached", "Goal!");
    })
    .buildAll()
```

Just remember: actions that take a long time will make your robot pause.

## Tuning for Your Path

Pure pursuit has two main settings:

**Lookahead Distance** — How far ahead the robot looks when steering. Higher = smoother turns, lower = tighter turns.

**Motion Profile Time** — How quickly the robot accelerates and decelerates.

These are set in your configuration. Your coach or the tuning guide will help you fine-tune them.

> 💡 **Quick fixes:**
> - If corners feel jerky → increase lookahead distance
> - If the robot overshoots waypoints → decrease lookahead distance
> - If the robot accelerates too suddenly → increase motion profile time

## Tips

**Use `.slow()` on final waypoints**

```java
Waypoint.at(48, 48)
    .heading(0)
    .slow()              // Slow down so we don't overshoot
    .onReach(() -> robot.openClaw())
    .buildAll()
```

This prevents the robot from sliding past where it needs to be.

**Larger waypoint spacing = faster, smoother paths**

Don't put waypoints every 2 inches. Space them out to 12+ inches for the best pure pursuit behavior.

**Always align before taking action**

If you need precision, finish with a robot-oriented command:

```java
follower.follow(/* your path */);
ro.turnPID(0);     // Face exactly forward
ro.drivePID(2, 0); // Move forward 2 more inches to hit target
robot.openClaw();
```

---

## Next Steps

**[Tuning →](tuning.md)** Teach Crawler how your specific robot moves
