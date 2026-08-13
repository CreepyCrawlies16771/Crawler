---
title: Tuning Guide
description: Tab-by-tab walkthrough of every Crawler Tuner test — what each one tunes, how to run it, and how to know when it passes
---

# Tuning Guide

*Every tuner in the Crawler Tuner, tab by tab — what it tunes, how the test runs, and how to read the result.*

The **Crawler Tuner** runs **9 tuning tests** that calibrate every number your robot moves with. They appear on-screen as 7 steps; Step 5 (PID) is really four different tests, so this guide splits it into four tabs.

> 🔁 **Read the tabs in order (1 → 10).** Odometry first, PID next, paths last. You cannot tune PID against wrong odometry, and you cannot tune paths against an unstable PID loop. Each tab assumes the previous ones passed.

Before you start:

- A clear **3×3 m floor space** and a **charged battery** (voltage affects power — tune at competition conditions)
- [FTC Dashboard](ftc-dashboard.md) open on a laptop on the robot's WiFi (recommended — every value can be typed there)
- **No sync needed** — the tuner rebuilds your registered robot from the live values

## How to read this page

| Tab | Tuner step | What it tunes |
|---|---|---|
| [Motors](#md-tab-motors) | 1 | Wheel directions |
| [Encoders](#md-tab-encoders) | 2 | Wheel diameter + ticks per rev |
| [Track width](#md-tab-track-width) | 3 | Parallel odometry pod spacing |
| [Center offset](#md-tab-center-offset) | 4 | Center pod position |
| [Drive PID](#md-tab-drive-pid) | 5 | Forward/back gains |
| [Strafe PID](#md-tab-strafe-pid) | 5 | Lateral gains |
| [Turn PID](#md-tab-turn-pid) | 5 | Heading gains |
| [Min power](#md-tab-min-power) | 5 | Friction deadband |
| [Auto path](#md-tab-auto-path) | 6 | Path defaults (speed + look-ahead) |
| [Finish](#md-tab-finish) | 7 | Copy the tuned values into your robot |

Every tab follows the same shape: **what it tunes → the test → read the result → pass → tips**.

## Controls (quick reference)

| Button | Action |
|---|---|
| **RB** | Run the current test |
| **D-pad ↑ / ↓** | Increase / decrease the current value |
| **D-pad ← / →** | Pick a term (PID tabs: P → I → D) |
| **Triangle** | PID tab: next test (Drive → Strafe → Turn → Min power) |
| **X** | Back a step |
| **Circle** | Accept this step, move on |
| **Square** | Toggle the builder snippet for your robot |

Everything you can do with the gamepad you can also type into **FTC Dashboard → Crawler Tuner** — the two stay in sync.

---

%%%tabs
%%%tab Motors

### What it tunes

Which direction each drive wheel spins. There is **no config value** here — the fix is wiring or an inversion flag on the motor in `MyRobot.builder()`.

### The test

Place the robot with clearance, then hold one button per wheel:

| Button | Wheel | Power |
|---|---|---|
| **RB** | Front left | 0.5 |
| **LB** | Front right | 0.5 |
| **RT** | Back left | proportional to trigger |
| **LT** | Back right | proportional to trigger |

### Read the result

Every wheel should spin **forward** — the direction the robot drives.

### Pass

All four wheels spin forward.

### Fix a reversed wheel

If a wheel spins backward, add the matching inverter to that motor in `MyRobot.builder()`, rebuild, and re-run:

```java
.invertFrontLeft()    // or invertFrontRight() / invertBackLeft() / invertBackRight()
```

> ⚠️ **Don't continue with a backwards motor.** Every later step reads odometry, and a reversed drive wheel makes all of it garbage. Fix it now.

%%%tab Encoders

### What it tunes

`wheelDiameterIn` and `ticksPerRev` — the two numbers that convert **encoder ticks → distance**:

```
distance = ticks / (ticksPerRev / (wheelDiameterIn × π × 0.0254))   // meters
```

Get these wrong and every distance is off by the same ratio: drive 90 cm when told 100, stop short of every waypoint.

### The test

- **D-pad ↑ / ↓** — wheel diameter (inches, 0.01 steps)
- **D-pad ← / →** — ticks per revolution (50 steps)
- **RB** — spins the drive wheels; the **Left / Right / Center** encoder tick counts climb live on the Driver Station

### Read the result

Drive (or push) the robot a **measured distance** — a tape measure, not your eyes. Compare the odometry distance against reality.

Known values to start from:

| Wheel | Diameter (in) |
|---|---|
| GoBILDA 35 mm pod | 1.37795 |
| REV 25 mm pod | 0.984 |
| 50 mm pod | 1.968 |

| Motor / encoder | Ticks per rev |
|---|---|
| GoBILDA 5203 (odometry pods) | 2000 |
| REV HD Hex (built-in motor encoders) | 560 |
| REV through-bore / shaft encoders | 8192 |

### Pass

Odometry distance matches the tape measure within **~2%**.

### Tips

- Consistently **short** by 10%? Increase `wheelDiameterIn` by 10%. Consistently **long**? Decrease it.
- Type the exact measured diameter into the Dashboard instead of nudging with the D-pad — it's faster and precise.

%%%tab Track width

### What it tunes

`trackWidthIn` — the **center-to-center distance between the two parallel odometry wheels**. This one value controls how wheel rotation turns into heading change. Wrong, and the robot appears to "spin" in its own position estimate and drifts sideways going straight.

### The test

- **D-pad ↑ / ↓** — adjust `trackWidthIn` (inches, 0.1 steps)
- **RB** — the robot spins **10 full rotations** (3600°) in place at 0.3 power; the tuner compares the odometry heading change against the IMU

The robot is drawn live on the Dashboard field view while it spins.

### Read the result

- **"Track width OK"** → the two headings agree within 5°, move on.
- Otherwise the status shows the drift in degrees:
  - Odometry **over-rotates** (odom heading > IMU) → **decrease** track width
  - Odometry **under-rotates** (odom heading < IMU) → **increase** track width
- If the two headings go **opposite directions**, a pod encoder is wired backwards — check it before tuning further.

### Pass

Odom and IMU headings within **5°** after ten turns.

### Tips

`trackWidthIn` is a *physical* measurement — measure pod-center to pod-center with a ruler first, then fine-tune from the spin result. Change by 0.1" and re-run; the drift tells you which way to go.

%%%tab Center offset

### What it tunes

`centerWheelOffsetIn` — how far **forward of the robot's center** the perpendicular (center) odometry wheel sits. Wrong, and the robot appears to rotate while it strafes.

### The test

- **D-pad ↑ / ↓** — adjust `centerWheelOffsetIn` (inches, 0.1 steps)
- **RB** — the robot **strafes 1 meter** at 0.5 power; the tuner measures heading drift the whole way

### Read the result

- Drift **< 2°** → **"Center offset OK"**, move on.
- Heading drifts **counterclockwise** during a rightward strafe → **increase** the offset; **clockwise** → **decrease** it.

### Pass

Heading drift under **2°** across the 1 m strafe.

### Tips

Sign conventions are counterintuitive — change by **0.5" at a time** and watch *which way* the drift moves. If strafing drifts one way and reversing strafe drifts the other, the offset sign is flipped entirely.

%%%tab Drive PID

### What it tunes

`driveKp`, `driveKi`, `driveKd` — the closed-loop gains on **forward distance error**, per **meter**.

### The test

- **Triangle** until the test reads **Drive**
- **D-pad ← / →** — pick the term: **P → I → D**
- **D-pad ↑ / ↓** — adjust (P step 0.01, I step 0.001, D step 0.01)
- **RB** — drives **100 cm** and stops, holding the starting heading

> 💡 **This runs through the real engine.** The tuner calls the exact same `RobotOrientedDrive.drivePID(...)` your autos use — not a simplified copy — so tuned gains behave identically in a match. While it runs, telemetry streams **Error (cm), Power**, and the live **P / I / D** terms, and the robot is drawn on the Dashboard field view.

### Read the result

| Symptom | Fix |
|---|---|
| Never reaches the target (stops short) | **Increase P** |
| Overshoots and oscillates | **Decrease P** |
| Moves smoothly, stops gently | Keep it |
| Stops a couple of cm short every time | Add a little **I** |
| Wobbles around the target | Add **D** |

### Pass

Stops within a few cm of 100 cm without sustained oscillation.

### Tuning I and D

- **I** — only needed for steady-state error (stops a couple of cm short and P alone won't fix it). Start at `0.001`. The loop resets the integral when the error changes sign, so windup is limited — but keep it small anyway.
- **D** — only needed if the robot oscillates around the target. Start at `0.01` and increase until the wobble is damped. Too much D makes motion sluggish and buzzy.

Start from your builder's values (defaults are `0.05` P) and step by `0.01`.

### Builder line

`.drivePid(kp, ki, kd)` — see [Configuration Reference](configuration.md#robot-relative-pid).

%%%tab Strafe PID

### What it tunes

`strafeKp`, `strafeKi`, `strafeKd` — the closed-loop gains on **lateral distance error**, per **meter**.

### The test

- **Triangle** until the test reads **Strafe**
- **D-pad ← / →** — pick the term: **P → I → D**
- **D-pad ↑ / ↓** — adjust
- **RB** — **strafes 100 cm** to the right and stops, holding the starting heading

Same live telemetry as Drive: Error (cm), Power, P/I/D terms, robot drawn on the field view.

### Read the result

| Symptom | Fix |
|---|---|
| Stops short of the strafe | **Increase P** |
| Overshoots / slides past and oscillates | **Decrease P** |
| Drifts forward/back while strafing | Not PID — re-check [Track width](#md-tab-track-width) and [Center offset](#md-tab-center-offset) |
| Stops a couple of cm short consistently | Add a little **I** |
| Wobbles at the end | Add **D** |

### Pass

Stops within a few cm of the 100 cm strafe without sustained oscillation or heading drift.

### Tips

Strafe usually needs a **bit more push** than drive (same P, or slightly higher) because wheels have more friction sideways. If strafing drifts into an arc, the odometry values are wrong — fix track width / center offset before touching these gains.

### Builder line

`.strafePid(kp, ki, kd)` — see [Configuration Reference](configuration.md#robot-relative-pid).

%%%tab Turn PID

### What it tunes

`steerP`, `steerI`, `steerD` — the closed-loop gains on **heading error**, per **degree**. These same gains hold the heading while the robot drives *and* turn in place.

### The test

- **Triangle** until the test reads **Turn**
- **D-pad ← / →** — pick the term: **P → I → D**
- **D-pad ↑ / ↓** — adjust (P step 0.005, I step 0.0005, D step 0.01)
- **RB** — turns **90°** from the current heading and settles

Telemetry streams Error (deg), Power, and P/I/D.

### Read the result

| Symptom | Fix |
|---|---|
| Stops short of 90° | **Increase P** |
| Overshoots, swings past, oscillates | **Decrease P** |
| Never quite settles on the heading | Add a little **I** |
| Wobbles around the heading | Add **D** |

### Pass

Settles within **~2°** of the 90° target without overshoot wobble.

### Tips

- Defaults are smaller than drive/strafe (`0.03` P) because gains are per **degree**, a much smaller error unit than meters.
- Turn gains are also what keeps the robot pointed straight during a path leg — an unstable `steerP` shows up as zig-zagging on straights.

### Builder line

`.steerPid(p, i, d)` — see [Configuration Reference](configuration.md#robot-relative-pid).

%%%tab Min power

### What it tunes

`minPower` — the **smallest power that still overcomes static friction**. Below it, the robot's motors may not move at all; too high, and slow precision moves become impossible.

### The test

- **Triangle** until the test reads **Min power**
- **RB** — runs an **automatic deadband search**: the tuner ramps raw power from `0.05` upward in `0.02` steps until odometry reports the robot moving, then sets `minPower` to a safe value a little above that (clamped to `0.05 – 0.4`)

### Read the result

Telemetry reports:

```
Starts moving at 0.14 power
Recommended minPower 0.17
```

The recommended value is written straight into `minPower`. Nudge it with the D-pad or type a fine-tuned value into the Dashboard — then press **RB** again to confirm.

### Pass

The robot starts moving consistently at the deadband value, and slow precision moves are smooth.

### Tips

- Too **low** → the robot shudders at low speeds.
- Too **high** → it can't do slow, precise final alignments.
- This is a *measurement*, not an engine call — it drives the motors directly, then stores the result in the same `minPower` the engine reads.

### Builder line

`.minPower(x)` — see [Configuration Reference](configuration.md#robot-relative-pid).

%%%tab Auto path

### What it tunes

`moveSpeed` (the `defaultMoveSpeed` used between waypoints), plus `followDistanceCm` and `turnSpeed` from the Dashboard. This test validates **odometry + PID + path following together** — if everything before this passed, the only knob left is how fast and how far ahead to look.

### The test

- **D-pad ↑ / ↓** — adjust `moveSpeed` (0.05 steps, 0.1–1.0)
- **RB** — the robot follows a **1 m square** with pure pursuit: (0,0) → (100,0) → (100,100) → (0,100) → (0,0)

### Read the result

| What you see | Fix |
|---|---|
| Returns near its start | Good — tune speed to taste |
| Overshoots corners | Lower `moveSpeed` or `followDistanceCm` |
| Cuts corners | Raise `followDistanceCm` |
| Sluggish / slow | Raise `moveSpeed` |

When the square finishes, the tuner says **"Path done — copy pathDefaults into your robot"**.

### Pass

Completes the square and ends within **~10 cm** of where it started.

### Tips

- `followDistanceCm` is the pure-pursuit look-ahead radius — a bigger radius smooths and cuts corners; a smaller one corners tighter but can jitter.
- Leave `turnSpeed` and `followDistanceCm` on the Dashboard while you pick `moveSpeed`, then type the final numbers in.

### Builder line

`.pathDefaults(move, turn, follow)` — plus the arrival/orbit/timeout values printed at [Finish](#md-tab-finish).

%%%tab Finish

### What it tunes

Nothing — this is the **output step**. Press **Square** any time, or press **Circle** through to Step 7, to see every tuned value as copy-paste **builder lines** for your robot's `builder()`:

```java
// Paste into your robot's builder(), replacing the tuned values below:
.setTrackWidth(13.0000)
.setCenterWheelOffset(3.5000)
.wheelDiameter(1.3780)
.ticksPerRev(2000)
.drivePid(0.0500, 0.0000, 0.0000)
.strafePid(0.0500, 0.0000, 0.0000)
.steerPid(0.0300, 0.0000, 0.0000)
.minPower(0.1500)
.pathDefaults(0.7000, 0.4000, 25.4000)
.arrivalThresholdCm(5.0000)
.orbitThresholdCm(25.4000)
.timeoutSecs(5.0000)
.turnReferenceRadians(0.5236)
.maxDriveSpeed(1.0000)
```

`.slowSpeeds(...)` and `.slowDownTurn(...)` are only printed if your robot uses slow mode.

### After the tuner

1. Copy the lines into the **tuned section of your robot's `builder()`**
2. Rebuild and deploy
3. Run **Crawler Smoke Test** (2 min) to confirm the robot builds and odometry reports movement
4. Run **Crawler System Test** (~15 min) for a full drive / strafe / square validation

> ⚠️ **This step is mandatory.** Tuning values live only in the tuner's memory while the app runs — there are **no library presets**. Until you paste the printed lines into `builder()` and rebuild, the robot keeps whatever (possibly unset) values it had.

%%%endtabs

## Troubleshooting common tuning issues

| Problem | Likely cause | Fix |
|---|---|---|
| Motor spins backward | Wiring / inversion | [Motors](#md-tab-motors) — add `.invert…()` |
| Robot "spins" in its pose estimate | Track width wrong | [Track width](#md-tab-track-width) |
| Robot rotates while strafing | Center offset wrong | [Center offset](#md-tab-center-offset) |
| Every distance is short/long by a ratio | Wheel diameter / ticks per rev | [Encoders](#md-tab-encoders) |
| Stops short of every target | P too low, or minPower too high | [Drive / Strafe PID](#md-tab-drive-pid) |
| Oscillates around targets | P too high, or needs D | [PID tabs](#md-tab-drive-pid) |
| Zig-zags on straights | Turn PID unstable | [Turn PID](#md-tab-turn-pid) |
| Path overshoots corners | moveSpeed / followDistance too high | [Auto path](#md-tab-auto-path) |
| Path is jerky at corners | followDistance too low | Raise `followDistanceCm` |

---

## Next Steps

- **[Tuning overview →](tuning.md)** Why tuning matters and how the workflow fits together
- **[Configuration Reference →](configuration.md)** What every value does
- **[FTC Dashboard →](ftc-dashboard.md)** Editing values live in the browser
- **[Troubleshooting →](troubleshooting.md)** General problems
