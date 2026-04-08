# Crawler Documentation

A high-performance, low-complexity autonomous path-following library for FTC robots. Get a functioning autonomous routine in under 30 minutes.

## Quick Navigation

### Getting Started
- [Getting Started & Project Setup](01-getting-started.md) — Prerequisites, first-time setup, and "Hello World" autonomous

### Core Concepts
- [Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md) — The primary localization method explained in depth
- [Robot Configuration & FTC Dashboard](03-robot-configuration.md) — Configuring hardware names, physical constants, and PID tuning

### Movement Systems
- [Robot-Oriented Movement Commands](04-robot-oriented-movement.md) — Imperative PID-based movement: `drivePID()`, `strafePID()`, `turnPID()`
- [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md) — Waypoint-following with built-in heading control and event callbacks

---

## Documentation Scope

This documentation is written for **high school roboticists** in the FIRST Tech Challenge. It assumes:

- **Java experience** (Android Studio, FTC SDK basics)
- **Hardware knowledge** (motors, encoders, IMU, basic kinematics)
- **Willingness to measure** (track width, wheel diameter, etc.)

**What Crawler does:**
- Tracks robot position using encoders (dead wheel odometry) or GoBilda Pinpoint
- Executes imperative movement commands with PID feedback
- Follows waypoint paths autonomously with pure pursuit
- Integrates with FTC Dashboard for real-time tuning

**What Crawler does NOT do:**
- Generate motion profiles (all movement is instantaneous command-based)
- Handle vision-based localization (appendix provided separately)
- Manage mechanical subsystems like shooters, arms, etc.

---

## Reading Order

**First-time users:** Read in this order:
1. [Getting Started & Project Setup](01-getting-started.md)
2. [Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md)
3. [Robot Configuration & FTC Dashboard](03-robot-configuration.md)
4. [Robot-Oriented Movement Commands](04-robot-oriented-movement.md) OR [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md) depending on your autonomous strategy

**Teams with experience:** Jump directly to the movement system that applies to your strategy.

---

## Key Architecture Overview

The Crawler library is organized into these core packages:

```
org.firstinspires.ftc.teamcode.Crawler
├── core/
│   ├── Robot/           CrawlerRobot (hardware abstraction)
│   ├── Localizers/      Position tracking (DeadWheelLocaliser, Pinpoint, MotorEncoder)
│   ├── RobotConfig.java Constants & FTC Dashboard config
│   └── utils/           Math, geometries, waypoint builders
├── RobotOrient/         Imperative movement: drivePID, strafePID, turnPID
├── FieldOrient/         Pure pursuit path follower (FOFollower, RobotMovement)
├── Tuning/              OpModes for calibration
└── Vision/              Optional: AprilTag, vision pipelines
```

All movement is controlled through a single `CrawlerRobot` instance. Hardware names and physical constants are stored in `RobotConfig.java` and pulled from FTC Dashboard.

---

## Troubleshooting Quick Links

- Robot doesn't move straight → See [Odometry Tuning](02-understanding-odometry.md#odometry-tuning)
- Movement commands are jerky → See [PID Tuning](03-robot-configuration.md#pid-tuning)
- Pure pursuit overshoots waypoints → See [Pure Pursuit Calibration](05-field-oriented-pure-pursuit.md#common-pitfalls)
- Encoder values won't update → See [Encoder Inversion & Direction](02-understanding-odometry.md#common-pitfalls)

---

## Next Steps

Choose your path:

- **I want to move the robot in straight lines and turns** → Start with [Robot-Oriented Movement Commands](04-robot-oriented-movement.md)
- **I want to follow a pre-planned path on the field** → Start with [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md)
- **I'm not sure which approach to use** → Read both, they're complementary
