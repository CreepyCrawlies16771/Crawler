---
title: Step-by-Step Tuning Guide
description: Walkthrough of every tuning step, what to do physically, and how to know when you are done
---

*A detailed walkthrough of every tuning step with what to do, what to expect, and how to know when each step passes.*

This page walks you through every step of `CrawlerTuner`. Each section tells you what to do physically, what values to watch, and how to know if the step passed.

If anything feels wrong or you get stuck, check the **Troubleshooting** section at the bottom or reach out in your team Discord.

## Setting up the tuner

Create one file in Android Studio. This is the entire file you write — everything else is handled by Crawler.

```java
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Crawler.Tuning.CrawlerTuner;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@TeleOp(name = "My Tuner", group = "Crawler")
public class MyTuner extends CrawlerTuner {
    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);  // Return your robot instance
    }
}
```

Name the class whatever you want (e.g., `MyTuner` or `TunerOpMode`). Rebuild the app and deploy it.

On the Driver Station, find your new OpMode under the name you gave it (e.g., "My Tuner") and select it. Do not run it yet — first read the control scheme below.

## Gamepad controls

Use your gamepad's buttons to navigate through the tuner. These buttons are the only way to interact with the tuner:

| Button | What it does |
|--------|-------------|
| **Right Bumper (RB)** | Run the current test or measurement |
| **D-pad Up** | Increase the current value |
| **D-pad Down** | Decrease the current value |
| **Left Bumper (LB)** | Toggle between coarse and fine adjustment (see "Adjustment modes" below) |
| **Circle / O** | Accept this step and move to the next one (only works after PASS or MARGINAL) |
| **Triangle** | Go back one step |
| **X (hold 2 seconds)** | Force skip — use only if a step truly cannot be completed |

**Adjustment modes:**
- **Coarse:** Large jumps (e.g., +1.0 or –0.1). Use this to get close Fast.
- **Fine:** Small jumps (e.g., +0.01 or –0.001). Use this for final tweaks. Press Left Bumper to toggle.

## Reading the screen

During tuning, the Driver Hub shows a screen like this:

```
=== Step 2/11: Track Width ===    [14:32]
Elapsed: 18 min 42 sec | Step time: 2 min 11 sec
────────────────────────────────────────────
Current value:   13.24 inches
Heading error:   1.8 degrees

Status: ✓ PASS (error under 2 degrees)
────────────────────────────────────────────
RB: run  ↑↓: adjust (+0.1)  ●: accept  △: back
```

**What each part means:**

