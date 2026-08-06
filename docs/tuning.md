---
title: Tuning
description: Teach Crawler exactly how your robot moves with the 7-step guided tuner
---

# Tuning

*The most important step: teaching Crawler how your specific robot behaves*

Every robot is different. Same chassis, same motors, different wheels — different reality. Without tuning, Crawler's guesses are close but wrong: your robot might drive 22 cm when you asked for 25, or drift sideways while going straight.

The **Crawler Tuner** fixes that. It walks you through seven short tests, and every value can be edited **live in the FTC Dashboard** — you type numbers in the browser, the robot rebuilds itself, and you re-test. No recompiling between adjustments.

**First tune: ~30–45 minutes.** Re-tuning after a gear swap or rebuild: ~5 minutes.

## Getting started

1. Nothing to sync — the tuner rebuilds `MyRobot.builder()` with the live values
2. Deploy the app
3. Select **Crawler Tuner** (a TeleOp) on the Driver Station
4. Open **FTC Dashboard** in a browser: `http://192.168.43.1:8080/dash`
5. Press **Play**

You'll see a `Crawler Tuner` config panel in the Dashboard — every value the tuner manages lives there:

```
Crawler Tuner
├── trackWidthIn          13.0
├── centerWheelOffsetIn    3.5
├── wheelDiameterIn      1.37795
├── ticksPerRev           2000
├── driveKp / driveKi / driveKd
├── strafeKp / strafeKi / strafeKd
├── steerP / steerI / steerD
├── minPower              0.15
├── moveSpeed / turnSpeed / followDistanceCm
├── arrivalThresholdCm / orbitThresholdCm
├── timeoutSecs / maxDriveSpeed
```

> 💡 **The workflow:** type values in the Dashboard (or nudge them with the gamepad) → press **RB** to run the test → watch the result on the Driver Station and the Dashboard telemetry → repeat until it passes → **Square** to print the final builder code for `MyRobot.java`.

## The 7 steps

| # | Step | What you do | Pass looks like |
|---|---|---|---|
| 1 | **Motors** | Hold RB / LB / RT / LT — check each wheel spins the right way | All four spin forward |
| 2 | **Encoders** | D-pad to set wheel diameter & ticks/rev; RB spins the drive wheels | Encoder counts climb smoothly |
| 3 | **Track width** | D-pad to adjust; RB spins the robot 10 turns | Odometry rotation matches IMU (within 5°) |
| 4 | **Center offset** | D-pad to adjust; RB strafes 1 m | Heading drift under 2° while strafing |
| 5 | **PID** | Triangle cycles Drive / Strafe / Turn / Min power; D-pad L/R picks P, I, or D; U/D adjusts; RB runs | Smooth stop at target with minimal overshoot |
| 6 | **Auto path** | D-pad adjusts move speed; RB runs a 1 m square | Robot returns near its start |
| 7 | **Finish** | Press Square | Values printed as builder lines for `MyRobot.builder()` |

## Gamepad controls

| Button | What it does |
|---|---|
| **RB** | Run the current test / measurement |
| **D-pad up / down** | Increase / decrease the current value |
| **D-pad left / right** | In the PID step: pick which term to adjust (P → I → D) |
| **Triangle** | In the PID step: switch test (Drive → Strafe → Turn → Min power) |
| **X** | Go back a step |
| **Circle** | Accept this step and move to the next |
| **Square** | Toggle the `MyRobot.builder()` snippet (also shown at Step 7) |

Everything you can do with the gamepad you can also do by typing into the Dashboard — the two stay in sync automatically.

> 💡 **The tuner uses the real movement engine.** The PID tests (Step 5) run through the exact same `RobotOrientedDrive` (`drivePID` / `strafePID` / `turnPID`) loops your autos use — not a simplified copy — so the values you tune behave identically in a match.

## Watching it live on FTC Dashboard

The tuner streams everywhere at once:

- **Telemetry panel** — every test reports error, power, and the live P/I/D terms, on both the Driver Station and the Dashboard
- **Field view** — the robot is drawn live while it spins (Step 3), strafes (Step 4), and runs the PID / min-power tests (Step 5)
- **Config panel** — type any value to change it instantly

## What the screen shows

```
Crawler Tuner | Step 3/7: Track width
Circle: next  X: back  Square: MyRobot builder code
Edit values live in FTC Dashboard -> Crawler Tuner
D-pad U/D: track width  RB: spin test  (rebuilds robot)
trackWidth in: 13.0
Track width OK — paste into MyRobot
```

## After tuning

1. Press **Square** (or finish Step 7) — the tuner prints the exact tuned values
2. Copy them into the tuned section of `MyRobot.builder()`
3. Rebuild and deploy
4. Run the **Crawler Smoke Test** — a 2-minute sanity check that odometry is reporting movement

> ⚠️ **Values are not permanent until you paste them.** The Dashboard values live in the tuner's memory only while the app runs. Pasting the printed builder lines into `MyRobot.builder()` is what makes them permanent.

## Re-tuning

Your robot feels off? Re-run the tuner and adjust just the affected value in the Dashboard:

| Symptom | Tune |
|---|---|
| Robot drives 90 cm when told 100 | `wheelDiameterIn` / `ticksPerRev` (Step 2) |
| Drifts sideways going straight | `trackWidthIn` (Step 3) |
| Spins while strafing | `centerWheelOffsetIn` (Step 4) |
| Overshoots targets / oscillates | Step 5 PID gains |
| Robot never starts moving at low power | `minPower` (Step 5) |
| Overshoots waypoints on paths | `moveSpeed`, `followDistanceCm` (Step 6) |

---

## Next Steps

**[Step-by-Step Guide →](tuning-guide.md)** What to do physically for every step
