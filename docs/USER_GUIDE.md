---
title: Crawler User Guide
description: A practical, code-first tour of the Crawler library
---

# Crawler User Guide

*A practical tour of the Crawler library — what's here, where it lives, and how to use it.*

Crawler is a straightforward pathing library for FTC teams. It's written to be *readable*: every control loop is a few dozen lines you can actually understand, and the whole library lives in source so you can step through it in the debugger.

## Where everything lives

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/                          ← the library (read, don't break)
│   ├── core/
│   │   ├── Robot/CrawlerRobot.java   ← the robot + builder + Config
│   │   ├── Localizers/               ← 5 localizers (3DW, 2DW, Pinpoint, motors, dev)
│   │   └── utils/                    ← Waypoint, Point, Vector2d, CrawlerMath, UnitConverter
│   ├── FieldOrient/                  ← FOFollower + RobotMovement (pure pursuit)
│   ├── RobotOrient/                  ← RobotOrientedDrive + ROMovementEngine
│   ├── Tuning/                       ← TuningSession + TuningConfig (FTC Dashboard)
│   └── Dashboard/                    ← field-view drawing helpers
└── TeamscodeNotLibrary/              ← YOUR code (edit freely)
    ├── MyRobot.java                 ← names + localizer + tuning in one builder
    ├── CrawlerTuner.java            ← the tuning OpMode (rebuilds your registered robot)
    ├── ExampleAuto.java / ExampleTeleOp.java
    ├── CrawlerSmokeTest.java / CrawlerSystemTest.java
    └── ManualAdjustExample.java
```

## The mental model

1. **`CrawlerRobot`** is the chassis. It owns four motors, the IMU, a localizer, and one `Config` object of tuned numbers.
2. **`MyRobot extends CrawlerRobot`** adds your mechanisms (claw, lift, intake…) via the builder in its constructor. `MyRobot` is just the example name — any subclass of `CrawlerRobot` works, and the library never references it directly.
3. **OpModes** build a `MyRobot`, then either follow **waypoints** (`FOFollower`) or run precise **PID moves** (`RobotOrientedDrive`).
4. **The tuner** calibrates the numbers in `Config` so the robot's estimates match reality.

## The 60-second example

**TeleOp** (field-relative driving):

```java
@TeleOp(name = "Driver", group = "Crawler Examples")
public class Driver extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        waitForStart();
        while (opModeIsActive()) {
            robot.update();
            robot.driveFieldRelative(
                    -gamepad1.left_stick_y,
                    gamepad1.left_stick_x,
                    gamepad1.right_stick_x);
            if (gamepad1.a) robot.openClaw();
            idle();
        }
        robot.stop();
    }
}
```

**Autonomous** (pure pursuit):

```java
@Autonomous(name = "Red Auto", group = "Crawler Examples")
public class RedAuto extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        waitForStart();
        robot.resetPose();   // required before following (CRWL-101 otherwise)
        follower.follow(
            Waypoint.at(0, 0, robot.config).build(),
            Waypoint.at(100, 0, robot.config).speed(0.8).build(),
            Waypoint.at(100, 100, robot.config).onReach(robot::openClaw).build()
        );
        robot.stop();
    }
}
```

**Precise moves** (robot-relative PID) — use `ROMovementEngine` as the base class (see [robot-oriented.md](robot-oriented.md)):

```java
// Inside a class that extends ROMovementEngine:
drivePID(0.30, 0);   // 30 cm forward, hold 0°
turnPID(45);         // face absolute 45°
strafePID(0.20, 45); // 20 cm right, hold 45°
```

## What Crawler is (and isn't)

**Is:** readable, debuggable, source-visible pathing — odometry, robot-relative PID, and pure pursuit, plus a guided tuner.

**Isn't:** a trajectory optimizer, a physics engine, or a magic black box. If you outgrow it, the pure-pursuit loop in `RobotMovement` is a great first read before moving to Road Runner.

## Before you compete

1. No sync needed — the tuner rebuilds your registered robot with live values
2. Run the **Crawler Tuner** end-to-end and paste Step 7's builder lines into your robot's `builder()`
3. Run **Crawler Smoke Test** (2 min) → **Crawler System Test** (15 min)
4. Tune at competition voltage

## Getting help

- **Docs** — start at [Installation](installation.md), then [Setup](setup.md)
- **Examples** — `TeamscodeNotLibrary/ExampleAuto.java`, `ExampleTeleOp.java`, `ManualAdjustExample.java`
- **FTC official** — [ftc-docs.firstinspires.org](https://ftc-docs.firstinspires.org/), [FTC Javadoc](https://javadoc.io/doc/org.firstinspires.ftc)

---

## Next Steps

**[Installation →](installation.md)** Get Crawler into your project
