---
title: Crawler — Simple FTC Pathing
description: A simple, open source FTC path-following library. Pure pursuit, robot-oriented PID, and a guided tuner.
---

# Crawler

*A friendly FTC pathing library, that aims to make programming enjoyable for the average user*

Crawler is a lightweight, readable FTC library for autonomous movement. You describe **where** you want the robot to go and it figures out **how** to get there — smooth pure-pursuit paths, precise robot-relative moves, and a guided tuner that calibrates odometry and PID to *your* specific robot.

Road Runner and Pedro Pathing are powerful, but they take days to learn. Crawler gets a rookie team driving a real path in an afternoon — and the source is short enough that you can actually understand what it does.

## What you get

| | |
|---|---|
| **Pure pursuit paths** | `FOFollower` + `Waypoint` drive smooth, curved paths between field coordinates. |
| **Robot-relative moves** | `drivePID`, `strafePID`, `turnPID` for precise final alignments. |
| **Guided tuner** | One OpMode walks you through odometry, PID, and path tuning — with live editing in **FTC Dashboard**. |
| **Any localizer** | Three dead wheels, two dead wheels, GoBILDA Pinpoint, motor encoders. |

## The whole idea in 20 lines

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
            Waypoint.at(100, 0, robot.config)
                .speed(0.8)
                .onReach(robot::openClaw)
                .build(),
            Waypoint.at(100, 100, robot.config).slow(robot.config).build()
        );
        robot.stop();
    }
}
```

## Tune once, drive forever

Every robot is different: different wheel diameters, different track widths, different friction. The **Crawler Tuner** OpMode runs 7 quick tests, and every value is editable live in the **FTC Dashboard** config panel:

<div class="diagram" role="img" aria-label="The seven tuning steps">
<svg viewBox="0 0 880 120" xmlns="http://www.w3.org/2000/svg" font-family="'JetBrains Mono', monospace">
  <defs>
    <linearGradient id="g1" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0" stop-color="var(--green)"/><stop offset="1" stop-color="var(--green-strong)"/>
    </linearGradient>
  </defs>
  <g fill="var(--diagram-bg)" stroke="none">
    <rect x="10"  y="30" width="112" height="60" rx="10" stroke="var(--green)" stroke-width="2"/>
    <rect x="134" y="30" width="112" height="60" rx="10" stroke="var(--green-strong)" stroke-width="2"/>
    <rect x="258" y="30" width="112" height="60" rx="10" stroke="var(--green-strong)" stroke-width="2"/>
    <rect x="382" y="30" width="112" height="60" rx="10" stroke="var(--green-strong)" stroke-width="2"/>
    <rect x="506" y="30" width="112" height="60" rx="10" stroke="var(--green-strong)" stroke-width="2"/>
    <rect x="630" y="30" width="112" height="60" rx="10" stroke="var(--green-strong)" stroke-width="2"/>
    <rect x="754" y="30" width="116" height="60" rx="10" stroke="url(#g1)" stroke-width="3"/>
  </g>
  <g fill="var(--diagram-text)" font-size="12" text-anchor="middle">
    <text x="66"  y="50">1 · Motors</text>
    <text x="66"  y="66">2 · Encoders</text>
    <text x="190" y="50">3 · Track</text>
    <text x="190" y="66">width</text>
    <text x="314" y="50">4 · Center</text>
    <text x="314" y="66">offset</text>
    <text x="438" y="50">5 · PID</text>
    <text x="438" y="66">(P·I·D)</text>
    <text x="562" y="50">6 · Auto</text>
    <text x="562" y="66">path</text>
    <text x="686" y="50">7 · Copy</text>
    <text x="686" y="66">snippet</text>
    <text x="812" y="50">Finish</text>
    <text x="812" y="66">→ MyRobot</text>
  </g>
  <g stroke="var(--green)" stroke-width="2.5" fill="none">
    <path d="M122 60 H134"/><path d="M246 60 H258"/><path d="M370 60 H382"/>
    <path d="M494 60 H506"/><path d="M618 60 H630"/><path d="M742 60 H754"/>
  </g>
</svg>
</div>

When you finish, the tuner prints the exact tuned values as **builder lines** to paste into `MyRobot.builder()` (`MyRobot` is just the example name):

```java
// Example generated output from the tuner. Paste in your robot class:
.setTrackWidth(13.0000)
.setCenterWheelOffset(3.5000)
.wheelDiameter(1.3780)
.ticksPerRev(2000)
.drivePid(0.0500, 0.0000, 0.0000)
.strafePid(0.0500, 0.0000, 0.0000)
.steerPid(0.0300, 0.0000, 0.0000)
.minPower(0.1500)
.pathDefaults(0.7000, 0.4000, 25.4000)
.arrivalThresholdCm(5.0000)
.orbitThresholdCm(25.4000)
.timeoutSecs(5.0000)
.maxDriveSpeed(1.0000)
```

## Start here

**New to autonomous?** Follow these pages in order:

1. [Installation](installation.md) — get Crawler into your FTC SDK project
2. [Setup](setup.md) — create `MyRobot.java` (names + localizer + tuning in one builder) and choose a localizer
3. [Your First Autonomous](first-auto.md) — drive a real path in 5 minutes
4. [Your First TeleOp](first-teleop.md) — driver-controlled movement

**Already familiar?** Jump straight to the [Full Example](example.md) or the [Tuning Guide](tuning-guide.md).

## Where everything lives

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/                          ← the library (read, don't break)
│   ├── core/Robot/CrawlerRobot.java  ← the robot + builder + Config
│   ├── core/Localizers/              ← 5 localizers (3DW, 2DW, Pinpoint, motors, dev)
│   ├── core/utils/                   ← Waypoint, Point, Vector2d, CrawlerMath, UnitConverter
│   ├── FieldOrient/                  ← FOFollower + RobotMovement (pure pursuit)
│   ├── RobotOrient/                  ← RobotOrientedDrive + ROMovementEngine
│   ├── Tuning/                       ← TuningSession + TuningConfig (FTC Dashboard)
│   └── Dashboard/                    ← field-view drawing helpers
└── Teamcode/                       ← YOUR code (edit freely)
    ├── Examples/                     ← MyRobot, ExampleAuto, ExampleTeleOp, ManualAdjustExample
    └── CrawlerOpModes/               ← CrawlerTuner, CrawlerSmokeTest, CrawlerSystemTest
```

