---
title: Troubleshooting
description: Fix common Crawler problems
---

# Troubleshooting

*What to do when something goes wrong*

## Robot doesn't move during autonomous

**Likely causes:** device name mismatch · untuned robot · hardware not in the Driver Hub configuration.

**Fix:**

1. Check every name at the top of `MyRobot.java` against the Driver Hub configuration — spelling, case, and spaces matter
2. Run the **Crawler Tuner** once — an untuned robot can report zero movement
3. Watch the Driver Station: a device-name error appears at INIT
4. Try the **Crawler Smoke Test** — it tells you whether odometry is reporting movement

## Robot moves the wrong way

**Likely causes:** motor inversion · reversed encoder.

**Fix:**

1. Run the tuner's **Motors** step — it tells you exactly which wheel spins wrong
2. Invert that motor in the builder: `.frontLeft("fl").invertFrontLeft()`
3. If odometry runs backward, invert the encoder: `.withThreeDeadWheels(...).setTrackWidth(13.0).invertLeftEncoder()`

## Robot "spins" in its pose estimate (telemetry rotates but robot doesn't)

**Likely causes:** track width way off · IMU orientation wrong.

**Fix:**

1. Re-tune **Step 3 (track width)** — the spin test compares odometry to the IMU
2. Double-check `.imuOrientation(IMU_LOGO, IMU_USB)` matches the physical REV Hub mounting (UP / FORWARD is typical; a sideways hub needs different values)
3. Verify the IMU yaw reads 0 and grows as the robot turns left

## Distances are wrong by the same ratio everywhere

**Likely causes:** wheel diameter or ticks-per-rev.

**Fix:** tune **Step 2**. If the robot drives 90 cm when told 100 cm, increase `wheelDiameterIn` by ~10%. The ratio between odometry and reality should be nearly constant — if it is, it's this, not PID.

## Robot rotates while strafing

**Likely causes:** center wheel offset wrong.

**Fix:** re-tune **Step 4**. If heading drifts counterclockwise during a rightward strafe, increase `centerWheelOffsetIn`.

## Robot overshoots / oscillates around targets

**Likely causes:** P too high (or needs D).

**Fix:** in the tuner's **Step 5** (or the Dashboard):

- Overshoots and oscillates → lower P by 30–50%
- Wobbles around the target with P already low → add `Kd` (start 0.01)
- Stops a couple of cm short → add `Ki` (start 0.001)

## Robot never starts moving on small commands

**Likely causes:** `minPower` too low for your floor friction.

**Fix:** run the tuner's **Min power** test (Step 5 — press **Triangle** to cycle the PID tests to *Min power*) — it searches the deadband automatically.

## Path overshoots corners or is jerky

| Symptom | Fix |
|---|---|
| Cuts corners / misses waypoints | Lower `followDistanceCm` (Dashboard) |
| Jerky, wiggly corners | Raise `followDistanceCm` |
| Overshoots waypoints | Lower `moveSpeed`, or `.slow(robot.config)` on the last waypoint |
| Over-rotates at corners | Raise `orbitThresholdCm` |

## `onReach` never fires (or fires late)

**Likely causes:** arrival threshold too small · waypoint unreachable.

**Fix:**

1. Raise `arrivalThresholdCm` (default 5 cm)
2. Make sure the waypoint is actually reachable (not inside a wall)
3. Check the follower telemetry — "Distance (cm)" shows how close the robot gets before the leg times out

## The OpMode crashes at INIT

**Likely causes:** a device name isn't in the configuration, or the builder stage order is wrong.

**Fix:**

1. Read the error on the Driver Station — it names the missing device
2. Verify the builder order: motor names → `.imu(...)` → `.motors()` → localizer → tuning values
3. A `IllegalStateException` from `motors()` or `build()` means a required stage was skipped

## The tuner's snippet doesn't match my robot

**Likely causes:** the tuned values were pasted into the wrong place, or `MyRobot` was changed mid-season.

**Fix:** paste the snippet into the tuned section of your robot's `builder()` and rebuild. The tuner rebuilds your registered robot with live values — your device names and localizer — so the hardware always matches; only the numbers can drift.

## Changes in the Dashboard don't take effect

**Likely causes:** the OpMode isn't running, or the field isn't saved.

**Fix:**

1. The **Crawler Tuner** must be running (Play pressed)
2. Press Enter in the Dashboard field to commit the value
3. The robot rebuilds itself when a value changes — give it one loop

## Still stuck?

1. Re-read [Setup](setup.md) and the [Tuning Guide](tuning-guide.md)
2. Run the **Crawler Smoke Test** then the **Crawler System Test** — they isolate odometry, PID, and path issues
3. Ask your coach or the FTC community Discord

---

## Next Steps

**[Full Example →](example.md)** A complete, working project
