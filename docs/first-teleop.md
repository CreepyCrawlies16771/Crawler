---
title: Your First TeleOp
description: Write driver-controlled movement with driveFieldRelative
---

# Your First TeleOp

*Letting the driver control the robot with a gamepad*

## The minimum working example

```java
package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Driver", group = "Crawler Examples")
public class Driver extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);

        waitForStart();

        while (opModeIsActive()) {
            robot.update();   // update odometry first

            // Field-relative drive: "forward" is always away from the robot
            robot.driveFieldRelative(
                    -gamepad1.left_stick_y,   // forward / backward
                    gamepad1.left_stick_x,    // strafe left / right
                    gamepad1.right_stick_x    // rotate
            );

            // Mechanisms
            if (gamepad1.a) robot.openClaw();
            else if (gamepad1.b) robot.closeClaw();

            // Telemetry: where does the robot think it is?
            telemetry.addData("X (cm)", String.format("%.2f", robot.getPose().getX()));
            telemetry.addData("Y (cm)", String.format("%.2f", robot.getPose().getY()));
            telemetry.addData("Heading (°)", String.format("%.2f", Math.toDegrees(robot.getHeading())));
            telemetry.update();

            idle();
        }
        robot.stop();
    }
}
```

## Field-relative vs robot-relative

| | Behavior |
|---|---|
| `robot.driveFieldRelative(f, s, r)` | Forward always points *away from the robot* — like driving a car. Recommended. |
| `robot.drive(forward, strafe, rotate)` | Forward is wherever the robot is *facing* — like flying a drone. Use for precise local adjustments. |

Both take powers from **-1.0 to 1.0**, clamped by `maxDriveSpeed`.

## A two-gamepad setup

Driver 1 moves; Driver 2 runs mechanisms:

```java
while (opModeIsActive()) {
    robot.update();

    // Driver 1 — movement
    robot.driveFieldRelative(
            -gamepad1.left_stick_y,
            gamepad1.left_stick_x,
            gamepad1.right_stick_x);

    // Driver 2 — lift (target positions, RUN_TO_POSITION)
    if (gamepad2.right_trigger > 0.1) robot.scoreHighBasket();
    else if (gamepad2.left_trigger > 0.1) robot.liftMotor.setTargetPosition(0);

    idle();
}
```

## Useful patterns

**Slow mode** — scale the sticks for precision:

```java
double slow = gamepad1.left_trigger > 0.1 ? 0.35 : 1.0;
robot.driveFieldRelative(
        -gamepad1.left_stick_y * slow,
        gamepad1.left_stick_x * slow,
        gamepad1.right_stick_x * slow);
```

**Always call `robot.update()`** at the top of the loop so `getPose()` is fresh — `getPose()` returns **centimeters** for X/Y and **radians** for heading.

> 💡 **See the field view live:** while the **Crawler Tuner** runs, the robot is drawn on the [FTC Dashboard](ftc-dashboard.md) field view automatically. In your own OpMode, add `DashboardFieldViewUtils.drawRobot(...)` to draw it yourself.

---

## Next Steps

- **[Robot-Oriented Movement →](robot-oriented.md)** Precise `drivePID` / `strafePID` / `turnPID` moves
- **[Pure Pursuit →](pure-pursuit.md)** How `FOFollower` steers along paths
- **[Tuning →](tuning.md)** Teach Crawler how *your* robot moves
