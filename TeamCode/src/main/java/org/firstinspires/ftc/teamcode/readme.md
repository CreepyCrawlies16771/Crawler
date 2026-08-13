# TeamCode Module

This module contains the **Crawler** library and the example/team code that ships with it.

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── Crawler/     ← the library (read, don't break)
└── Teamcode/    ← your code (edit freely)
    ├── Examples/        MyRobot.java, ExampleAuto.java, ExampleTeleOp.java, ManualAdjustExample.java
    └── CrawlerOpModes/  CrawlerTuner.java, CrawlerSmokeTest.java, CrawlerSystemTest.java
```

## For teams using the library

Only edit files under `Teamcode/` — the `Crawler/` package is the library.

- Start with `Teamcode/SETUP.md` (30-minute setup).
- All robot configuration (device names, localizer, tuned values) lives in one
  builder chain in `Teamcode/Examples/MyRobot.java`.
- Full user documentation is in the repo's `docs/` folder; build it with `npm run build`.
