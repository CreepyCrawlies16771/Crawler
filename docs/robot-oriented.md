---
title: Robot-Oriented Movement
description: Precise drivePID, strafePID, and turnPID moves with ROMovementEngine
---

# Robot-Oriented Movement

*Telling your robot exactly what to do, step by step*

Pure pursuit is great for long, flowing paths. But for the last few centimeters before a mechanism action — parking, lining up with a basket, backing away — you want simple, predictable commands: *drive 30 cm forward, turn to 45°, strafe 20 cm right*. That's what `RobotOrientedDrive` gives you, driven through the `ROMovementEngine` base class.

## The three commands

| Command | What it does | Units |
|---|---|---|
| `drivePID(meters, headingDeg)` | Drive forward/back while holding a heading | distance in **meters**, heading in **degrees** |
| `strafePID(meters, headingDeg)` | Strafe left/right while holding a heading | positive = right |
| `turnPID(headingDeg)` | Turn in place to an **absolute** heading | degrees (IMU-based) |

All three are blocking (they finish before the next line runs), use `config.timeoutSecs` as a safety timeout, and clamp power to ±0.7 with the `minPower` deadband.

## The pattern: extend `ROMovementEngine`

`ROMovementEngine` wires up `MyRobot`, waits for start, resets the pose, and calls your `runPath()`:

```java
package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@Autonomous(name = "Manual Adjust", group = "Crawler Tests")
public class ManualAdjust extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);      // your configured robot
    }

    @Override
    public void runPath() throws InterruptedException {
        // 30 cm forward, hold 0°
        drivePID(0.30, 0);

        // Turn to an absolute heading of 45°
        turnPID(45);

        // 20 cm right, hold 45° while moving
        strafePID(0.20, 45);

        // Reach the mechanism
        MyRobot robot = (MyRobot) this.robot;
        robot.openClaw();
    }
}
```

## Mixing with pure pursuit

The best of both worlds — long drives as waypoints, precise finishes as PID moves:

```java
@Autonomous(name = "Basket Auto", group = "Main")
public class BasketAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        RobotOrientedDrive ro = new RobotOrientedDrive(robot, this::opModeIsActive, telemetry);

        waitForStart();

        robot.resetPose();   // required before following (CRWL-101 otherwise)

        // Long, smooth approach
        follower.follow(
            Waypoint.at(0, 0, robot.config).build(),
            Waypoint.at(80, 40, robot.config).speed(0.8).build()
        );

        // Precise final alignment
        ro.turnPID(90);          // face the basket
        ro.drivePID(0.15, 90);   // inch in 15 cm
        robot.scoreHighBasket();

        // Retreat
        ro.drivePID(-0.3, 90);
        robot.stop();
    }
}
```

## Tuning the moves

The gains are the same values the [tuner](tuning-guide.md#step-5--pid-drive--strafe--turn--min-power) adjusts:

- `drivePid(kp, ki, kd)` → per-meter gains for `drivePID`
- `strafePid(kp, ki, kd)` → per-meter gains for `strafePID`
- `steerPid(p, i, d)` → per-degree gains for `turnPID` and heading hold
- `minPower` → deadband so tiny errors still move the robot

| Symptom | Fix |
|---|---|
| Stops short consistently | Raise P, or add a little I |
| Oscillates around the target | Lower P, or add D |
| Drifts off heading while driving | Raise `steerP` |
| Wobbles side to side | Lower `steerP` |

> 💡 **Keep robot-relative moves short** (under ~1 m). For long travel, pure pursuit is smoother and uses odometry better.

## When to use which

| Situation | Use |
|---|---|
| Long field traversal | `FOFollower` + waypoints |
| Final alignment before a mechanism | `drivePID` / `strafePID` / `turnPID` |
| Driving in TeleOp | `robot.driveFieldRelative(...)` |
| Raw motor control | `robot.drive(forward, strafe, rotate)` |

---

## Next Steps

- **[Pure Pursuit →](pure-pursuit.md)** The smooth path follower
- **[Configuration →](configuration.md)** All the PID gains in detail