- **Step 2/11** — you are on step 2 out of 11 total
- **Current value** — the value being tuned right now (e.g., track width = 13.24")
- **Heading/Position error** — what the tuner is measuring (e.g., 1.8 degrees off)
- **Status line** — the grade: ✓ PASS, ⚠ MARGINAL, or ✗ FAIL
- **Button hints** — reminders of what each button does

**Status meanings:**

- **✓ PASS** — This value is good enough. Press Circle to continue.
- **⚠ MARGINAL** — Close but not perfect. You can press Circle to continue, but consider re-tuning this value later to dial it in more.
- **✗ FAIL** — Not good enough. Adjust and run again before continuing. You cannot press Circle until it passes or is marginal.

## The 11 steps

### Step 0: Hardware Verification

**What this step does:** Checks if all four wheels spin forward.

**What to do:**
1. Place your robot on the floor with no obstructions around it
2. Make sure the battery is fully connected
3. Press Right Bumper

**What happens:** All four motors spin forward at half power for 2 seconds. You should hear them spin and feel gentle air movement from the wheels.

**How to know if it passes:**
- All four wheels spin forward — if even one spins backward, the step fails
- If a wheel spins backward: add `.invert()` to that motor in your `MyRobot` code and restart
- Example: `new Motor(hwMap, "frontLeft").invert()` or just use your motor's invert method

**Tip:** Kneel beside the robot and watch each wheel individually. Take a photo with your phone if you are unsure which direction it spun.

> ⚠️ **Warning:** If a motor spins backward, you must edit `MyRobot.java`, rebuild, and redeploy before continuing tuning. Just pressing Circle without fixing it will cause your tuner to misalign.

---

### Step 1: IMU Verification

**What this step does:** Checks if your IMU (gyro) is oriented correctly.

**What to do:**
1. Clear the floor in front of the robot
2. Place tape on the floor marking exactly 90 degrees to the **left** of the robot's current heading
3. Manually rotate the robot 90 degrees to the left by hand until it points at the tape
4. Open the telemetry on the Driver Hub and watch the heading value

**How to know if it passes:**
- After you rotate the robot left 90 degrees, the heading should read between 80 and 100 degrees
- If it reads close to –90 or +270 instead, the IMU is mounted upside-down — fix it and restart

**What to adjust:**
- If heading reads 270 instead of 90, the IMU needs to be mounted differently or mounted 180 degrees rotated
- Check the IMU documentation on how to mount it

**Tip:** Use a smartphone level app to make sure you are rotating exactly 90 degrees. Anything within 10 degrees is fine.

---

### Step 2: Track Width

**What this step does:** Measures how wide your robot is (left wheel to right wheel), which affects how much the robot drifts sideways.

**What to do:**
1. Clear a 2 meter circle of space on the floor so the robot can spin without hitting walls
2. Place tape on the floor marking the robot's starting heading
3. Press Right Bumper — the robot will spin 10 full rotations clockwise in place
4. After it stops, check the heading on the screen

**What to watch for:**
- The heading should read close to 0 degrees (or very close to where it started)
- If after 10 spins the heading reads 5 degrees, that is good (PASS)
- If it reads 15 degrees, that is too much error (FAIL or MARGINAL)

**How to adjust:**
- Heading reads **positive** (e.g., +12°) — the robot overshot; D-pad down to **decrease** track width
- Heading reads **negative** (e.g., –12°) — the robot undershot; D-pad up to **increase** track width
- Each D-pad press increases or decreases by 0.1 inches in coarse mode

**PASS condition:** Heading error under 2 degrees.

**Tip:** Switch to fine adjustment mode (Left Bumper) once you get close. The finer you tune this, the better your odometry will be overall.

---

### Step 3: Center Wheel Offset

**What this step does:** If you are using a center (perpendicular) encoder wheel, this measures its offset from the robot's center, which affects Y-position accuracy during rotations.

**What to do:**
1. This step only applies if you have a **perpendicular dead wheel** (not if using motor encoders)
2. If you do not have a center wheel, press X for 2 seconds to skip this step
3. If you have a center wheel:
   - Place your robot with room to spin
   - Press Right Bumper — the robot will spin 10 rotations clockwise while you watch the Y value on the screen

**What to watch for:**
- The Y position should stay near 0.0 throughout all 10 spins
- If Y drifts to +5.0, that is bad (FAIL)
- If Y stays between –0.5 and +0.5, that is good (PASS)

**How to adjust:**
- If Y drifts **positive** — D-pad down to decrease offset
- If Y drifts **negative** — D-pad up to increase offset

**PASS condition:** Y drift under 0.5 inches across 10 rotations.

**Tip:** This is one of the trickier steps. If you cannot get it right after a few tries, go back to Step 2 and

 re-tune track width more carefully, then come back to this step.

---

### Step 4: Odometry Accuracy Gate

**What this step does:** Tests if your odometry (odometer) is accurate enough before moving on to motor PID tuning.

**What to do:**
1. Mark a small square on the floor using tape — mark the starting corner clearly
2. Press Right Bumper to start
3. Manually guide your robot forward exactly 24 inches from the marked corner (use a tape measure)
4. Turn the robot 90 degrees left (counterclockwise)
5. Repeat: drive 24 inches, turn 90 degrees left, 4 times total (this draws a square)
6. Try to return to your exact starting corner and exact starting heading
7. The tuner will show you how far the final position is from (0, 0)

**What to watch for:**
- If the tuner shows "error: 0.3 inches" — that is great (PASS)
- If it shows "error: 2.5 inches" — that is too much (FAIL)
- This tells you if your track width and center wheel offset are correct

**If it fails:**
- Go back to Step 2 and Step 3
- Re-tune track width and center wheel offset more carefully
- Come back to this step and try again

**Tip:** The more precisely you drive the square manually, the more accurate your tuner result will be. Use tape to mark each corner and drive slowly.

---

### Step 5: Drive PID

**What this step does:** Tunes `RobotConfig.RobotOriented.Kp` — how hard the robot pushes to reach the exact distance you commanded.

**What to do:**
1. Clear 30 inches of floor space in front of your robot
2. Place tape on the floor marking exactly 24 inches away from your robot's current position
3. Press Right Bumper — the robot drives forward automatically
4. Measure exactly where the robot stopped using the tape on the floor

**What to watch for:**
- Stopped short of the tape: D-pad up (increase Kp — push harder)
- Stopped past the tape: D-pad down (decrease Kp — push softer)
- Oscillating back and forth: decrease Kp even more
- Smooth, confident drive: might be PASS already

**How to adjust:**
- Start in coarse mode (0.005 jumps) to find the ballpark
- Switch to fine mode (0.001 jumps) for the last adjustments
- Each run gives you new data — adjust, run again, repeat

**PASS condition:** Robot stops within 0.5 inches of the 24-inch tape mark.

**Tip:** This step teaches Crawler how much power is needed for smooth, precise driving. A correct Kp value looks like the robot accelerates smoothly, coasts near the end, and stops gently without overshooting. If it stops with a jerk, Kp might be too low.

---

### Step 6: Strafe PID

**What this step does:** Tunes `RobotConfig.RobotOriented.strafe_Kp` — how hard the robot pushes sideways to reach the exact distance using strafe (side-to-side) movement.

**What to do:**
1. Clear 30 inches of floor to the right of your robot's starting position
2. Place tape on the floor marking exactly 24 inches to the right
3. Press Right Bumper — the robot strafes right automatically
4. Measure where the robot stopped

**What to watch for:**
- Strafe is slower to respond than forward drive for most Mecanum robots
- Strafe Kp is often slightly **higher** than Drive Kp
- Same logic as Step 5: short = increase, overshoot = decrease

**PASS condition:** Robot stops within 0.5 inches of the 24-inch tape mark.

**Tip:** If the robot feels sluggish during strafe, do not be afraid to try values higher than your drive Kp. Mecanum wheels are different from tank treads, and strafing often needs a little more push.

---

### Step 7: Turn PID

**What this step does:** Tunes `RobotConfig.RobotOriented.STEER_P` — how accurately the robot turns to a specific angle.

**What to do:**
1. Clear floor space around the robot so it can turn in place
2. Place tape on the floor marking the robot's current heading
3. Place a second piece of tape at exactly 180 degrees behind the robot (straight line)
4. Press Right Bumper — the robot turns 180 degrees in place

**What to watch for:**
- After turning, the heading on screen should read close to 180 degrees
- If it reads 175: close to PASS
- If it reads 190: overshot, needs adjustment

**How to adjust:**
- Heading under 180 (e.g., 170°): D-pad up (turn harder)
- Heading over 180 (e.g., 190°): D-pad down (turn softer)
- Oscillating: decrease STEER_P — the turn is overshooting and correcting too aggressively

**PASS condition:** Final heading within 2 degrees of 180 (so 178–182° is fine).

**Tip:** Turning is sensitive — small changes matter a lot. Start with fine adjustment mode (Left Bumper) to avoid overshooting.

---

### Step 8: Robot-Oriented Integration Test

**What this step does:** Validates that all three PID values (drive, strafe, turn) work together by driving a complete square autonomously.

**What to do:**
1. Mark a square on the floor with tape — make the starting corner clear
2. Press Right Bumper — the robot drives a complete square: 24" forward, 90° turn, 24" forward, 90° turn, repeat 4 times
3. The robot should return to the starting position
4. The tuner shows you how far off it ended

**What to watch for:**
- Error under 2 inches: PASS
- 2–5 inches: MARGINAL (consider re-tuning)
- Over 5 inches: FAIL — re-tune the PID values

**If it fails:**
- Is the forward distance accurate but turning is off? → Go back to Step 7 and re-tune STEER_P
- Is both forward and turning off? → Go back to Step 5 and re-tune Drive Kp first
- Is it consistently drifting left or right after turns? → The IMU or center wheel offset might be wrong; consider re-tuning Step 1 or Step 3

**Tip:** This test shows you how well your odometry and PID work together. If you make it through this step with a small error, you are ready for field-oriented tuning.

---

### Step 9: Follow Distance

**What this step does:** Tunes `RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE` — the "look-ahead" distance that tells the robot how far ahead to look when cornering, which controls smoothness.

**What to do:**
1. Mark an L-shaped path on the floor with tape: drive 24 inches forward, then turn 90 degrees right, then drive 24 inches right
2. Press Right Bumper — the robot follows this L-path autonomously using pure pursuit
3. Watch how the robot handles the corner
4. After the test, the tuner tells you how far off the final position is

**What to watch for:**
- Path is smooth and confident: likely PASS
- Robot cuts the corner too aggressively and goes off the path: D-pad down (decrease follow distance)
- Robot is too sluggish on the corner, slows down a lot: D-pad up (increase follow distance)
- Robot oscillates (wiggles) at waypoints: D-pad down
- Good starting range: 8–12 inches (depends on your robot size)

**How to adjust:**
- Start at 10 inches and run the test
- If it cuts corners: lower to 8 inches
- If it is too slow: raise to 12 inches
- Fine-tune from there

**PASS condition:** The robot follows the L-path smoothly without major errors, and the final position is within 3 inches of the target. If you cannot get PASS, MARGINAL is acceptable — you can re-tune this later.

**Tip:** Follow distance is one of the easiest values to tune live using FTC Dashboard (see [Using FTC Dashboard with Crawler](ftc-dashboard.md)). Try a few values quickly in the browser, then copy the best one back into your code.

---

### Step 10: Field-Oriented Integration Test

**What this step does:** Validates that pure pursuit and follow distance work correctly by following a complete square path on the field.

**What to do:**
1. Mark a four-corner square on the floor with tape
2. Press Right Bumper — the robot autonomously follows the four corners in order using pure pursuit
3. Watch it drive each leg and handle each corner
4. It should return to the starting position

**What to watch for:**
- Smooth turns: good sign
- Oscillating at waypoints: follow distance might be too small
- Cutting corners: follow distance might be too large
- Error under 3 inches: PASS

**If it fails:**
- Re-check Step 4 (Odometry Accuracy) — if odometry is off, FO will be off
- Re-tune Step 9 (Follow Distance) — the most common culprit
- Re-check PID values from Steps 5–7 — if PID is wrong, the robot cannot track accurately

**Tip:** This is the final validation step. If you pass this, your robot is ready for autonomous missions.

---

### Step 11: Tuning Complete

When you press Circle after Step 10 (or select a final step), you see:

```
=== Tuning Complete! ===

Total time: 52 min 14 sec

Your tuned values:
  Track width:          13.24 inches
  Center wheel offset:   3.47 inches
  Drive Kp:             0.048
  Strafe Kp:            0.051
  STEER_P:              0.031
  Follow distance:       9.5 inches

Saved to: /sdcard/Crawler/tune.json
Copy these values into RobotConfig.java
```

## Copying values into RobotConfig.java

After tuning completes, you must copy the tuned values into your `RobotConfig.java` file in Android Studio.

Open `RobotConfig.java` and update the `@Config` classes with your tuned values. Fill in the highlighted lines with **your exact tuned numbers**:

```java
public static class Odometry {
    public static double TRACK_WIDTH         = 13.24; // ← YOUR TUNED VALUE
    public static double CENTER_WHEEL_OFFSET = 3.47;  // ← YOUR TUNED VALUE
    public static double WHEEL_DIAMETER      = 1.37795;
    public static double TICKS_PER_REV       = 2000;
}

public static class RobotOriented {
    public static double Kp        = 0.048; // ← YOUR TUNED VALUE
    public static double Ki        = 0.0;
    public static double Kd        = 0.0;
    public static double strafe_Kp = 0.051; // ← YOUR TUNED VALUE
    public static double strafe_Ki = 0.0;
    public static double strafe_Kd = 0.0;
    public static double STEER_P   = 0.031; // ← YOUR TUNED VALUE
    public static double STEER_I   = 0.0;
    public static double STEER_D   = 0.0;
    public static double MIN_POWER = 0.15;
}

public static class FieldOriented {
    public static double DEFAULT_MOVE_SPEED            = 0.7;
    public static double DEFAULT_TURN_SPEED            = 0.4;
    public static double DEFAULT_FOLLOW_DISTANCE       = 9.5; // ← YOUR TUNED VALUE
    public static double DEFAULT_FOLLOW_ANGLE          = 0.0;
    public static double ARRIVAL_THRESHOLD             = 2.0;
    public static double ORBIT_THRESHOLD               = 10.0;
    // ... leave other values at defaults
}
```

**Key points:**
- Only change the values that were tuned (highlighted above)
- Leave all other values at the defaults
- Double-check your numbers before saving
- After saving, rebuild and redeploy the app

> ⚠️ **Warning:** After copying values into `RobotConfig.java`, you must rebuild and redeploy the app for those values to take effect. The tuner applies values live during a session, but they reset to the default `RobotConfig` values when the app closes. Without rebuilding, your next autonomous match will use the old defaults, not your tuned values.

## Troubleshooting

| Problem | Likely cause | Fix |
|---------|--------------|-----|
| Motor spins backward in Step 0 | Motor is wired backwards or needs to be inverted | Add `.invert()` to that motor in `MyRobot` |
| IMU heading wrong in Step 1 | IMU is mounted upside-down or rotated | Check IMU mounting orientation |
| Cannot get track width to pass (Step 2) | IMU is drifting; odometry pods dirty or loose | Clean encoder wheels; check IMU calibration |
| Oscillation in PID steps (5-7) | Kp value too high | Decrease Kp significantly and try again |
| Robot does not move in PID steps | Kp is 0 or too low | Increase Kp |
| Follow distance test is jerky (Step 9) | Follow distance too small; path is short | Increase follow distance; test with longer paths |
| Robot overshoots waypoints (Step 10) | Follow distance too large or PID is wrong | Decrease follow distance or re-tune PID |

## Next steps

Once you have successfully completed all steps and copied the values into `RobotConfig.java`, your robot is tuned and ready for autonomous missions!

If you want to visualize your robot's movement in real time during testing, see [Using FTC Dashboard with Crawler](ftc-dashboard.md).
