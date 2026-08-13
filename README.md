# Crawler

*A friendly FTC pathing library, that aims to make programming enjoyable for the average user.*

Crawler is a lightweight, readable FTC library for autonomous movement. You describe **where** you want the robot to go and it figures out **how** to get there — smooth pure-pursuit paths, precise robot-relative moves, and a guided tuner that calibrates odometry and PID to *your* specific robot.

Road Runner and Pedro Pathing are powerful, but they take days to learn. Crawler gets a rookie team driving a real path in an afternoon — and the source is short enough that you can actually understand what it does.

## Features

- **Pure pursuit paths** — `FOFollower` + `Waypoint` drive smooth, curved paths between field coordinates.
- **Robot-relative moves** — `drivePID`, `strafePID`, `turnPID` for precise final alignments.
- **Guided tuner** — one OpMode walks you through odometry, PID, and path tuning, with live editing in **FTC Dashboard**.
- **Any localizer** — three dead wheels, two dead wheels, GoBILDA Pinpoint, or motor encoders.
- **Source-included** — copy the `Crawler` package into your `TeamCode` module; there is no artifact to fetch.

## Quick start

1. Clone this repo — or copy `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/` into your own FTC project.
2. Add the FTCLib and FTC Dashboard dependencies ([Installation](https://creepycrawlies16771.github.io/Crawler/installation.html)).
3. Create `MyRobot.java` — device names, localizer, and tuned values all live in one builder chain ([Setup](https://creepycrawlies16771.github.io/Crawler/setup.html)).
4. Run the **Crawler Tuner** once to calibrate odometry and PID to your robot ([Tuning Guide](https://creepycrawlies16771.github.io/Crawler/tuning-guide.html)).
5. Copy an example from `Teamcode/Examples/` and drive your first path ([Your First Autonomous](https://creepycrawlies16771.github.io/Crawler/first-auto.html)).

## Documentation

Full user documentation is published to GitHub Pages on every push to the `site` branch:

- [Installation](https://creepycrawlies16771.github.io/Crawler/installation.html) · [Setup](https://creepycrawlies16771.github.io/Crawler/setup.html)
- [Your First Autonomous](https://creepycrawlies16771.github.io/Crawler/first-auto.html) · [Your First TeleOp](https://creepycrawlies16771.github.io/Crawler/first-teleop.html)
- [Full Example](https://creepycrawlies16771.github.io/Crawler/example.html) · [API Reference](https://creepycrawlies16771.github.io/Crawler/api-reference.html)
- [Troubleshooting](https://creepycrawlies16771.github.io/Crawler/troubleshooting.html) · [Errors](https://creepycrawlies16771.github.io/Crawler/errors.html)

The `docs/` sources and developer docs ([ARCHITECTURE.md](https://github.com/CreepyCrawlies16771/Crawler/blob/site/ARCHITECTURE.md), [BUILD_SPECIFICATION.md](https://github.com/CreepyCrawlies16771/Crawler/blob/site/BUILD_SPECIFICATION.md)) live on the [`site`](https://github.com/CreepyCrawlies16771/Crawler/tree/site) branch.

## Repository layout

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/     ← the library (read, don't break)
└── Teamcode/    ← your code (edit freely)
```

## Requirements

- Android Studio Ladybug (2024.2) or later
- FTC SDK 11.1 (DECODE 2025–2026 season)

## License

BSD 3-Clause — see [LICENSE](LICENSE). Built on the FTC SDK with FTCLib and FTC Dashboard.

---

Developed by **Creepy Crawlies · Team 16771**.
