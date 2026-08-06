# Crawler Tuner — In-Repo Guide

This guide covers the tuning system in this package (`TuningSession`, `TuningConfig`, …)
and its OpMode, `TeamscodeNotLibrary/CrawlerTuner.java`. For the full user-facing
walkthrough, see `docs/tuning.md` and `docs/tuning-guide.md` (or the built site in `docs-html/`).

## What the tuner does

The tuner calibrates the values that live in `CrawlerRobot.Config` (set via the builder
chain in `MyRobot.java`) by running real tests on your robot:

1. **Motors** — verify each wheel direction
2. **Encoders** — wheel diameter + ticks/rev
3. **Track width** — odometry rotation vs IMU (10-turn spin)
4. **Center offset** — heading drift while strafing 1 m
5. **PID** — drive / strafe / turn (P, I, D) + automatic min-power deadband search. Tests run through the real `RobotOrientedDrive` engine (`drivePID` / `strafePID` / `turnPID`) so tuned gains match match behavior.
6. **Auto path** — 1 m square with pure pursuit
7. **Finish** — prints the tuned builder lines for `MyRobot.builder()`

## Live values: `TuningConfig`

All tunable values are `public static` fields in `TuningConfig`, annotated with
`@Config("Crawler Tuner")`. That means:

- **FTC Dashboard** (`http://<robot-ip>:8080/dash`) shows a `Crawler Tuner` panel where
  you can type values directly — they apply on the next loop.
- Gamepad adjustments write to the same fields, so both inputs stay in sync.
- Values persist between OpMode runs (static fields), but **not** across app restarts —
  the final values must be pasted into `MyRobot.builder()`.

`TuningSession` calls `TuningConfig.toConfig()` every loop and rebuilds the robot only
when a value actually changed.

Tuner output appears in the FTC Dashboard:

- **Telemetry panel** — every test streams error, power, and live P/I/D terms
  (via `TuningTelemetry`, a `MultipleTelemetry` wrapping Driver Station + Dashboard).
- **Field view** — `TuningDashboard.drawRobot(...)` draws the robot's odometry pose
  live during the spin, strafe, PID, and min-power tests.

## Gamepad controls

| Button | Action |
|---|---|
| RB | Run the current test |
| D-pad ↑ / ↓ | Increase / decrease the current value |
| D-pad ← / → | PID step: pick the term (P → I → D) |
| Triangle | PID step: next test (Drive → Strafe → Turn → Min power) |
| X | Back a step |
| Circle | Accept step, move on |
| Square | Toggle the `MyRobot.builder()` snippet |

## Setup requirements

- The `CrawlerTuner` OpMode lives in `TeamscodeNotLibrary/` and supplies the tuning robot
  through a `TuningRobotFactory` — in the sample, `config -> MyRobot.buildTuned(hwMap, config)`,
  built from the `MyRobot` device-name constants. There is no separate config file to keep in sync.
- `TuningSession` rebuilds the robot whenever a tuning value changes, calling the factory
  with the live values from `TuningConfig`.
- If your robot uses a different localizer or extra builder stages, update
  `MyRobot.builder(...)` — the tuner follows automatically.

## After tuning

1. Press **Square** (or finish Step 7) and copy the printed builder lines.
2. Paste them into the tuned section of `MyRobot.builder()` in `MyRobot.java`.
3. Rebuild, deploy, and run `CrawlerSmokeTest` to confirm odometry is reporting movement.

> ⚠️ Tuning values are **not permanent** until pasted into `MyRobot.builder()`.
