---
title: Tuning
description: Teach Crawler exactly how your specific robot moves using the guided tuner
---

# Tuning

*The most important step: teaching Crawler how your robot behaves*

## Why Tuning Matters

Here's the thing: every robot is different.

Even if two robots look identical, their motors have slightly different power curves, their wheels have different traction, and their weight distribution is unique. Without tuning, Crawler doesn't know how *your specific* robot moves.

**Without tuning:** Your robot might drive 24 inches when you ask it to drive 22 inches, or it might only drive 20 inches.

**With tuning:** Crawler knows your robot inside and out and compensates automatically.

The good news: Crawler has a guided tuner that walks you through everything step by step. It takes 45 minutes the first time. Future re-tunings take 5 minutes.

## The Guided Tuner

The tuner is a special autonomous that runs a series of tests and learns from them.

Create a `MyTuner.java` file:

```java
import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerTuner;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Tuner", group = "Tuning")
public class MyTuner extends CrawlerTuner<MyRobot> {
    @Override
    public void buildRobot() {
        robot = new MyRobot();
    }
}
```

That's literally it. The tuner handles everything. One file. Done.

## The 11 Tuning Steps

Here's what the tuner will ask you to do:

| Step | Name | What You Do | Time |
|------|------|-----------|------|
| 1 | **Verify Hardware** | Move each motor to confirm direction | 2 min |
| 2 | **Motor Powers** | Hold buttons to find each motor's max speed | 3 min |
| 3 | **Track Width** | Drive straight; tuner calculates wheelbase | 3 min |
| 4 | **Forward Offset** | Position sensor; tuner calibrates | 2 min |
| 5 | **Lateral Multiplier** | Strafe straight; tuner auto-corrects | 3 min |
| 6 | **Localizer Forward** | Drive in a line; tuner measures accuracy | 5 min |
| 7 | **Localizer Strafe** | Strafe in a line; tuner measures accuracy | 5 min |
| 8 | **Rotation Multiplier** | Turn in a circle; tuner calibrates rotations | 3 min |
| 9 | **IMU Heading Offset** | Robot faces a marking; tuner sets heading | 2 min |
| 10 | **Max Acceleration** | Robot accelerates; tuner measures limits | 3 min |
| 11 | **Finalize** | Tuner saves all values | 1 min |

> 💡 **First-time tuning = 45-60 minutes total.** Just follow what the tuner says. You'll get one warning if something seems wrong, but re-run the previous step and try again.

## Gamepad Controls

The tuner uses your gamepad a lot:

| Button | Action |
|--------|--------|
| A | Confirm, move to next step |
| B | Go back one step |
| X | Pause/resume tuning |
| Y | Skip this step (only if instructed) |
| Right bumper | Increase value |
| Left bumper | Decrease value |
| Right trigger | Increase value (fine) |
| Left trigger | Decrease value (fine) |
| D-pad up/down | Adjust value |

Pay attention to the screen. The tuner tells you what to press.

## What to Expect

**Some steps feel manual.** In step 2, you'll hold buttons and watch motors spin. That's fine — this is the tuner learning.

**You'll be asked to drive straight.** Steps 6 and 7 ask you to drive your robot in a straight line for a known distance. Make sure your floor is clear and you have 10+ feet of space.

**If a step shows FAIL,** don't skip it. Instead:
1. Go back to the previous step
2. Re-run the previous step
3. Try this step again

**Keep tuning values,** they're saved to your robot's onboard storage.

## After Tuning

When tuning finishes, Crawler saves all values to:

```
/sdcard/Crawler/tune.json
```

These values apply immediately to all your autonomouses.

**To make values permanent** (so they survive a phone reset), copy them into `RobotConfig`:

```java
public class RobotConfig {
    public static final float TRACK_WIDTH = 8.75f;      // Value from tuning
    public static final float FORWARD_OFFSET = 0.15f;
    public static final float LATERAL_MULTIPLIER = 1.04f;
    // ... more values ...
}
```

Your coach can help with this if needed.

## Re-Tuning

If your robot feels off later (overshooting waypoints, drifting, etc.), you can re-tune just the affected step:

1. Open the tuner again
2. Press **Y** to skip steps until you reach the problematic one
3. Re-run that step
4. Continue to the end

Or run the full tuning again — it only takes 5 minutes the second time once you know what to expect.

## Tips

**Don't rush.** If the tuner is at step 6 and you're not confident, press B to go back and review.

**Clear the floor.** Steps that involve driving need 10+ feet of clear space.

**Use a straight line on the floor.** Use tape or chalk to mark where "straight" is.

**Keep the robot still between steps.** Unless the tuner tells you to move, keep your robot in the same spot.

**Tuning is robot-specific.** If your team building a new robot, you tuned it once, don't need to re-tune. If you change wheels, motors, or geometry, re-tune.

---

## Next Steps

**[Configuration Reference →](configuration.md)** Understand all the tuning values and what they do
