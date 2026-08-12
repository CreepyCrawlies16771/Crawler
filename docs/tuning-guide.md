---
title: Step-by-Step Tuning Guide
description: What to do physically for each of the 7 tuner steps, and how to know when it passes
---

# Step-by-Step Tuning Guide

*A walkthrough of every `CrawlerTuner` step — what to do, what to watch, and when to move on.*

Before you start, make sure:

- **No sync needed** — the tuner rebuilds your registered robot with the live values
- You have a **clear 3×3 m floor space**
- The battery is charged
- [FTC Dashboard](ftc-dashboard.md) is open on a laptop on the robot's WiFi (highly recommended)

## Control scheme (quick reference)

| Button | Action |
|---|---|
| **RB** | Run the current test |
| **D-pad ↑ / ↓** | Increase / decrease the current value |
| **D-pad ← / →** | PID step: choose the term (P → I → D) |
| **Triangle** | PID step: next test (Drive → Strafe → Turn → Min power) |
| **X** | Back a step |
| **Circle** | Accept step, move on |
| **Square** | Toggle the builder snippet for your robot |

Values can also be typed directly into the **FTC Dashboard → Crawler Tuner** panel — both inputs stay in sync.

---

## Step 1 · Motors

**Purpose:** confirm every wheel spins the correct direction before trusting odometry.

**What to do:**

- Place the robot on the floor with clearance
- **RB** → front-left spins · **LB** → front-right · **RT** → back-left · **LT** → back-right

**Pass:** each wheel spins **forward** (the direction the robot drives). If any wheel is reversed, add `.invertFrontLeft()` / `.invertFrontRight()` / `.invertBackLeft()` / `.invertBackRight()` to that motor in `MyRobot.java`, rebuild, and re-run.

> ⚠️ **Don't continue with a backwards motor.** Every later step will be garbage. Fix it now.

---

## Step 2 · Encoders (wheel diameter & ticks/rev)

**Purpose:** make odometry distances match reality. Odometry converts **ticks → distance** using:

```
distance = ticks / (ticksPerRev / (wheelDiameterIn × π × 0.0254))   // meters
```

**What to do:**

- **D-pad ↑ / ↓** — wheel diameter (inches, 0.01 steps)
- **D-pad ← / →** — ticks per revolution (50 steps)
- **RB** — spin the drive wheels and watch the three encoder tick counts climb

**Known values:**

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

**Pass:** after a known-distance drive (tape measure!), the odometry distance matches within ~2%. If it's consistently short by 10%, increase `wheelDiameterIn` by 10%.

---

## Step 3 · Track width

**Purpose:** the distance between the two parallel odometry wheels controls how odometry converts wheel rotation into heading change. Wrong → the robot "spins" in its own position estimate and drifts sideways.

**What to do:**

- Clear a circle of space
- **D-pad ↑ / ↓** — adjust `trackWidthIn` (inches)
- **RB** — the robot spins **10 full rotations** in place; the tuner compares the odometry heading change against the IMU

**How to interpret the result:**

- Message says **"Track width OK"** → within 5°, move on
- Otherwise the status shows the drift in degrees:
  - Odomometry **over-rotates** (odom heading > IMU) → **decrease** track width
  - Odomometry **under-rotates** (odom heading < IMU) → **increase** track width

**Tip:** `trackWidthIn` is a *physical* measurement (center-to-center of the two parallel pods) — measure it with a ruler first, then fine-tune.

---

## Step 4 · Center offset

**Purpose:** how far forward of the robot's center the **perpendicular** (center) odometry wheel sits. Wrong → the robot appears to rotate while it strafes.

**What to do:**

- **D-pad ↑ / ↓** — adjust `centerWheelOffsetIn`
- **RB** — the robot strafes **1 meter**; the tuner watches heading drift

**How to interpret:**

- Drift **< 2°** → "Center offset OK"
- Heading drifts **counterclockwise** during a rightward strafe → **increase** offset; **clockwise** → **decrease**

**Tip:** sign conventions can be counterintuitive. Change it by 0.5" at a time and watch which direction the drift moves.

---

## Step 5 · PID (drive / strafe / turn / min power)

**Purpose:** tune the closed-loop gains so the robot stops exactly on target without oscillating.

