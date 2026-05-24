---
title: Tuning Your Robot
description: Learn why tuning matters and how Crawler guides you through every step
---

*What tuning is, why it matters, and how Crawler makes it simple*

## Why tuning matters

Imagine trying to walk to a target while blindfolded. If someone told you that every step is exactly 12 inches but your actual stride is only 10 inches, you would always stop short. Your brain would need to re-learn how many steps it actually takes to reach the target.

Your robot has the same problem.

When you build your robot, the encoder wheels measure how fast and how far the motors turn. But Crawler doesn't automatically know your robot's exact size, wheel diameter, or how the wheels are positioned. Without tuning, Crawler makes guesses — and guesses lead to errors that get bigger the farther the robot travels.

Tuning teaches Crawler the truth about **your specific robot**. It tells Crawler:
- How wide your robot actually is
- How fast your wheels actually spin
- How much power to apply to stop exactly where you want
- How to follow a smooth path without overshooting corners

Once you tune, Crawler can drive accurately across the field, turn predictably, and follow complex paths — all without overshooting, drifting, or crashing.

## What you will tune

Crawler has three areas to tune, depending on which movement commands you use:

| Area | What it controls | When you need it |
|------|-----------------|-----------------|
| **Odometry** | How accurately the robot tracks its position on the field | Always — needed for any autonomous |
| **Robot-Oriented PID** | How precisely commands like `drivePID()` and `turnPID()` stop at the exact distance or angle | If you use robot-oriented commands |
| **Pure Pursuit** | How smoothly the robot follows waypoint paths without cutting corners or oscillating | If you use field-oriented or path-following commands |

Most teams need all three.

## How Crawler's tuner works

Crawler includes a single OpMode called `CrawlerTuner` that guides you through every tuning step in the correct order.

**The key insight:** You cannot tune PID before odometry is correct, and you cannot tune field-oriented movement without solid PID values. Crawler locks steps in this order so you always tune the foundation first.

Here is what makes it different from tuning manually:

- **One OpMode does everything** — no separate scripts or external tools
- **Steps cannot be skipped** — they are locked in the right order
- **Each step shows you exactly what to do** — press Right Bumper, adjust with D-pad, press Circle when done
- **Each step grades itself** — you see PASS ✓, MARGINAL ⚠, or FAIL ✗ on the Driver Hub screen
- **Values are visible live** — telemetry on the Driver Hub updates as you adjust
- **Optional live visualization** — FTC Dashboard shows your robot's position on a field view in real time
- **Results saved automatically** — all tuned values are saved to `/sdcard/Crawler/tune.json` so you do not lose them

## The V1.0 workflow

Here is the honest truth about V1.0: after the tuner finds your values, you must manually copy them into `RobotConfig.java`, rebuild the app, and redeploy to make them permanent.

A future version of Crawler will load values from the saved JSON file automatically — but V1.0 is intentionally simple, and this manual step keeps it that way.

The workflow is straightforward:

```
1. Run the tuner → see the tuned value on Driver Hub
2. Write that value into RobotConfig.java in Android Studio
3. Build and deploy the app
4. Run the tuner again to verify (values now load from RobotConfig)
```

You repeat this loop for each area you tune (Odometry, then Robot-Oriented, then Pure Pursuit).

> 📝 **Note:** In a future version of Crawler, this manual copy step will go away — Crawler will load the saved values automatically from the JSON. For V1.0, you just need to spend 2 minutes typing numbers across once per area. It is not hard, just a little repetitive.

## Before you start

Check this checklist before running the tuner for the first time:

- ✓ Robot is fully built and wired
- ✓ All motor names in `MyRobot.java` match the Driver Hub configuration **exactly** (e.g., `"frontLeft"` must be named `frontLeft` in the Driver Hub)
- ✓ Odometry encoder pods are mounted, plugged in, and can rotate freely
- ✓ You have a clear 3×3 meter space on the floor to test (or at least enough room for the steps you are running)
- ✓ Battery is charged above 80% (low battery causes power inconsistencies)
- ✓ FTC Dashboard is open in a browser on a laptop connected to the robot's WiFi (optional but highly recommended for steps 9 and 10)

If any of these are missing, fix them now before starting. A wrong motor name or loose encoder pod will make tuning impossible.

## Next steps

When you are ready, move to [Step-by-step tuning guide](tuning-guide.md) to start the tuner.
