---
title: Full Example
description: A complete real-world autonomous with all the pieces
---

# Full Example

*From hardware to autonomous: one complete example*

## The Complete Picture

This page shows four complete, realistic Java files from a full FTC season build. Copy these, adapt the hardware names and positions to your field, and you have a working autonomous.

## MyRobot.java

This is your hardware profile. It defines all motors, servos, and which localizer you're using.

```java
import org.firstinspires.ftc.teamcode.Crawler.core.HardwareProfile;
import org.firstinspires.ftc.teamcode.Crawler.core.Localizer;
import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * MyRobot represents our FTC robot.
 * 
 * This is where we tell Crawler:
 * - What motors we have and what they're named
 * - What sensors we use (localizers)
 * - What mechanisms we control
 */
public class MyRobot extends CrawlerRobot {
    // Drive motors (mecanum chassis)
    public DcMotor frontLeft;
    public DcMotor frontRight;
    public DcMotor backLeft;
    public DcMotor backRight;
    
    // Odometry motors (three dead wheels)
    public DcMotor deadWheelLeft;
    public DcMotor deadWheelCenter;
    public DcMotor deadWheelRight;
    
    // Mechanisms
    public Servo claw;
    public DcMotor lift;
    
    @Override
    public void init(HardwareMap hardwareMap) {
        super.init(hardwareMap);
        
        // Get drive motors from hardware map
        // IMPORTANT: These names MUST match your Driver Hub configuration exactly
        frontLeft = hardwareMap.get(DcMotor.class, "FL");
        frontRight = hardwareMap.get(DcMotor.class, "FR");
        backLeft = hardwareMap.get(DcMotor.class, "BL");
        backRight = hardwareMap.get(DcMotor.class, "BR");
        
        // Get odometry motors
        deadWheelLeft = hardwareMap.get(DcMotor.class, "DWL");
        deadWheelCenter = hardwareMap.get(DcMotor.class, "DWC");
        deadWheelRight = hardwareMap.get(DcMotor.class, "DWR");
        
        // Get mechanism motors and servos
        claw = hardwareMap.get(Servo.class, "claw");
        lift = hardwareMap.get(DcMotor.class, "lift");
        
        // Set the localizer type (we're using three dead wheels for accuracy)
        initLocalizer(Localizer.THREE_DEAD_WHEELS);
        
        // Invert motors based on our chassis layout
        // (Your robot might be different - run the tuner to figure this out)
        invertFrontRight()
            .invertBackLeft();
    }
    
    /**
     * Open the claw to release game pieces
     */
    public void openClaw() {
        claw.setPosition(0.5);  // 0.5 = open position
    }
    
    /**
     * Close the claw to grip game pieces
     */
    public void closeClaw() {
        claw.setPosition(0.15);  // 0.15 = closed position
    }
    
    /**
     * Raise the lift to place pieces at height
     */
    public void raiseLift() {
        lift.setPower(0.8);  // Positive power = up
    }
    
    /**
     * Lower the lift back down
     */
    public void lowerLift() {
        lift.setPower(-0.8);  // Negative power = down
    }
    
    /**
     * Stop the lift (hold position against gravity)
     */
    public void holdLift() {
        lift.setPower(0.1);  // Small power to hold against gravity
    }
}
```

## RedAuto.java

A typical autonomous for the red alliance. This robot drives to specimen pickup, picks up three specimens, and places them in the basket.

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerAuto;
import org.firstinspires.ftc.teamcode.Crawler.core.Waypoint;
import org.firstinspires.ftc.teamcode.Crawler.core.PoseData;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * RedAuto: Full autonomous for red alliance
 * 
 * Strategy:
 * 1. Start at red specimen staging area
 * 2. Drive to basket area
 * 3. Place preloaded specimen in basket
 * 4. Drive back to specimen pickup
 * 5. Pick up three more specimens
 * 6. Place each one in basket
 * 
 * Total time: ~25 seconds
 */
@Autonomous(name = "Red Auto", group = "Main")
public class RedAuto extends CrawlerAuto<MyRobot> {
    @Override
    public void buildRobot() {
        // Create and initialize the robot
        robot = new MyRobot();
    }

