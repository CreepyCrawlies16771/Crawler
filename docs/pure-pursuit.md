---
title: Pure Pursuit
description: How FOFollower steers smooth paths with a look-ahead point
---

# Pure Pursuit

*The smooth path-following behind `FOFollower`*

## The idea

Instead of commanding "turn 30°, drive 40 cm, turn…", pure pursuit continuously asks: *"if I look a fixed distance ahead on the path, where is that point, and which way should I steer to reach it?"* The robot recomputes this every loop, so paths come out smooth and curved — no stop-and-turn.

<div class="diagram" role="img" aria-label="Pure pursuit lookahead geometry">
<svg viewBox="0 0 640 380" xmlns="http://www.w3.org/2000/svg" font-family="'JetBrains Mono', monospace">
  <defs>
    <linearGradient id="pp" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0" stop-color="#4ADE80"/><stop offset="1" stop-color="#22C55E"/>
    </linearGradient>
  </defs>
  <rect x="30" y="30" width="580" height="320" rx="14" fill="#0f1720" stroke="#374151"/>
  <g stroke="#4ADE80" stroke-width="3" fill="none">
    <path d="M70 300 L250 300 L330 150 L520 90"/>
  </g>
  <g fill="#22C55E">
    <circle cx="70" cy="300" r="7"/><circle cx="250" cy="300" r="7"/>
    <circle cx="330" cy="150" r="7"/><circle cx="520" cy="90" r="7"/>
  </g>
  <g fill="#9CA3AF" font-size="12" text-anchor="middle">
    <text x="70" y="330">wp 0</text><text x="250" y="330">wp 1</text>
    <text x="330" y="138">wp 2</text><text x="520" y="78">wp 3</text>
  </g>
  <g stroke="#E5E7EB" stroke-width="1.5" stroke-dasharray="6 5" fill="none">
    <circle cx="150" cy="292" r="80"/>
  </g>
  <g fill="#4ADE80">
    <rect x="120" y="262" width="60" height="60" rx="8" transform="rotate(-12 150 292)"/>
  </g>
  <g fill="#E5E7EB" font-size="13">
    <text x="150" y="420" visibility="hidden">.</text>
    <text x="60" y="120" fill="#4ADE80">robot</text>
    <text x="60" y="140" fill="#9CA3AF" font-size="12">look-ahead circle</text>
  </g>
  <g stroke="#f59e0b" stroke-width="3" fill="none">
    <circle cx="205" cy="288" r="7"/>
  </g>
  <g fill="#f59e0b" font-size="12">
    <text x="205" y="275" text-anchor="middle">follow point</text>
  </g>
  <g fill="#9CA3AF" font-size="12">
    <text x="120" y="375">The robot steers toward where the look-ahead circle crosses the path.</text>
  </g>
</svg>
</div>

## The Waypoint API

Waypoints are built with `Waypoint.at(x, y, robot.config)` — **x and y are centimeters in the field frame**:

```java
Waypoint.at(x, y, robot.config)     // use — pulls your tuned defaults
    .speed(0.8)                     // move speed override (0.0–1.0)
    .turnSpeed(0.4)                 // turn power override
    .followDistance(20.0)           // look-ahead radius for this leg (cm)
    .slow(robot.config)             // slow-mode preset (see configuration.md)
    .onReach(() -> robot.openClaw())// run when the robot arrives
    .build()                        // required — finish the waypoint
```

The builder also exposes `.heading(...)` and `.slowDown(radians, amount)` — both set fields on the waypoint, but the current `FOFollower` never reads them, so they have **no effect on the path yet**. To shape a leg today, use `.speed(...)`, `.turnSpeed(...)`, and `.followDistance(...)`.

### A complete path

```java
FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

robot.resetPose();   // required before following (CRWL-101 otherwise)

follower.follow(
    Waypoint.at(0, 0, robot.config).build(),
    Waypoint.at(100, 0, robot.config).speed(0.8).build(),
    Waypoint.at(100, 100, robot.config)
        .slow(robot.config)
        .onReach(robot::scoreHighBasket)
        .build(),
    Waypoint.at(0, 0, robot.config).speed(0.7).build()
);
```

## How `follow()` behaves

- **Blocks** until the whole path is done (or the OpMode stops) — it's a `LinearOpMode`-style call
- Needs **at least 2 waypoints**; the first is the start point
- Reaches a waypoint when the robot is within `arrivalThresholdCm` (default 5 cm)
- Fires that waypoint's `onReach` **once**, then moves on
- Aborts a leg after `timeoutSecs` (default 5 s) and logs a warning
- `RobotMovement.goToPosition(...)` fades turn power to zero as the robot approaches (via `orbitThresholdCm`) so it doesn't over-rotate at corners

## Tuning the path feel

| Symptom | Tune |
|---|---|
| Jerky, wiggly corners | **Increase** `followDistanceCm` (look further ahead) |
| Cuts corners / misses waypoints | **Decrease** `followDistanceCm` |
| Overshoots waypoints | Lower `moveSpeed` or add `.slow(robot.config)` to the last waypoint |
| Too sluggish | Raise `moveSpeed` |
| Over-rotates at waypoints | Raise `orbitThresholdCm` |
| Fires `onReach` too early / never | Lower / raise `arrivalThresholdCm` |

## Tips

**Space waypoints out.** 12+ cm apart gives pure pursuit room to work. Dense waypoints make the path wiggly.

**Slow down at the end.**

```java
Waypoint.at(80, 80, robot.config)
        .slow(robot.config)              // precision approach
        .onReach(robot::scoreHighBasket)
        .build()
```

**Finish with a precise move.** Pure pursuit gets you near; `drivePID`/`strafePID` get you exact:

```java
import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.RobotOrientedDrive;

RobotOrientedDrive ro = new RobotOrientedDrive(robot, this::opModeIsActive, telemetry);

follower.follow(
    Waypoint.at(0, 0, robot.config).build(),
    Waypoint.at(48, 36, robot.config).speed(0.8).build()
);
ro.drivePID(0.15, 90);   // inch in 15 cm, hold 90° — see robot-oriented.md
robot.openClaw();
```

**Watch it live.** `FOFollower` streams target / distance / elapsed telemetry to the Driver Station and Dashboard telemetry panels. (The Dashboard *field view* is currently drawn only by the tuner's own tests — see [FTC Dashboard](ftc-dashboard.md).)

---

## Next Steps

**[Robot-Oriented Movement →](robot-oriented.md)** Precise `drivePID` / `strafePID` / `turnPID`