## The mental model

1. **`CrawlerRobot`** is the chassis. It owns four motors, the IMU, a localizer, and one `Config` object of tuned numbers.
2. **`MyRobot extends CrawlerRobot`** adds your mechanisms (claw, lift, intake…) via the builder in its constructor. `MyRobot` is just the example name — any subclass of `CrawlerRobot` works, and the library never references it directly.
3. **OpModes** build a `MyRobot`, then either follow **waypoints** (`FOFollower`) or run precise **PID moves** (`RobotOrientedDrive`).
4. **The tuner** calibrates the numbers in `Config` so the robot's estimates match reality.

## Precise moves

For short robot-relative moves (nudge forward, align heading), use `ROMovementEngine` as the base class — see [robot-oriented.md](robot-oriented.md):

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
2. Run the **Crawler Tuner** end-to-end and paste the printed builder lines into your robot's `builder()`
3. Run **Crawler Smoke Test** (2 min) → **Crawler System Test** (15 min)
4. Tune at competition voltage

## Getting help

- **Docs** — start at [Installation](installation.md), then [Setup](setup.md)
- **Examples** — `Teamcode/Examples/ExampleAuto.java`, `ExampleTeleOp.java`, `ManualAdjustExample.java`
- **FTC official** — [ftc-docs.firstinspires.org](https://ftc-docs.firstinspires.org/), [FTC Javadoc](https://javadoc.io/doc/org.firstinspires.ftc)
