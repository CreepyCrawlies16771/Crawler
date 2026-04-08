---
title: Troubleshooting
description: Fix common Crawler problems
---

# Troubleshooting

*What to do when something goes wrong*

## Robot Doesn't Move During Autonomous

**Symptom:** You select your autonomous, press Play, and nothing happens.

**Likely causes:**

- Motors aren't named correctly in MyRobot.java
- Hardware hasn't been initialized
- Build errors (check the build log)

**Fix:**

1. Check that motor names in MyRobot match exactly what you named them in the FTC Driver Hub configuration
2. Make sure `buildRobot()` is calling `robot = new MyRobot();`
3. Check the logcat for errors (Logcat is in Android Studio: View → Tool Windows → Logcat)
4. Try running a simple TeleOp first — if the driver can move the robot, the hardware is fine

---

## Robot Moves in the Wrong Direction

**Symptom:** You command "drive forward" but the robot reverses.

**Likely causes:**

- Motors are inverted incorrectly
- You're reading the field coordinates backwards

**Fix:**

1. In MyRobot.java, check which motors you're inverting
2. Test by removing all `invert*()` calls and see what happens
3. Add inverting back one motor at a time until motion is correct
4. Or: run the hardware verification step in the tuner (Step 1 walks through each motor)

---

## Robot Spins in Circles

**Symptom:** Robot rotates uncontrollably during tuning or autonomous.

**Likely causes (in order of probability):**

1. Track width is way too small
2. One motor is inverted when it shouldn't be
3. Odometry pods aren't positioned correctly

**Fix:**

1. If this happens during tuning: when tuner asks you to drive straight, ignore the spinning. Let it complete Step 3 (Track Width) — the tuner will fix it
2. If it happens in autonomous: re-run tuning, especially Step 3
3. Double-check motor inverting (see "Robot Moves in the Wrong Direction" above)

---

## Robot Overshoots Waypoints

**Symptom:** Robot goes past the waypoint before turning toward the next one.

**Likely causes:**

- Lookahead distance too large
- Your waypoints are too close together
- Robot velocity is too high relative to how tight your turns are

**Fix:**

1. Decrease `LOOKAHEAD_DISTANCE` in RobotConfig (try 5.0 instead of 6.0)
2. Space waypoints further apart (minimum 12 inches)
3. Use `.slow()` on waypoints where precision matters
4. In Configuration Reference page, read the LOOKAHEAD_DISTANCE section for more details

---

## Robot Orbits a Waypoint and Never Arrives

**Symptom:** Robot circles around a waypoint without ever reaching it.

**Likely causes:**

- Lookahead distance too small
- Lateral coefficient too high (robot oscillates)
- Waypoint is unreachable (inside an obstacle or off the field)

**Fix:**

1. Increase `LOOKAHEAD_DISTANCE` (try 7.0 or 8.0)
2. Decrease `LATERAL_DISTANCE_COEFFICIENT` (try 0.8 or 0.9)
3. Check that the waypoint coordinates are actually reachable on your field
4. Make sure your robot's starting pose is set correctly: `follower.setStartingPose(...)`

---

## Odometry Reads Wrong Values

**Symptom:** The robot's position estimate (from getDoPose()) is wrong.

**Likely causes:**

- Odometry pods aren't mounted correctly
- Localizer type doesn't match your actual hardware
- Track width is way off (causes rotational errors to accumulate)

**Fix:**

1. Check that your MyRobot code specifies the correct localizer type:
   - Do you have three dead wheels? Use `THREE_DEAD_WHEELS`
   - Two? Use `TWO_DEAD_WHEELS`
   - Just encoders? Use `MOTOR_ENCODERS`
2. Re-run tuning (especially Step 6 and 7, which directly measure odometry)
3. Check that pods are mechanically sound and not slipping
4. Verify track width — if it's off by a lot, rotational errors compound

---

## Build Error: "Cannot Find Symbol CrawlerAuto"

**Symptom:** Android Studio shows a red error on `extends CrawlerAuto<MyRobot>`

