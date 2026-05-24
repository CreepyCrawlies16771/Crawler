---
title: Welcome to Crawler
description: A simple, open source FTC path following library for autonomous movement
---

# Welcome to Crawler

*The easiest way to make your FTC robot drive autonomously*

## What Is Crawler?

**Crawler** is an FTC library that handles the complex math of autonomous movement. You tell it where you want your robot to go, and it drives there. That's it.

Road Runner and Pedro Pathing are powerful but take days to learn. Crawler gets your robot moving autonomously in under 30 minutes — and you'll actually understand what's happening.

## What Can It Do?

**🎯 Simple Setup**
Install Crawler, write three simple Java files, and you're ready to go. No math degrees required.

**🤖 Two Movement Modes**
Choose **robot-oriented** for precise, step-by-step commands. Or use **pure pursuit** for smooth AI-like paths that competitive teams use.

**⚙️ Built-In Guided Tuning**
The tuner walks you through 11 simple steps to teach Crawler exactly how your robot moves. Takes 45 minutes the first time.

## See It In Action

This is all you need to write an autonomous that follows three waypoints and opens a claw:

```java
import org.firstinspires.ftc.teamcode.Crawler.CrawlerAuto;

public class RedAuto extends CrawlerAuto<MyRobot> {
    @Override
    public void runPath() throws InterruptedException {
        follower.follow(
            Waypoint.at(0, 0)
                .heading(0)
                .speed(0.8),
            
            Waypoint.at(24, 24)
                .heading(45)
                .speed(0.8),
            
            Waypoint.at(48, 12)
                .heading(0)
                .onReach(() -> robot.openClaw())
                .buildAll()
        );
    }
}
```

Your robot will navigate that path perfectly. No PID tuning. No motion profiles. No physics equations.

## Ready? Start Here

You have two paths forward:

- **New to autonomous?** → Start with [Installation](installation.md), then [Setup](setup.md)
- **Already familiar?** → Jump to [Your First Autonomous](first-auto.md)

---

## Next Steps

**[Installation →](installation.md)** Learn how to add Crawler to your project