> 💡 **These tests run through the real engine** — the tuner calls the same `RobotOrientedDrive.drivePID` / `strafePID` / `turnPID` methods your autos use, so the gains you settle on behave identically in a match. While a test runs, the robot is drawn live on the [FTC Dashboard](ftc-dashboard.md) field view and the telemetry panel streams error, power, and the live P/I/D terms.

**Controls inside this step:**

- **Triangle** cycles: `Drive → Strafe → Turn → Min power`
- **D-pad ← / →** picks the term: **P → I → D**
- **D-pad ↑ / ↓** adjusts the selected term
- **RB** runs the test

**Drive & Strafe tests:** the robot drives / strafes 100 cm and stops. Gains are per **meter** of error.

**Turn test:** the robot turns 90° in place. Gains are per **degree** of error.

### Tuning P (proportional)

| Symptom | Fix |
|---|---|
| Never reaches the target (stops short) | **Increase** P |
| Overshoots and oscillates | **Decrease** P |
| Moves smoothly, stops gently | Keep it |

Start with the defaults (`0.05` drive/strafe, `0.03` steer) and step by `0.01`.

### Tuning I (integral)

Only needed if the robot consistently stops a couple of cm short and P alone won't fix it. Start at `0.001`, watch for windup (slow drift). The control loops reset the integral when the error changes sign, so aggressive I is safer than usual — but keep it small.

### Tuning D (derivative)

Only needed if the robot oscillates around the target. Start at `0.01` and increase until the wobble is damped. Too much D makes the motion sluggish and buzzy.

### Min power (the deadband)

The smallest power that still overcomes static friction. If it's too low, the robot shudders at low speeds; too high, it can't do slow precision moves.

**RB runs the automatic deadband search:** the tuner ramps power up until the odometry reports movement, then sets `minPower` to a safe value above that. Watch the result in telemetry — you can type a fine-tuned value straight into the Dashboard.

### Pass criteria

- Drive / strafe: stops within a few cm of 100 cm without sustained oscillation
- Turn: settles within ~2° of 90°
- Min power: robot starts moving consistently at the deadband value

---

## Step 6 · Auto path (square test)

**Purpose:** validate odometry + PID + path following together, and pick a good default move speed.

**What to do:**

- **D-pad ↑ / ↓** — adjust `moveSpeed`
- **RB** — the robot follows a 1 m square (0,0 → 100,0 → 100,100 → 0,100 → 0,0) with pure pursuit

**What to watch:**

- Returns near its start → good
- Overshoots corners → lower `moveSpeed` or `followDistanceCm` (Dashboard)
- Cuts corners → raise `followDistanceCm`
- Sluggish → raise `moveSpeed`

**Pass:** the robot completes the square and ends within ~10 cm of where it started.

---

## Step 7 · Finish

The tuner shows the complete tuned values as **builder lines** for your robot's `builder()` (`MyRobot` is just the example name — any class extending `CrawlerRobot` works). Press **Square** any time to see them:

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

(`.slowSpeeds(...)` / `.slowDownTurn(...)` are only printed when your robot uses them.)

1. Copy the lines into the tuned section of your robot's `builder()`
2. Rebuild and deploy
3. Run **Crawler Smoke Test** to confirm the robot builds and odometry moves
4. Run **Crawler System Test** for a full validation (drive, strafe, square)

> ⚠️ **This step is mandatory.** Tuning values live only in the tuner's memory while the app runs. There are no library defaults — until you paste them into your robot's `builder()` and rebuild, the robot keeps whatever (possibly unset) values it had.

## Troubleshooting common tuning issues

| Problem | Likely cause | Fix |
|---|---|---|
| Motor spins backward (Step 1) | Wiring / inversion | Add `.invert…()` in the builder |
| Robot "spins" in its pose estimate | Track width wrong | Step 3 |
| Robot rotates while strafing | Center offset wrong | Step 4 |
| Stops short of every target | P too low or minPower too high | Step 5 |
| Oscillates around targets | P too high (or needs D) | Lower P, add D |
| Odom distance ≠ real distance | Wheel diameter / ticks per rev | Step 2 |
| Path overshoots corners | moveSpeed / followDistance too high | Step 6 / Dashboard |
| Path is jerky at corners | followDistance too low | Raise `followDistanceCm` |

---

## Next Steps

- **[Configuration Reference →](configuration.md)** What every value does
- **[FTC Dashboard →](ftc-dashboard.md)** Editing values live in the browser
- **[Troubleshooting →](troubleshooting.md)** General problems
