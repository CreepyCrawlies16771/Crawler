---
title: Your First Autonomous
description: Write a simple path-following autonomous in 5 minutes
---

# Your First Autonomous

*Making your robot drive itself*

## What Is an Autonomous?

An autonomous (or "auto") is code that runs when the match starts. Your robot drives around the field without any driver input — it just follows the path you programmed.

In FTC, you typically write different autos for red alliance and blue alliance, since the field is mirrored.

## Creating Your First Autonomous

Create a new Java class called `RedAuto.java` in your Crawler folder:

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerAuto;
import org.firstinspires.ftc.teamcode.Crawler.core.Waypoint;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous(name = "Red Auto", group = "Main")
public class RedAuto extends CrawlerAuto<MyRobot> {
    @Override
    public void buildRobot() {
        // This method sets up your robot before the match starts
        robot = new MyRobot();
    }

    @Override
    public void runPath() throws InterruptedException {
        // This method runs when you press Play on the Driver Station
        // Your path goes here
    }
}
```

**Key pieces:**

- `CrawlerAuto<MyRobot>` — This says "I'm writing an autonomous using Crawler and my robot type is MyRobot"
- `@Autonomous(name = "Red Auto", group = "Main")` — This makes your autonomous show up on the Driver Station
- `buildRobot()` — Runs before the match starts. Sets up your robot
- `runPath()` — Runs when you press Play. Your path and actions go here

## Writing Your Path

Now let's add the actual path. This is the easy part. Fill in the `runPath()` method:

```java
@Override
public void runPath() throws InterruptedException {
    // Tell Crawler where we start on the field
    follower.setStartingPose(new PoseData(0, 0, Math.toRadians(0)));
    
    // Build our path - one waypoint at a time
    follower.follow(
        // First waypoint: drive forward 24 inches
        Waypoint.at(0, 24)
            .heading(0)           // Face forward (0 degrees)
            .speed(0.8)           // 80% power
            .buildAll(),
        
        // Second waypoint: turn and drive
        Waypoint.at(24, 24)
            .heading(90)          // Face right (90 degrees)
            .speed(0.8)
            .buildAll(),
        
        // Third waypoint: end at the goal
        Waypoint.at(24, 48)
            .heading(90)
            .speed(0.4)           // Slow down to 40% power
            .slow()               // Special slow mode for precision
            .onReach(() -> robot.openClaw())  // Open claw when we arrive
            .buildAll()
    );
}
```

**What this does:**

- `setStartingPose()` — Tells Crawler where your robot starts (0,0 inches from the corner, facing 0 degrees)
- `Waypoint.at(x, y)` — Coordinates in inches from your starting position
- `.heading(degrees)` — The direction your robot should face at this waypoint
- `.speed(power)` — How fast to drive (0 to 1, where 1 is full speed)
- `.slow()` — A preset that slows down for tight turns and endpoints
- `.onReach(() -> action)` — Do something when the robot arrives at this waypoint
- `.buildAll()` — Finish the waypoint definition and pass it to the follower

> 💡 **Waypoints are like checkpoints on the field.** You give Crawler a list, and it drives your robot through each one automatically, smoothly turning between them.

## Understanding Coordinates

Everything is in inches from your starting position:

- **X-axis** — left (-) and right (+)
- **Y-axis** — backward (-) and forward (+)
- **Heading** — 0° is forward, 90° is right, 180° is backward, 270° is left

## Running Actions at Waypoints

The best part: you can tell the robot to do something when it reaches a waypoint.

```java
Waypoint.at(24, 48)
    .heading(90)
    .onReach(() -> robot.openClaw())
    .buildAll()
```

Here are common actions:

```java
.onReach(() -> robot.openClaw())
.onReach(() -> robot.closeClaw())
.onReach(() -> robot.raiseLift())
.onReach(() -> robot.lowerLift())
.onReach(() -> {
    // You can do multiple things
    robot.openClaw();
    robot.raiseLift();
})
```

> ⚠️ **Keep actions fast.** If your action takes 2 seconds, the robot will wait 2 seconds before moving to the next waypoint.

## Running the Autonomous

Once you've written your autonomous:

1. **Connect your robot** to the Driver Station laptop
2. **Open the Driver Station app** on the robot's phone
3. **Click the Autonomous dropdown** (where it says "No Op Mode Selected")
4. **Select "Red Auto"**
5. **Press Play**

Watch your robot follow the path. If something goes wrong, check the Troubleshooting page.

---

## Next Steps

**[Your First TeleOp →](first-teleop.md)** Write code for driver-controlled play
