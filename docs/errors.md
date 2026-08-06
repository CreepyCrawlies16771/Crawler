---
title: Error Messages
description: The CRWL error spec and full error catalog
---

# Error Messages

*Every error Crawler can throw, what it means, and how to fix it — in plain English*

Crawler detects problems **as early as possible** — at INIT, when a robot is built,
and the instant a path starts — and reports them as `CRWL-XXX` codes on the Driver
Station in a fixed format:

```
CRWL-XXX: <what happened, in plain English>
→ <why it likely happened + the exact fix, with a code snippet if the fix is code>
→ Error found in <filename> at <Linenumber>! (IN RED)
```

> 🚨 The last line is **shown in red**. If your code throws one of these, read the
> first line, do what the second line says, and fix the file the third line names.

## Where errors get caught

| Code range | Category | Caught when |
|---|---|---|
| `1xx` | Setup | Building the robot (INIT) and when a path starts |
| `2xx` | Odometry | Preflight before a path, and while driving / tuning |
| `3xx` | Path definition | The instant `follow()` is called, before motors spin |
| `4xx` | Runtime | While a path is running |

Most codes are **thrown** — they stop the OpMode so nothing drives wrong.
A few are **warnings** — they show in telemetry but let the code keep running
(so tuning and testing never get interrupted).

## When is the check run?

1. **`CrawlerRobot.Builder.build()`** validates motor names, IMU name, localizer
   config, and your tuned values.
2. **`RobotOrientedDrive`** (and anything using it) checks config + IMU at INIT.
3. **`FOFollower.follow()`** runs the full preflight: config, start pose, localizer
   health, IMU, and every waypoint — before the first motor spins.

## How to set a start pose (CRWL-101)

Every path needs a starting position. Call it once in `init()` — or right after
`waitForStart()` — before `follow()`:

```java
robot.startPose(0, 0, 0);     // x cm, y cm, heading radians
// or, to start at the origin facing 0:
robot.resetPose();
```

## How to use the codes

- **The code is searchable.** Paste `CRWL-101` into Discord, Chief Delphi, or this
  page and you'll find other teams with the same problem.
- **The fix is an action.** Every entry tells you what to *do* — not just what's wrong.
- **The file and line are where Crawler noticed it.** That is usually the builder call
  or the `follow()` call that triggered the check.

## The full catalog

### 1xx Setup

#### CRWL-101 — Start pose never set

**Message:** Can't start following a path — the robot's starting position was never set.

**Fix:** Call `robot.startPose(x, y, heading)` — or `robot.resetPose()` — once in
`init()` before running any path:

```java
robot.resetPose();   // start at (0, 0), heading 0
```

#### CRWL-102 — Drive motor name missing

**Message:** Can't build the robot — a drive motor name is missing.

**Fix:** Set all four names before `.motors()` / `.build()`:

```java
.frontLeft("fl").frontRight("fr").backLeft("bl").backRight("br")
```

#### CRWL-103 — IMU name missing

**Message:** Can't build the robot — no IMU name was set.

**Fix:** Call `.imu("imu")` in your builder before `.build()`.

#### CRWL-104 — Hardware device not found

**Message:** Can't find hardware device `"<name>"` in the configuration.

**Fix:** Open the Driver Hub's **Configure Robot** screen and fix the name — check
spelling and case. Keep the constants at the top of `MyRobot.java` in sync with the configuration.

#### CRWL-105 — Localizer config missing

**Message:** Can't build the `<type>` localizer — required tuning values are missing or invalid.

**Fix:** Set the values in your builder before `.build()`:

```java
.setTrackWidth(13.0)          // dead-wheel robots
.setCenterWheelOffset(0.0)    // three-dead-wheel robots only
.wheelDiameter(2.0)
.ticksPerRev(288)
```

#### CRWL-106 — Pinpoint config missing

**Message:** Can't build the Pinpoint localizer — its device name or config is missing.

**Fix:** Call `.withPinpoint("odo")` then `.setConfig(...)` before `.build()`:

```java
.withPinpoint("odo")
.setConfig(0, 0, DistanceUnit.CM, GoBildaOdometryPods.GO_BILDA_4_BAR, ...)
```

#### CRWL-107 — Invalid config value

