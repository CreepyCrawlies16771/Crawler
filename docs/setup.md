---
title: Setup
description: Create your first three Crawler files and configure your robot
---

# Setup

*Telling Crawler about your robot's hardware*

## What You're Building

Every Crawler project needs three files:

1. **MyRobot** — What motors and sensors your robot has
2. **An Autonomous** — What path to follow (we'll write this next)
3. **A TeleOp** — Driver controls (we'll write this after)

Right now, focus on **MyRobot**. This is where you tell Crawler everything about your robot.

## Creating MyRobot

Create a new Java class in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/` called `MyRobot.java`:

```java
import org.firstinspires.ftc.teamcode.Crawler.core.HardwareProfile;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizer;
import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

public class MyRobot extends CrawlerRobot {
    // Motors for driving
    public DcMotor frontLeft;
    public DcMotor frontRight;
    public DcMotor backLeft;
    public DcMotor backRight;
    
    // Dead wheel motors for tracking position
    public DcMotor deadWheelLeft;
    public DcMotor deadWheelBack;
    
    // Mechanisms
    public Servo claw;

    @Override
    public void init(HardwareMap hardwareMap) {
        super.init(hardwareMap);
        
        // Get motors from hardware map (names must match your Driver Hub config)
        frontLeft = hardwareMap.get(DcMotor.class, "FL");
        frontRight = hardwareMap.get(DcMotor.class, "FR");
        backLeft = hardwareMap.get(DcMotor.class, "BL");
        backRight = hardwareMap.get(DcMotor.class, "BR");
        
        deadWheelLeft = hardwareMap.get(DcMotor.class, "DWL");
        deadWheelBack = hardwareMap.get(DcMotor.class, "DWB");
        
        claw = hardwareMap.get(Servo.class, "claw");
        
        // Set up the localizer (we'll explain this next)
        initLocalizer(Localizer.THREE_DEAD_WHEELS);
        
        // Set motor directions based on your robot's layout
        invertFrontLeft()
            .invertBackRight();
    }
    
    public void openClaw() {
        claw.setPosition(0.5);
    }
    
    public void closeClaw() {
        claw.setPosition(0);
    }
}
```

**What each section does:**

- **Motor variables** — Store references to your motors
- **The `init()` method** — Gets motors from the hardware map when the OpMode starts and sets them up
- **Motor names** — `"FL"`, `"FR"`, etc. must match **exactly** what you named them in the Driver Hub
- **`invertFrontLeft()`** — Flips the direction of that motor (more on this below)
- **Custom methods** — `openClaw()` and `closeClaw()` let your autonomouses and TeleOp control the claw

> ⚠️ **Motor names must match exactly!** If you named a motor "Front Left" in the Driver Hub but write "FL" here, Crawler will crash. Double-check spelling, capitalization, and spacing.

## Choosing a Localizer

A **localizer** is how your robot knows where it is on the field. Crawler supports four options:

| Localizer | Accuracy | Hardware | Best For |
|-----------|----------|----------|----------|
| **Motor Encoders** | ±2-4 inches | Already on your motors | Learning, casual competition |
| **Two Dead Wheels** | ±1 inch | Two shaft encoders | Most teams |
| **Three Dead Wheels** | ±0.5 inches | Three shaft encoders | Competitive teams |
| **Pinpoint** | ±0.25 inches | GoBILDA Pinpoint IMU | Advanced teams |

**Motor Encoders** are built into every FTC motor. Your robot can track its position using just those.

> 💡 **Tip:** Not sure which to pick? Start with Motor Encoders. You can upgrade to dead wheels later without changing your code.

In the code above, we used:

```java
initLocalizer(Localizer.THREE_DEAD_WHEELS);
```

Change this to one of:
- `Localizer.MOTOR_ENCODERS`
- `Localizer.TWO_DEAD_WHEELS`
- `Localizer.THREE_DEAD_WHEELS`
- `Localizer.PINPOINT`

## Motor Directions

When you first test your robot, it might move backwards instead of forwards. This is normal — it just means some motors are wired backwards.

You fix this by inverting them:

```java
invertFrontLeft()          // Reverse front left motor
    .invertBackRight()     // Reverse back right motor
    .invertDeadWheelLeft() // Reverse left dead wheel
```

> 💡 **Tip:** Run the hardware verification step in the tuner (next page) to find out which motors need inverting. You don't have to guess.

---

## Next Steps

**[Your First Autonomous →](first-auto.md)** Write a simple three-waypoint autonomous
