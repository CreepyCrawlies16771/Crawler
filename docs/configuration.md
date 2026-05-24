---
title: Configuration Reference
description: Understand every value that controls how Crawler behaves
---

# Configuration Reference

*Deep dive into the tuning values that control your robot*

## The RobotConfig Class

When you tune your robot, Crawler saves numbers that describe exactly how your robot moves. These live in a class called `RobotConfig`:

```java
public class RobotConfig {
    // Hardware values (don't change these)
    public static final int MOTOR_TICK_RESOLUTION = 384;
    
    // Mechanical measurements (from tuning)
    public static final float TRACK_WIDTH = 8.75f;
    public static final float FORWARD_OFFSET = 0.15f;
    
    // Behavior tuning
    public static final float LOOKAHEAD_DISTANCE = 6.0f;
    // ... more values ...
}
```

> 🚨 **Important:** Never put motor or servo names here. Those go in `MyRobot.java`. This class is *only* for the numbers that control movement.

## Localizer Values

These control how your robot knows where it is.

### `TRACK_WIDTH`
**What it is:** The distance between your left and right wheels (in inches)

**Default:** 8.75" (typical for a 28" chassis)

**Range:** 6" to 12"

**If too small:** Robot spins in circles during tuning, odometry drifts

**If too large:** Robot won't turn properly, coordinates off by large amounts

### `FORWARD_OFFSET`
**What it is:** Distance from the center of your robot to the forward odometry pod (in inches)

**Default:** 0.15"

**Range:** -2" to +2"

**If wrong:** Forward/backward movement is accurate, but turning causes position errors

### `LATERAL_MULTIPLIER`
**What it is:** A scaling factor for side-to-side movement (unitless)

**Default:** 1.04

**Range:** 0.95 to 1.15

**If too small:** Strafing doesn't go as far as commanded

**If too large:** Strafing overshoots

### `ROTATION_MULTIPLIER`
**What it is:** A scaling factor for turn accuracy (unitless)

**Default:** 1.0

**Range:** 0.9 to 1.1

**If too small:** Robot doesn't turn as many degrees as commanded

**If too large:** Robot spins too far

### `HEADING_OFFSET`
**What it is:** Correction for the IMU's compass zero point (in degrees)

**Default:** 0

**Range:** -360 to 360

**If wrong:** Robot's heading estimate is rotated compared to reality

## Pure Pursuit Behavior

These control how the pure pursuit algorithm steers your robot.

### `LOOKAHEAD_DISTANCE`
**What it is:** How far ahead the robot looks when following a path (in inches)

**Default:** 6.0"

**Range:** 3.0" to 15.0"

**If too small:** Jerky turns, robot overshoots waypoints

**If too large:** Smooth turns, but robot might cut corners and miss waypoints

### `LATERAL_DISTANCE_COEFFICIENT`
**What it is:** How aggressively to correct side-to-side errors (unitless)

**Default:** 1.0

**Range:** 0.5 to 2.0

**If too small:** Robot drifts sideways and doesn't correct

**If too large:** Robot oscillates (wiggles) side to side

### `HEADING_DISTANCE_COEFFICIENT`
**What it is:** How aggressively to correct rotation errors (unitless)

**Default:** 1.0

**Range:** 0.5 to 2.0

**If too small:** Robot's heading drifts away from desired direction

**If too large:** Robot overshoots heading and wobbles back and forth

## Motion Profile

These control acceleration and deceleration.

### `MOTION_PROFILE_TIME`
**What it is:** How many seconds the robot takes to go from zero to full speed (in seconds)

**Default:** 0.5s

**Range:** 0.2s to 2.0s

**If too small:** Robot jerks suddenly (hard on mechanics)

**If too large:** Acceleration feels sluggish, paths take longer

### `MAX_VELOCITY`
**What it is:** Maximum speed your motors can achieve (in inches per second)

**Default:** 48.0 in/s

**Range:** 30.0 to 80.0 in/s

**If too small:** Autonomous pathfinding never reaches the speeds you specify

**If too large:** Robot commands speeds it can't actually achieve, jerks/stutters

### `MAX_ACCELERATION`
**What it is:** Maximum acceleration available (in inches per second per second)

**Default:** 24.0 in/s²

**Range:** 10.0 to 60.0 in/s²

**If too small:** Paths are sluggish and slow

**If too large:** Robot can't produce this acceleration, paths are jerky

## Motor and Hardware

These describe your hardware and don't usually change.

### `MOTOR_TICK_RESOLUTION`
**What it is:** Encoder ticks per revolution of a motor (for the motors you're using)

**Default:** 384 (for common FTC motors)

**Don't change this** unless you've switched to a different motor type.

### `WHEEL_DIAMETER`
**What it is:** The diameter of your drive wheels (in inches)

**Default:** 3.78" (common FTC wheels)

**Range:** 2.5" to 5.0"

**If wrong:** Driving distances are off by proportional amount

### `DEAD_WHEEL_DIAMETER`
**What it is:** The diameter of your odometry wheels (in inches)

**Default:** 1.5" (standard shaft encoder wheel)

**Range:** 1.2" to 2.0"

**If wrong:** Position tracking is off

## How to Adjust Values

**Option 1: Re-run tuning**

This is the easiest. Just run the tuner again and let it update the values automatically. Takes 45 minutes first time, 5 minutes to re-tune one step.

**Option 2: Manual fine-tuning**

If your robot is close but not perfect:

1. Identify the problem (robot drifts left, overshoots waypoints, etc.)
2. Find the related value from this reference
3. Adjust by 5-10% and test
4. Repeat until happy

**Option 3: Copy from tuning to config**

After running the tuner, copy the values from `/sdcard/Crawler/tune.json` into your `RobotConfig` class to make them permanent.

> 💡 **Start conservative:** Adjust values slowly. Small changes have big effects.

---

## Next Steps

**[Troubleshooting →](troubleshooting.md)** Fix common problems