**Message:** Your robot config has an invalid value: `<name>=<value>`.

**Fix:** Open `MyRobot.builder()` (or the Crawler Tuner Dashboard panel) and set
every speed between 0 and 1 and every distance / timeout greater than 0, then rebuild.

### 2xx Odometry

#### CRWL-201 — IMU not responding

**Message:** The IMU is not responding — heading and path following will not work.

**Fix:** Check the `.imu("...")` device name and wiring, then hold the robot still
for 2 seconds after power-on before running autonomous.

#### CRWL-202 — Encoders not moving

**Message:** Odometry reports no movement — the robot was told to drive but the pose is frozen.

**Fix:** Check the odometry pod wiring and encoder names. Run the Crawler Tuner
**Step 2** and confirm tick counts change when you push the robot. Make sure the
robot isn't blocked and the pods aren't slipping.

#### CRWL-203 — Reversed odometry direction *(warning)*

**Message:** Reversed odometry direction — the pose estimate moves opposite to the real robot.

**Fix:** Invert the offending encoder in your builder:

```java
.withThreeDeadWheels("enc_l", "enc_r", "enc_c")
.setTrackWidth(13.0)
.invertLeftEncoder()
```

See the tuner's Motors / Encoders steps to find which one is wrong.

#### CRWL-204 — Odometry drift *(warning)*

**Message:** Odometry drifts during the spin test — track width or center-pod offset is off.

**Fix:** Adjust `trackWidthIn` (tuner Step 3) or `centerWheelOffsetIn` (Step 4)
until the drift is under 5 degrees.

#### CRWL-205 — Non-finite pose

**Message:** The localizer produced a non-finite pose (NaN or infinity) — odometry data is bad.

**Fix:** Check the encoder wiring and the `wheelDiameterIn` / `ticksPerRev` values,
then call `robot.resetPose()` and retry.

### 3xx Path definition

#### CRWL-301 — Empty path

**Message:** `follow()` was called with a null or empty path.

**Fix:** Pass at least two waypoints:

```java
follower.follow(
    Waypoint.at(0, 0, robot.config).build(),
    Waypoint.at(60, 0, robot.config).build()
);
```

#### CRWL-302 — Path too short

**Message:** `follow()` needs at least 2 waypoints, got `<n>`.

**Fix:** Add a second waypoint — a path needs a start and an end.

#### CRWL-303 — Non-finite waypoint

**Message:** Waypoint `(x, y)` is not a valid position — the coordinates are NaN or infinite.

**Fix:** Use real field coordinates in `Waypoint.at(x, y, robot.config)`, never NaN or
Infinity.

#### CRWL-304 — Speed out of range

**Message:** Waypoint speed `<value>` is out of range — motor power must be between 0 and 1.

**Fix:** Use `.speed(0.0 ... 1.0)` or remove it and let `robot.config.defaultMoveSpeed` apply.

#### CRWL-305 — Duplicate waypoints

**Message:** Consecutive waypoints `(x1, y1)` and `(x2, y2)` are the same point — the path
has a zero-length segment.

**Fix:** Remove the duplicate waypoint or give the second one a different coordinate.

#### CRWL-306 — Null waypoint

**Message:** `follow()` was given a null waypoint at index `<n>`.

**Fix:** Build every waypoint with `Waypoint.at(x, y, robot.config).build()` and pass
the built objects.

### 4xx Runtime

#### CRWL-401 — Overlapping follow

**Message:** `follow()` was called while another path was still being followed.

**Fix:** Call `follower.follow(...)` once per run — don't nest or overlap `follow()` calls.

#### CRWL-402 — Leg timeout *(warning)*

**Message:** A waypoint was not reached before the `<s>`s timeout — the leg was aborted.

**Fix:** Raise `robot.config.timeoutSecs`, lower the move speed near the target, or
check the waypoint is reachable (not inside a wall).

#### CRWL-403 — Non-finite drive power

**Message:** `drive()` received a non-finite power value `<value>` — the motors would get garbage.

**Fix:** Guard your input:

```java
double forward = Double.isNaN(raw) ? 0 : raw;
robot.drive(forward, 0, 0);
```

---

## Next Steps

**[Troubleshooting →](troubleshooting.md)** The fastest path from symptom to fix
