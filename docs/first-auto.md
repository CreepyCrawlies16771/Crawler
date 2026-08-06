---
title: Your First Autonomous
description: Write a path-following autonomous with FOFollower and Waypoint
---

# Your First Autonomous

*Making your robot drive itself*

An autonomous runs when the match starts, with no driver input. With Crawler you describe the path with `Waypoint`s and a `FOFollower` drives it using pure pursuit.

## The minimum working example

```java
package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

@Autonomous(name = "Red Auto", group = "Crawler Examples")
public class RedAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Build the robot (runs during INIT)
        MyRobot robot = new MyRobot(hardwareMap);

        // 2. Create the follower. The lambda lets it check "am I still running?"
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

        waitForStart();

        // 3. Set the starting pose (required — CRWL-101 fires inside follow() otherwise)
        robot.resetPose();

        // 4. Follow the path (blocking — runs until done or stopped)
        follower.follow(
            Waypoint.at(0, 0, robot.config).build(),
            Waypoint.at(100, 0, robot.config).speed(0.8).build(),
            Waypoint.at(100, 100, robot.config)
                .slow(robot.config)
                .onReach(robot::openClaw)
                .build()
        );

        // 4. Always stop the robot when done
        robot.stop();
    }
}
```

## What each piece does

- **`new MyRobot(hardwareMap)`** — builds your robot (drivetrain, localizer, mechanisms) during INIT
- **`new FOFollower(robot, telemetry, this::opModeIsActive)`** — the path follower; the third argument lets it stop immediately when you press STOP
- **`Waypoint.at(x, y, robot.config)`** — a goal position in **centimeters**, using your robot's tuned defaults for speed / turn speed / follow distance
- **`.speed(0.8)`** — override this waypoint's move speed (0.0–1.0)
- **`.slow(robot.config)`** — use the tuned slow-mode speeds for precision
- **`.onReach(() -> …)`** — run code the moment the robot arrives
- **`.build()`** — finish the waypoint (required)

> ⚠️ `follow()` needs **at least two** waypoints — the first is the starting point, the rest are goals. And always pass your robot's config to `Waypoint.at(x, y, robot.config)` so it picks up your tuned defaults (a bare `Waypoint.at(x, y)` overload exists, but it falls back to the library's built-in defaults).

## Understanding coordinates

Coordinates are **centimeters** from your starting position, in the **field** frame:

<div class="diagram" role="img" aria-label="Field coordinate system with waypoints">
<svg viewBox="0 0 560 440" xmlns="http://www.w3.org/2000/svg" font-family="'JetBrains Mono', monospace">
  <defs>
    <linearGradient id="fg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#0f1720"/><stop offset="1" stop-color="#111827"/>
    </linearGradient>
  </defs>
  <rect x="40" y="40" width="480" height="360" rx="14" fill="url(#fg)" stroke="#374151"/>
  <g stroke="#1f2937">
    <line x1="160" y1="40" x2="160" y2="400"/>
    <line x1="280" y1="40" x2="280" y2="400"/>
    <line x1="400" y1="40" x2="400" y2="400"/>
    <line x1="40" y1="160" x2="520" y2="160"/>
    <line x1="40" y1="280" x2="520" y2="280"/>
  </g>
  <g stroke="#4ADE80" stroke-width="3" fill="none">
    <path d="M90 320 L250 320 L250 120 L90 120 Z" opacity="0.9"/>
  </g>
  <g>
    <circle cx="90"  cy="320" r="8" fill="#22C55E"/>
    <circle cx="250" cy="320" r="8" fill="#22C55E"/>
    <circle cx="250" cy="120" r="8" fill="#4ADE80"/>
    <circle cx="90"  cy="120" r="8" fill="#4ADE80"/>
  </g>
  <g fill="#E5E7EB" font-size="13" text-anchor="middle">
    <text x="90"  y="352">(0, 0)</text>
    <text x="250" y="352">(100, 0)</text>
    <text x="250" y="106">(100, 100)</text>
    <text x="90"  y="106">(0, 100)</text>
  </g>
  <g fill="#9CA3AF" font-size="12">
    <text x="20" y="60">Y+ forward</text>
    <text x="440" y="415">X+ right →</text>
  </g>
</svg>
</div>

- **X** — right is positive, left is negative
- **Y** — forward is positive, backward is negative
- Heading comes from the IMU; 0° is the robot's direction at start

## Running actions at waypoints

Actions run the instant the robot arrives — open a claw, raise a lift, fire a mechanism:

```java
Waypoint.at(60, 0, robot.config)
        .onReach(() -> {
            robot.openClaw();
            telemetry.addData("Action", "Open claw");
            telemetry.update();
        })
        .build()
```

> ⚠️ **Keep actions fast.** `follow()` blocks, so a 2-second action pauses the robot for 2 seconds before it drives to the next waypoint.

## Mixing in precise moves

Pure pursuit is great for long drives; finish with a precise robot-relative move via `RobotOrientedDrive` (see [robot-oriented.md](robot-oriented.md)):

```java
import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.RobotOrientedDrive;

RobotOrientedDrive ro = new RobotOrientedDrive(robot, this::opModeIsActive, telemetry);

robot.resetPose();      // required before following
follower.follow(
    Waypoint.at(0, 0, robot.config).build(),
    Waypoint.at(48, 36, robot.config).speed(0.8).build()
);

ro.drivePID(0.2, 90);   // inch forward 20 cm, hold 90°
robot.openClaw();
```

## Running it

1. Deploy the app to the robot
2. On the Driver Station, select **Red Auto**
3. Press **Play**

If the robot doesn't move, check the [Troubleshooting](troubleshooting.md) page first — 9 times out of 10 it's a device name mismatch or an untuned robot.

---

## Next Steps

**[Your First TeleOp →](first-teleop.md)** Driver-controlled movement