    @Override
    public void runPath() throws InterruptedException {
        // Set starting position on the field
        // Our robot starts at the left side (X=0), facing the basket (Y+ direction)
        follower.setStartingPose(new PoseData(0, 0, Math.toRadians(0)));
        
        // ===== FIRST SPECIMEN (PRELOADED) =====
        // Drive to the high basket and place the preloaded specimen
        follower.follow(
            // Drive to center field, heading toward basket
            Waypoint.at(0, 12)
                .speed(0.8)
                .heading(0)
                .buildAll(),
            
            // Approach the basket
            Waypoint.at(12, 24)
                .speed(0.6)
                .heading(45)
                .buildAll(),
            
            // Line up with basket (slow for precision)
            Waypoint.at(15, 28)
                .heading(45)
                .speed(0.3)
                .slow()
                // When we reach the basket, open the claw to drop the specimen
                .onReach(() -> {
                    robot.openClaw();
                    // Give it a moment to open
                    try { Thread.sleep(300); } catch (InterruptedException e) {}
                })
                .buildAll()
        );
        
        // ===== BACK TO PICKUP =====
        // Drive back to the specimen staging area to pick up more
        follower.follow(
            // Leave basket
            Waypoint.at(12, 24)
                .speed(0.7)
                .heading(180)
                .buildAll(),
            
            // Approach pickup zone
            Waypoint.at(0, 12)
                .speed(0.7)
                .heading(180)
                .buildAll(),
            
            // Align with next specimen (slow mode)
            Waypoint.at(-2, 6)
                .speed(0.3)
                .heading(180)
                .slow()
                // Close claw on next specimen
                .onReach(() -> robot.closeClaw())
                .buildAll()
        );
        
        // Small pause to ensure claw grips
        sleep(200);
        
        // Raise lift slightly to clear ground
        robot.raiseLift();
        sleep(300);
        
        // ===== SECOND SPECIMEN =====
        // Drive back to basket with the second specimen
        follower.follow(
            Waypoint.at(0, 12)
                .speed(0.8)
                .heading(0)
                .buildAll(),
            
            Waypoint.at(12, 24)
                .speed(0.6)
                .heading(45)
                .buildAll(),
            
            // Place specimen
            Waypoint.at(15, 28)
                .heading(45)
                .speed(0.3)
                .slow()
                .onReach(() -> {
                    robot.lowerLift();
                    try { Thread.sleep(200); } catch (InterruptedException e) {}
                    robot.openClaw();
                    try { Thread.sleep(300); } catch (InterruptedException e) {}
                })
                .buildAll()
        );
        
        // ===== THIRD SPECIMEN =====
        // Same pattern: back to pickup, place, repeat
        follower.follow(
            Waypoint.at(12, 24)
                .speed(0.7)
                .heading(180)
                .buildAll(),
            
            Waypoint.at(-2, 6)
                .speed(0.3)
                .heading(180)
                .slow()
                .onReach(() -> robot.closeClaw())
                .buildAll()
        );
        
        sleep(200);
        robot.raiseLift();
        sleep(300);
        
        // Place third specimen
        follower.follow(
            Waypoint.at(0, 12)
                .speed(0.8)
                .heading(0)
                .buildAll(),
            
            Waypoint.at(12, 24)
                .speed(0.6)
                .heading(45)
                .buildAll(),
            
            Waypoint.at(15, 28)
                .heading(45)
                .speed(0.3)
                .slow()
                .onReach(() -> {
                    robot.lowerLift();
                    try { Thread.sleep(200); } catch (InterruptedException e) {}
                    robot.openClaw();
                    try { Thread.sleep(300); } catch (InterruptedException e) {}
                })
                .buildAll()
        );
        
        // Autonomous complete, hold lift to prevent gravity drop
        robot.holdLift();
    }
}
```

## BlueAuto.java

The blue alliance version. It's almost identical to RedAuto, just with mirrored coordinates.

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerAuto;
import org.firstinspires.ftc.teamcode.Crawler.core.Waypoint;
import org.firstinspires.ftc.teamcode.Crawler.core.PoseData;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * BlueAuto: Same as RedAuto but mirrored for blue alliance
 * 
 * The code structure is identical - only the X coordinates are negative (left instead of right)
 */
@Autonomous(name = "Blue Auto", group = "Main")
public class BlueAuto extends CrawlerAuto<MyRobot> {
    @Override
    public void buildRobot() {
        robot = new MyRobot();
    }

    @Override
    public void runPath() throws InterruptedException {
        // Start on blue side (left side of field from above)
        follower.setStartingPose(new PoseData(0, 0, Math.toRadians(0)));
        
        // ===== FIRST SPECIMEN (PRELOADED) =====
        follower.follow(
            Waypoint.at(0, 12)
                .speed(0.8)
                .heading(0)
                .buildAll(),
            
            // X is now NEGATIVE because we're on the blue side
            Waypoint.at(-12, 24)
                .speed(0.6)
                .heading(-45)
                .buildAll(),
            
            Waypoint.at(-15, 28)
                .heading(-45)
                .speed(0.3)
                .slow()
                .onReach(() -> {
                    robot.openClaw();
                    try { Thread.sleep(300); } catch (InterruptedException e) {}
                })
                .buildAll()
        );
        
        // ===== BACK TO PICKUP =====
        follower.follow(
            Waypoint.at(-12, 24)
                .speed(0.7)
                .heading(180)
                .buildAll(),
            
            Waypoint.at(0, 12)
                .speed(0.7)
                .heading(180)
                .buildAll(),
            
            Waypoint.at(2, 6)  // Mirrored X coordinate
                .speed(0.3)
                .heading(180)
                .slow()
                .onReach(() -> robot.closeClaw())
                .buildAll()
        );
        
        sleep(200);
        robot.raiseLift();
        sleep(300);
        
        // Continue with specimens 2 and 3 (pattern repeats)
        // ... rest of code is same pattern, just X values are negative ...
    }
}
```

