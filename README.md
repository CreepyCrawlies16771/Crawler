# Crawler Starter

*A clean, ready-to-build FTC project with the [Crawler](https://github.com/CreepyCrawlies16771/Crawler) pathing library installed.*

Clone this branch to start a new season: the FTC SDK, Gradle build, and Crawler are already set up, so you only write your robot. No copying packages, no hunting for dependencies.

## What's inside

- **FTC SDK 11.1** — `FtcRobotController` + `TeamCode` modules, ready to build and deploy.
- **Crawler library** — the full `Crawler` package (pure pursuit, PID moves, localizers, guided tuner).
- **Starter robot** — `Teamcode/Examples/MyRobot.java` with placeholder device names and the one-stop builder chain.
- **Example OpModes** — `ExampleAuto`, `ExampleTeleOp`, `ManualAdjustExample`.
- **Crawler tooling** — `CrawlerTuner`, `CrawlerSmokeTest`, `CrawlerSystemTest`.

## Get started

1. Clone and open in Android Studio (Ladybug or later), then let Gradle sync:

   ```bash
   git clone -b starter https://github.com/CreepyCrawlies16771/Crawler.git
   ```

2. Configure your robot — edit device names and localizer in `Teamcode/Examples/MyRobot.java` ([Setup](https://creepycrawlies16771.github.io/Crawler/setup.html)).
3. Run the **Crawler Tuner** once to calibrate odometry and PID ([Tuning Guide](https://creepycrawlies16771.github.io/Crawler/tuning-guide.html)).
4. Copy an example OpMode and drive your first path ([Your First Autonomous](https://creepycrawlies16771.github.io/Crawler/first-auto.html)).

Full docs: [creepycrawlies16771.github.io/Crawler](https://creepycrawlies16771.github.io/Crawler/) and [`Teamcode/SETUP.md`](TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Teamcode/SETUP.md).

## Project layout

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/     ← the library (read, don't break)
└── Teamcode/    ← your code (edit freely)
    ├── Examples/        MyRobot.java, ExampleAuto.java, ExampleTeleOp.java, ManualAdjustExample.java
    └── CrawlerOpModes/  CrawlerTuner.java, CrawlerSmokeTest.java, CrawlerSystemTest.java
```

## Updating the library

This starter is a snapshot of the library. To pull the latest Crawler changes, copy the `Crawler` package from the [main branch](https://github.com/CreepyCrawlies16771/Crawler/tree/main/TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler) into your project.

## Requirements

- Android Studio Ladybug (2024.2) or later
- FTC SDK 11.1 (DECODE 2025–2026 season)

## License

BSD 3-Clause — see [LICENSE](LICENSE). Built on the FTC SDK with FTCLib and FTC Dashboard.

---

Developed by **Creepy Crawlies · Team 16771**.
