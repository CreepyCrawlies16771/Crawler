---
title: Tuning
description: Why tuning matters and how to teach Crawler exactly how your robot moves with the 7-step guided tuner
---

# Tuning

*Why tuning matters, what Crawler tunes, and how the 7-step guided tuner works*

## Why tuning matters

Every robot is different. Same chassis, same motors, different wheels — different reality. Without tuning, Crawler's guesses are close but wrong: your robot might drive 22 cm when you asked for 25, or drift sideways while going straight.

Imagine walking toward a target while blindfolded, believing every stride is exactly 30 cm — but your real stride is 25 cm. You'd stop short every single time. A robot is exactly the same, except it can't feel the floor: it only knows encoder ticks and IMU readings.

Tuning teaches Crawler the truth about **your** robot:

- How wide the odometry wheels are apart (**track width**)
- How far the center wheel sits from the robot's center (**center offset**)
- How big the wheels really are and how many ticks a revolution produces (**wheel diameter / ticks per rev**)
- How much power is needed to overcome friction (**min power**)
- How aggressively to correct position and heading errors (**PID gains**)
- How fast to cruise and how far ahead to look (**move speed / follow distance**)

## Why the order matters

You cannot tune PID before odometry is accurate — the PID loop measures its error from odometry, so bad odometry looks like a bad PID controller. And you can't tune path following before the PID loop is stable, because pure pursuit commands drive through the same motors.

```
Odometry (steps 1–4)  →  PID (step 5)  →  Path following (steps 6–7)
```

## Before you start

- ✓ Robot fully wired; all device names in `MyRobot.java` match the Driver Hub configuration
- ✓ Odometry pods mounted, plugged in, spinning freely
- ✓ 3×3 m clear floor space
- ✓ Battery above 80% (voltage affects power — tune at competition conditions)
- ✓ FTC Dashboard open on a laptop on the robot's WiFi

**First tune: ~30–45 minutes.** Re-tuning after a gear swap or rebuild: ~5 minutes.

## Getting started

1. Nothing to sync — the tuner rebuilds your registered robot with the live values
2. Deploy the app
3. Select **Crawler Tuner** (a TeleOp) on the Driver Station
4. Open **FTC Dashboard** in a browser: `http://192.168.43.1:8080/dash`
5. Press **Play**

You'll see a `Crawler Tuner` config panel in the Dashboard — every value the tuner manages lives there. The values are **seeded from your robot's builder** when the tuner starts, so you begin tuning from the numbers already in your robot class (there are no library presets):

```
Crawler Tuner
├── trackWidthIn            (from .setTrackWidth)
├── centerWheelOffsetIn     (from .setCenterWheelOffset)
├── wheelDiameterIn         (from .wheelDiameter)
├── ticksPerRev             (from .ticksPerRev)
├── driveKp / driveKi / driveKd
├── strafeKp / strafeKi / strafeKd
├── steerP / steerI / steerD
├── minPower                (from .minPower)
├── moveSpeed / turnSpeed / followDistanceCm
├── arrivalThresholdCm / orbitThresholdCm
├── timeoutSecs / maxDriveSpeed / turnReferenceRadians
├── slowMoveSpeed / slowTurnSpeed / slowFollowDistanceCm    (only if you use slow-down)
├── slowDownTurnRadians / slowDownTurnAmount                (only if you use slow-down)
```

> 💡 **The workflow:** type values in the Dashboard (or nudge them with the gamepad) → press **RB** to run the test → watch the result on the Driver Station and the Dashboard telemetry → repeat until it passes → **Square** to print the final builder code for your robot's `builder()`.

> ⚠️ **Still at 0?** PID **I/D** terms and the `slow*` values are legitimately 0. But if a **P gain** (`driveKp`, `strafeKp`, `steerP`) is 0, the tuner warns you on the Driver Station — the robot won't move under PID control until you tune it (Step 5).

## The 7 steps

| # | Step | What you do | Pass looks like |
|---|---|---|---|
| 1 | **Motors** | Hold RB / LB / RT / LT — check each wheel spins the right way | All four spin forward |
| 2 | **Encoders** | D-pad to set wheel diameter & ticks/rev; RB spins the drive wheels | Encoder counts climb smoothly |
| 3 | **Track width** | D-pad to adjust; RB spins the robot 10 turns | Odometry rotation matches IMU (within 5°) |
| 4 | **Center offset** | D-pad to adjust; RB strafes 1 m | Heading drift under 2° while strafing |
| 5 | **PID** | Triangle cycles Drive / Strafe / Turn / Min power; D-pad L/R picks P, I, or D; U/D adjusts; RB runs | Smooth stop at target with minimal overshoot |
| 6 | **Auto path** | D-pad adjusts move speed; RB runs a 1 m square | Robot returns near its start |
| 7 | **Finish** | Press Square | Values printed as builder lines for your robot's `builder()` |

## Gamepad controls

| Button | What it does |
|---|---|
| **RB** | Run the current test / measurement |
| **D-pad up / down** | Increase / decrease the current value |
| **D-pad left / right** | In the PID step: pick which term to adjust (P → I → D) |
| **Triangle** | In the PID step: switch test (Drive → Strafe → Turn → Min power) |
| **X** | Go back a step |
| **Circle** | Accept this step and move to the next |
| **Square** | Toggle the builder snippet for your robot (also shown at Step 7) |

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
Circle: next  X: back  Square: builder code
Edit values live in FTC Dashboard -> Crawler Tuner
D-pad U/D: track width  RB: spin test  (rebuilds robot)
trackWidth in: 13.0
Track width OK — paste into your robot
```

## After tuning

1. Press **Square** (or finish Step 7) — the tuner prints the exact tuned values
2. Copy them into the tuned section of your robot's `builder()`
3. Rebuild and deploy
4. Run the **Crawler Smoke Test** — a 2-minute sanity check that odometry is reporting movement

> ⚠️ **Values are not permanent until you paste them.** The Dashboard values live in the tuner's memory only while the app runs. Pasting the printed builder lines into your robot's `builder()` is what makes them permanent.

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
