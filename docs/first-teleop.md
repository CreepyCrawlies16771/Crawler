---
title: Your First TeleOp
description: Write code for driver-controlled play time
---

# Your First TeleOp

*Letting the driver control the robot with a gamepad*

## Creating Your TeleOp

Create a new Java class called `Driver.java`:

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerTeleOp;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Driver", group = "Main")
public class Driver extends CrawlerTeleOp<MyRobot> {
    @Override
    public void buildRobot() {
        robot = new MyRobot();
    }

    @Override
    public void loop() {
        // This runs repeatedly while the OpMode is active
        
        // Drive the robot
        driveFieldRelative(gamepad1);
        
        // Control the claw
        if (gamepad2.a) {
            robot.openClaw();
        } else if (gamepad2.b) {
            robot.closeClaw();
        }
        
        // Update any mechanisms that need it
        telemetry.addData("Status", "Running");
        telemetry.update();
    }
}
```

**Key pieces:**

- `CrawlerTeleOp<MyRobot>` — Tells Crawler this is a TeleOp using MyRobot
- `buildRobot()` — Sets up your robot before the driver takes control
- `loop()` — Runs repeatedly (about 50 times per second) while the match is running
- `gamepad1` — The driver's controller
- `gamepad2` — The operator's controller (usually controls mechanisms)

## Driving the Robot

Crawler gives you two options for steering:

### Field-Relative Drive (Recommended)

```java
driveFieldRelative(gamepad1);
```

This is like driving a car: the robot moves based on which direction is "forward," regardless of which way the robot is facing.

Use this for most of your robot's movement.

### Robot-Relative Drive

```java
driveRobotRelative(gamepad1);
```

The robot moves based on which way it's facing. If you push the stick forward but the robot is facing left, it drives left.

Use this only if you want the driver to think differently.

> 💡 **Analogy:** Field-relative is like a self-driving car that always knows north. Robot-relative is like flying a drone where "forward" always means "forward from the drone's perspective."

## Controlling Mechanisms

Use button presses to control claws, lifts, and other mechanisms:

```java
if (gamepad2.a) {
    robot.openClaw();
}

if (gamepad2.b) {
    robot.closeClaw();
}

if (gamepad2.x) {
    robot.raiseLift();
}

if (gamepad2.y) {
    robot.lowerLift();
}
```

**Available buttons:**

- `gamepad2.a`, `gamepad2.b`, `gamepad2.x`, `gamepad2.y` — the colored buttons
- `gamepad2.dpad_up`, `gamepad2.dpad_down`, `gamepad2.dpad_left`, `gamepad2.dpad_right` — the D-pad
- `gamepad2.left_bumper`, `gamepad2.right_bumper` — the shoulder buttons (LB/RB)
- `gamepad2.left_trigger`, `gamepad2.right_trigger` — the analog triggers (0 to 1)

## Continuous Updates

Some mechanisms need to be told what to do every single loop cycle.

For example, if your lift has gravity, you need to give it power every loop to hold it up:

```java
@Override
public void loop() {
    driveFieldRelative(gamepad1);
    
    // Hold the lift at current position
    robot.updateLift();
    
    telemetry.addData("Status", "Running");
    telemetry.update();
}
```

If you forget to call `updateLift()` every loop, the lift will fall.

> 💡 **Rule of thumb:** If a mechanism needs continuous power to stay in place, update it every loop.

## Telemetry (Debugging Info)

Show information on the driver station screen:

```java
@Override
public void loop() {
    driveFieldRelative(gamepad1);
    
    // Show debug info
    telemetry.addData("Robot X Position", follower.getPoseEstimate().x);
    telemetry.addData("Robot Y Position", follower.getPoseEstimate().y);
    telemetry.addData("Claw Position", robot.claw.getPosition());
    telemetry.update();
}
```

This is super helpful for fixing problems. You can see your robot's exact position, sensor values, and more in real-time.

---

## Next Steps

Ready to go deeper?

- **[Robot-Oriented Movement →](robot-oriented.md)** Learn precise step-by-step commands
- **[Pure Pursuit →](pure-pursuit.md)** Understand the smooth path-following algorithm
- **[Tuning →](tuning.md)** Teach Crawler how your specific robot moves