## Driver.java

The TeleOp code for manual driver-controlled play.

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerTeleOp;

/**
 * Driver: Manual control during TeleOp period
 * 
 * Controls:
 * - Gamepad 1 (driver): Movement
 * - Gamepad 2 (operator): Claw and lift mechanisms
 */
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Driver", group = "Main")
public class Driver extends CrawlerTeleOp<MyRobot> {
    @Override
    public void buildRobot() {
        robot = new MyRobot();
    }

    @Override
    public void loop() {
        // Let Crawler handle the drive commands from gamepad1
        // Field-relative means "forward" is always away from the current heading
        driveFieldRelative(gamepad1);
        
        // OPERATOR CONTROLS (Gamepad 2)
        
        // A button: Close claw (pick up)
        if (gamepad2.a) {
            robot.closeClaw();
        }
        
        // B button: Open claw (drop)
        if (gamepad2.b) {
            robot.openClaw();
        }
        
        // Right bumper: Raise lift
        if (gamepad2.right_bumper) {
            robot.raiseLift();
        }
        
        // Left bumper: Lower lift
        if (gamepad2.left_bumper) {
            robot.lowerLift();
        }
        
        // Y button: Stop lift (hold position)
        if (gamepad2.y) {
            robot.holdLift();
        }
        
        // TELEMETRY (Debug info shown on Driver Station)
        telemetry.addData("Robot Position X", String.format("%.2f in", follower.getPoseEstimate().x));
        telemetry.addData("Robot Position Y", String.format("%.2f in", follower.getPoseEstimate().y));
        telemetry.addData("Robot Heading", String.format("%.1f°", Math.toDegrees(follower.getPoseEstimate().heading)));
        telemetry.addData("Claw Position", robot.claw.getPosition());
        telemetry.addData("FPS", String.format("%.1f", 1000.0 / (System.nanoTime() - prevTime) / 1e6));
        
        prevTime = System.nanoTime();
        telemetry.update();
    }
    
    private long prevTime = System.nanoTime();
}
```

## MyTuner.java

The single file you create to run the complete tuning routine.

```java
import org.firstinspires.ftc.teamcode.Crawler.core.CrawlerTuner;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * MyTuner: Run the guided tuning process
 * 
 * This single file enables the entire 11-step tuning process.
 * The tuner handles all the heavy lifting - you just follow the on-screen prompts.
 * 
 * First run: 45-60 minutes
 * Re-tune single step: 5 minutes
 */
@Autonomous(name = "Tuner", group = "Tuning")
public class MyTuner extends CrawlerTuner<MyRobot> {
    @Override
    public void buildRobot() {
        robot = new MyRobot();
    }
}
```

## Putting It Together

Here's all you need to do:

1. Create these four files in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/`
2. Adjust motor names to match your Driver Hub configuration
3. Adjust waypoint coordinates to match your field (this example uses approximate values)
4. Run the tuner first (MyTuner.java)
5. Run RedAuto or BlueAuto during autonomous
6. Run Driver during TeleOp

That's it. You now have a complete working autonomous and TeleOp.

---

## Next Steps

You've learned everything you need. Go back to troubleshooting if you run into any issues, or ask your coach.

Good luck. You've got this. 🚀
