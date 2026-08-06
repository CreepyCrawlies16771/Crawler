---
title: FTC Dashboard
description: Edit Crawler Tuner values live in the browser and watch the field view
---

# FTC Dashboard

*Type tuning values into a web browser and watch the robot react — no recompiling.*

FTC Dashboard is a free tool that runs in your browser and connects to the robot over its WiFi network. With Crawler it gives you two superpowers:

1. **A live config panel** — every `Crawler Tuner` value is editable in the browser, applied instantly
2. **A field view** — the robot, path, and look-ahead point drawn in real time

## Connecting

1. Put your laptop on the **same WiFi network as the robot controller**
2. Open a browser and go to:

```
http://192.168.43.1:8080/dash
```

3. Run any Crawler OpMode (the **Crawler Tuner** is the most useful)

> **Can't connect?** Check the laptop is on the robot's network, the robot controller is powered, and an OpMode is running. `192.168.43.1` is the default Control Hub address.

## The Crawler Tuner config panel

When the **Crawler Tuner** is running, the Dashboard's left panel shows a **`Crawler Tuner`** group. Every field is a live value — click it, type a new number, press **Enter**, and the tuner picks it up on the next loop and rebuilds the robot.

```
Crawler Tuner
├── trackWidthIn           13.0
├── centerWheelOffsetIn     3.5
├── wheelDiameterIn       1.37795
├── ticksPerRev            2000
├── driveKp / driveKi / driveKd
├── strafeKp / strafeKi / strafeKd
├── steerP / steerI / steerD
├── minPower               0.15
├── moveSpeed / turnSpeed / followDistanceCm
├── arrivalThresholdCm / orbitThresholdCm
├── timeoutSecs / maxDriveSpeed
```

**How it works:** the tuner's `TuningConfig` class is annotated with `@Config("Crawler Tuner")`, and all its fields are `public static`. The Dashboard edits those fields directly; the tuner re-reads them every loop. Whatever you type in the browser is exactly what the gamepad D-pad would adjust — they stay in sync.

**Example — tuning Drive Kp without touching the gamepad:**

1. Run the tuner, go to Step 5 (PID), press **Triangle** until the test reads `Drive`
2. In the Dashboard, click `driveKp`, type `0.06`, press **Enter**
3. Press **RB** on the gamepad to run the 100 cm drive test
4. Watch the telemetry — overshoot? Type `0.04`. Repeat

You can try five values in the time the gamepad alone would take for two.

## Telemetry & the field view

During tuning and autonomous, the right panel streams the same telemetry you see on the Driver Station:

- Current step and value (tuner)
- Error / power / live **P·I·D terms** (PID tests — streamed from the real `RobotOrientedDrive` engine)
- Target, distance, elapsed (path following)
- Robot pose X / Y / heading

While a tuner test is running (spin, strafe, PID drive/strafe/turn, or the min-power search), the **robot is drawn live on the field view** so you can watch the odometry move in the browser at the same time as the real robot.

Note on the field view during autonomous paths: `RobotMovement.follow(List, double)` builds the path-polyline and follow-point drawing, but that packet isn't transmitted to the Dashboard yet, so **paths don't currently appear on the field view**. The tuner draws your **real robot** in **green** while its spin, strafe, PID, and min-power tests run.

If you want the robot drawn in your own OpMode:

```java
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import org.firstinspires.ftc.teamcode.Crawler.Dashboard.DashboardFieldViewUtils;

TelemetryPacket packet = new TelemetryPacket();
DashboardFieldViewUtils.drawRobot(packet,
        robot.getPose().getX(), robot.getPose().getY(),
        robot.getPose().getHeading(), DashboardFieldViewUtils.FieldColor.BLUE);
FtcDashboard.getInstance().sendTelemetryPacket(packet);
```

## Tuning without the tuner

Add the `drawRobot(...)` snippet above to your own OpMode's loop, run any autonomous, and watch the field view:

- Blue dot/square matches the real robot → odometry is good
- Square drifts from the real robot during turns → track width (Step 3)
- Square drifts sideways → center offset (Step 4)
- Path cuts corners → lower `followDistanceCm` in the tuner, re-test

Then copy the final values into `MyRobot.java` (Square snippet) and rebuild.

## Troubleshooting Dashboard

| Problem | Likely cause | Fix |
|---|---|---|
| Browser can't connect | Laptop not on robot WiFi | Join the robot's network |
| Field view blank | No OpMode running | Start an OpMode |
| No robot drawn | Odometry not updating | Check encoder pods; run Smoke Test |
| `Crawler Tuner` group missing | Tuner not running | Press Play on **Crawler Tuner** |
| Typed value ignored | Field not committed | Press **Enter** in the field |
| Values reset after app restart | Static fields are in-memory | Paste the Square snippet into `MyRobot.java` |

> ⚠️ **Dashboard values are not permanent.** They live in the tuner's static fields for the lifetime of the app. After each session, paste the builder lines from **Square** (or Step 7) into `MyRobot.builder()` and rebuild.

---

## Next Steps

- **[Tuning →](tuning.md)** The full 7-step workflow
- **[Configuration →](configuration.md)** What each Dashboard field does