**Likely causes:**

- Crawler dependency didn't install correctly
- Gradle sync didn't complete
- You're using the wrong import

**Fix:**

1. Click **File → Sync Now** and wait for it to complete
2. If that doesn't work, click **File → Invalidate Caches / Restart** and wait
3. Check that your build.gradle has the Crawler dependency (see Installation page)
4. Delete the `build/` folder and rebuild

---

## Build Error: "Duplicate Class"

**Symptom:** Gradle build fails with "duplicate class" message.

**Likely causes:**

- You have Crawler imported twice (once as a dependency, once manually)
- A library dependency conflict

**Fix:**

1. Do NOT manually add Crawler to your TeamCode folder. It should only come from the dependency
2. Search your entire project for duplicate Crawler files and delete them
3. Run Gradle clean: **Build → Clean Project**
4. Rebuild

---

## Robot Jerks or Oscillates During Pure Pursuit

**Symptom:** Robot's movement is jerky or wiggles side to side instead of smooth.

**Likely causes:**

- Motion profile time is too short
- Lateral or heading coefficients are too high
- Lookahead distance is too small

**Fix:**

1. Increase `MOTION_PROFILE_TIME` from 0.5s to 1.0s (gives robot more time to accelerate smoothly)
2. Decrease `LATERAL_DISTANCE_COEFFICIENT` to 0.8 or lower
3. Increase `LOOKAHEAD_DISTANCE` to 7.0 or higher
4. Re-tune if you've made big changes to robot setup

---

## Path Following Is Slow and Sluggish

**Symptom:** Your robot is slow following paths, taking forever to reach waypoints.

**Likely causes:**

- Motion profile time is too long
- Max velocity is set too low
- You're using `.slow()` on every waypoint
- Lookahead distance is too large (robot takes very smooth, wide turns)

**Fix:**

1. Check `MAX_VELOCITY` — is it set to something reasonable? (Should be 40-60 in/s for most robots)
2. Decrease `MOTION_PROFILE_TIME` from 0.5s to 0.3s (allow faster acceleration)
3. Only use `.slow()` on waypoints that need precision
4. Slightly decrease `LOOKAHEAD_DISTANCE` to tighten turns (but watch for overshooting)

---

## onReach Action Fires at the Wrong Waypoint

**Symptom:** Your claw opens when the robot reaches waypoint 2, but it's supposed to open at waypoint 3.

**Likely causes:**

- Copy-paste error in your path definition
- You have the waypoint data wrong (different coordinates than you think)

**Fix:**

1. Check your code — verify each waypoint has the action you intended
2. Add telemetry to see which waypoint you're at:
   ```java
   telemetry.addData("Current Waypoint", follower.getCurrentWaypoint());
   telemetry.update();
   ```
3. Re-count your waypoints starting from zero (first waypoint is #0, not #1)

---

## Robot Doesn't Stop at the End of the Path

**Symptom:** Robot coast past the final waypoint.

**Likely causes:**

- Final waypoint doesn't have `.slow()`
- Motion profile time is too long (robot takes a while to decelerate)
- Final waypoint has a high speed setting

**Fix:**

1. Add `.slow()` to your final waypoint:
   ```java
   Waypoint.at(48, 48)
       .heading(0)
       .slow()           // Add this
       .buildAll()
   ```
2. Decrease the speed on the final waypoint: `.speed(0.3)` instead of 0.8
3. After the path completes, add a small reverse to "back up into" the target:
   ```java
   follower.follow(/* ... path ... */);
   ro.drivePID(-2, heading);  // Back up slightly
   ```

---

## Still Stuck?

If you're still having problems:

1. Check the Crawler documentation at [crawler.github.io](https://github.com/Fission310/Crawler)
2. Ask on the FTC Community Discord (link on FTC website)
3. Ask your coach or programming mentor

Remember: every team runs into these. It's part of learning. You'll get it.

---

## Next Steps

**[Full Example →](example.md)** See a complete real-world autonomous with comments
