---
title: Using FTC Dashboard with Crawler
description: See your robot on a field view and change values in real time without rebuilding
---

*How to use FTC Dashboard to visualize your robot and tune values without touching Android Studio.*

FTC Dashboard is a free tool that runs in your web browser and connects to your robot over WiFi. It shows your robot's position on a live field view and lets you change tuning values instantly without rebuilding the app.

Think of it as a window into your robot's brain — you can see exactly what the robot thinks it is doing on the field, and you can experiment with different values in the browser instead of reopening Android Studio and rebuilding each time.

## Installing and connecting

FTC Dashboard is already included as a Crawler dependency — no extra installation needed. You just need to connect to it.

**Steps:**

1. Connect your laptop to the **same WiFi network** as your robot (same network where the Driver Station connects)
2. Open any web browser (Chrome, Firefox, Edge, etc.)
3. Type this address in the browser: `http://192.168.43.1:8080/dash`
4. Press Enter
5. You should see the FTC Dashboard interface with a field view on the left and control panels on the right

**If you get "cannot connect":**
- Double-check that your laptop is on the robot's WiFi network
- Ask your programming lead or coach for the correct IP address — Control Hubs sometimes use non-standard addresses
- Make sure the OpMode is running on the Driver Station

> 📝 **Note:** The IP address `192.168.43.1` is the default for FTC Control Hubs. Some Expansion Hubs or older setups might use a different address. If this does not work, check your robot's network settings.

## What you see during tuning

The FTC Dashboard interface has three main panels:

### Field view (center)

The field view shows a top-down map of the FTC field with your robot's current position and movement.

- **Blue dot** = your robot's current position according to odometry (what Crawler thinks the robot location is)
- **Blue arrow** = your robot's heading/rotation
- **Blue line** = the path the robot is following (during pure pursuit steps)
- **Red dot** = the "lookahead point" — where the robot is trying to go next during pure pursuit (Step 9 and 10)

Watch how the blue dot moves as your robot drives. If it matches the actual robot movement, your odometry is good. If it drifts or lags behind, something needs tuning.

### Telemetry panel (right side, top)

This shows all the same real-time values you see on the Driver Hub:

- Current tuning step and name
- Current value being tuned
- Measurement results (e.g., heading error, distance error)
- Status (PASS / MARGINAL / FAIL)

This updates live as the robot moves and as you change values.

### Config panel (left side)

This shows all `@Config` values from your `RobotConfig.java` file, organized by class:

```
RobotConfig
  └─ Odometry
      ├─ TRACK_WIDTH: 13.0
      ├─ CENTER_WHEEL_OFFSET: 3.5
      ├─ WHEEL_DIAMETER: 1.37795
      └─ TICKS_PER_REV: 2000
  └─ RobotOriented
      ├─ Kp: 0.05
      ├─ strafe_Kp: 0.05
      ├─ STEER_P: 0.03
      └─ ...
  └─ FieldOriented
      ├─ DEFAULT_FOLLOW_DISTANCE: 10.0
      ├─ DEFAULT_MOVE_SPEED: 0.7
      └─ ...
```

Click on any value to edit it.

## Changing values live

This is the most powerful feature of FTC Dashboard during tuning. Instead of pressing D-pad buttons on the gamepad, you can type values directly in the browser and they take effect immediately on the robot.

**How to change a value:**

1. In the Config panel on the left, expand the category you want (e.g., `RobotOriented`)
2. Click on the value you want to change (e.g., `Kp: 0.048`)
3. A text box appears — type your new number
4. Press Enter
5. The new value is sent to the robot instantly — no rebuild, no redeploy
6. In the telemetry on the right, you see the value update in real time

**Example:** During Step 5 (Drive PID), instead of:
- Pressing Right Bumper, measuring the robot, pressing D-pad up or down, repeat...

You can now:
- Type `0.045` in the `Kp` field, press Enter
- Press Right Bumper on the gamepad
- Watch the robot drive to 24 inches
- See immediately if it is better or worse than 0.048
- Type the next value, repeat

This is **much** faster than tuning with just the gamepad.

> 💡 **Tip:** Open FTC Dashboard on your laptop and the Driver Station on a tablet side-by-side. You can control the OpMode with the gamepad while watching the field view and changing values in the browser — multitasking at its finest.

## Watching pure pursuit in real time

During Steps 9 and 10 of the tuner (follow distance and field-oriented integration test), the field view is especially useful.

**What to watch:**

- The blue dot should follow close behind the red dot
- If the blue dot is far behind the red dot, your follow distance might be too large (the robot is not reacting fast enough)
- At corners, watch the red dot "jump" to the next waypoint — the blue dot should follow smoothly without overshooting
- If the robot cuts the corner, the blue dot goes off the path — follow distance is too large
- If the robot is jerky or oscillates at corners, follow distance is too small

**During Step 9 (Follow Distance):**
1. Note the current follow distance in the Config panel (e.g., 10.0)
2. Start the test (Right Bumper)
3. Watch how the robot handles the L-shaped path
4. If it cuts the corner: change follow distance to 8.0 in the browser and try again
5. If it is sluggish: change to 12.0 and try again
6. Repeat until it looks smooth

This lets you try 5 values in the time it would normally take to try 2 with gamepad controls.

## Re-tuning without the tuner

You can also use FTC Dashboard with regular autonomous OpModes to see where your tuned values are causing issues.

For example, if your robot overshoots corners during an autonomous match:
1. Run an autonomous OpMode that follows waypoints
2. Watch the field view — does the blue dot match the actual robot path?
3. If the robot overshoots, adjust follow distance in the browser and run the OpMode again
4. When you find the right value, copy it into RobotConfig and rebuild

## Troubleshooting Dashboard

| Problem | Likely cause | Fix |
|---------|------------|-----|
| Browser shows "cannot connect" | Laptop is not on robot WiFi | Connect to robot's WiFi network |
| IP address wrong | Using wrong or outdated IP | Ask coach for correct IP, or check Control Hub settings |
| Field view is blank | OpMode is not running | Start the OpMode on Driver Station |
| Field view shows no robot | Odometry not initialized or broken | Check that encoder pods are plugged in |
| Config panel is empty | `@Config` annotation missing from `RobotConfig` | Make sure inner classes have `@Config` annotation |
| Values reset after stopping | This is normal behavior | Copy your final values to `RobotConfig.java` and rebuild |
| Changes in browser do not take effect | OpMode is not running, or values not committed | Make sure OpMode is running and you pressed Enter in the text field |

## After you finish tuning

Remember: **Changes made in FTC Dashboard are temporary.** When you stop the OpMode or close the app, the values reset to what is in `RobotConfig.java`.

Always copy your final tuned values back into `RobotConfig.java` and rebuild:

1. Write down or screenshot your best values from FTC Dashboard
2. Open `RobotConfig.java` in Android Studio
3. Update the `@Config` class fields with your tuned numbers
4. Save and rebuild the app
5. Deploy to the robot

Now your tuned values are permanent, and every OpMode will use them automatically.

> 💡 **Tip:** Take a screenshot of the Config panel before you close Dashboard. You can always refer to it later while editing `RobotConfig.java`.

## Next steps

You are now ready to tune your robot. Start with [Step-by-step tuning guide](tuning-guide.md).
