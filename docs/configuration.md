---
title: Configuration Reference
description: Every CrawlerRobot.Config value, its default, and what it controls
---

# Configuration Reference

*Every value that controls how Crawler moves, and how to set it*

Your robot's tuning values live in **one place**: the builder chain in `MyRobot.builder()`. Those numbers fill in `CrawlerRobot.Config`, which every movement system reads. The **Crawler Tuner** prints the matching builder lines to paste into `builder()` when you're done — this page explains each value so you can also adjust them by hand.

> 🚨 **Never put motor or servo *names* here.** Device names are string constants at the top of `MyRobot.java`. This config is only numbers.

## Units

- **inches** — track width, center offset, wheel diameter
- **centimeters** — waypoint coordinates, follow distance, arrival/orbit thresholds
- **power** — speeds are 0.0–1.0
- **per meter / per degree** — PID gains

## Odometry

> 🚨 **No library defaults.** `CrawlerRobot.Config` starts every value at 0 — each one
> below must be set in your builder chain, or the robot refuses to build. The Crawler
> Tuner's values are seeded from that builder, so you always start from your robot.

| Builder method | Config field | Unit | What it does |
|---|---|---|---|
| `.setTrackWidth(x)` | `trackWidth` | in | Distance between the two parallel odometry wheels. Controls how wheel rotation becomes heading change. Too small → odometry "over-rotates" and the robot drifts sideways. |
| `.setCenterWheelOffset(x)` | `centerWheelOffset` | in | How far the perpendicular center wheel sits forward of the robot center. Wrong → the robot appears to rotate while strafing. |
| `.wheelDiameter(x)` | `wheelDiameter` | in | Diameter of the odometry wheels. Converts ticks → distance. 35 mm pod = 1.37795. |
| `.ticksPerRev(x)` | `ticksPerRev` | ticks | Encoder counts per full wheel revolution. GoBILDA 5203 pods: 2000; REV built-ins: 560. |

`ticksPerRev` and `wheelDiameter` combine into `ticksPerMeter()` (used internally):

```
ticksPerMeter = ticksPerRev / (wheelDiameter × 0.0254 × π)
```

If every distance is off by the same ratio, fix these two — not the PID.

## Robot-relative PID

| Builder method | Config field | What it does |
|---|---|---|
| `.drivePid(kp, ki, kd)` | `driveKp` / `driveKi` / `driveKd` | Gains on forward distance error (per meter). |
| `.strafePid(kp, ki, kd)` | `strafeKp` / `strafeKi` / `strafeKd` | Gains on strafe distance error (per meter). Often needs a bit more push than drive. |
| `.steerPid(p, i, d)` | `steerP` / `steerI` / `steerD` | Gains on heading error (per degree). Used to hold a heading while driving and to turn in place. |
| `.minPower(x)` | `minPower` | Smallest power that overcomes static friction. Below this the motors may not move at all. |

**How the loop uses them** (both `TuningPidRunner` and `RobotOrientedDrive`):

```
power = clamp( Kp × error + Ki × ∫error dt + Kd × d(error)/dt )
```

- **P** — main force toward the target. Too low: stops short. Too high: oscillates.
- **I** — fixes steady-state error (stopping a couple of cm short). The loops reset the integral when the error sign flips, so windup is limited.
- **D** — damps oscillation. Add only if the robot wobbles around the target.
- **minPower** — deadband applied when commanded power is near zero.

## Path following

| Builder method | Config field | Unit | What it does |
|---|---|---|---|
| `.pathDefaults(move, turn, follow)` | `defaultMoveSpeed` | power | Cruise power while traveling between waypoints. |
| | `defaultTurnSpeed` | power | Power scale for heading correction during paths. |
| | `followDistanceCm` | cm | Pure-pursuit look-ahead radius (10"). Smaller = tighter corners, more jitter; larger = smoother, cuts corners. |
| `.arrivalThresholdCm(x)` | `arrivalThresholdCm` | cm | Within this distance of a waypoint, the robot counts as "arrived" and fires `onReach`. |
| `.orbitThresholdCm(x)` | `orbitThresholdCm` | cm | Distance over which turn power fades to zero as the robot approaches a waypoint, so it glides in without over-rotating. |
| `.timeoutSecs(x)` | `timeoutSecs` | s | Max seconds a waypoint may take before the follower aborts that leg. |
| `.turnReferenceRadians(x)` | `turnReferenceRadians` | rad | Heading-error scale for path turn power — typically `Math.toRadians(30)`. |
| `.maxDriveSpeed(x)` | `maxDriveSpeed` | power | Clamps every `drive(...)` / `driveFieldRelative(...)` input. |

### Waypoint per-waypoint overrides

`Waypoint.at(x, y, robot.config)` copies the defaults, and each waypoint can override them:

```java
Waypoint.at(60, 0, robot.config)
        .speed(0.8)                  // override move speed
        .turnSpeed(0.4)              // override turn speed
        .followDistance(20.0)        // override look-ahead for this leg
        .slow(robot.config)          // use slowMoveSpeed / slowTurnSpeed / slowFollowDistanceCm
        .onReach(() -> robot.openClaw())
        .build();
```

The slow-mode values come from your builder (0 = not used):

| Builder method | Config field | Unit | Used by `.slow(config)` |
|---|---|---|---|
| `.slowSpeeds(move, turn, followCm)` | `slowMoveSpeed` | power | Cruise power in slow mode |
| | `slowTurnSpeed` | power | Turn power in slow mode |
| | `slowFollowDistanceCm` | cm | Tighter look-ahead in slow mode (5") |
| `.slowDownTurn(radians, amount)` | `slowDownTurnRadians` | rad | Heading-error trigger to start slowing |
| | `slowDownTurnAmount` | power | How much turn power drops in slow mode |

## Where to change values

| Situation | Where |
|---|---|
| Final, permanent values | builder chain in `MyRobot.builder()` |
| Live experiment during tuning | FTC Dashboard → `Crawler Tuner` |
| Per-path tweaks | `Waypoint` overrides (`.speed()`, `.slow()`, …) |

> 💡 **All field distances are centimeters, all builder odometry sizes are inches.**
> Use the `UnitConverter` utility (`UnitConverter.inToCm(...)`, `UnitConverter.cmToIn(...)`) to
> convert when you measure in the other unit — see the [API reference](api-reference.md#unitconverter).

> 💡 **Start conservative.** Change one value at a time and re-test. PID and track width interact — a big jump in one can look like a problem in another.

---

## Next Steps

**[Troubleshooting →](troubleshooting.md)** Fix common problems
