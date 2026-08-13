# Crawler Tuner — In-Repo Guide

This guide covers the tuning system in this package (`TuningSession`, `TuningConfig`, …)
and its OpMode, `Teamcode/CrawlerOpModes/CrawlerTuner.java`. For the full user-facing
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
- There are **no presets**: `TuningSession` seeds the fields from your robot's builder
  each time the tuner starts (`TuningConfig.seed(...)`), so tuning always begins from
  the values already in your robot class. Within a run the values stay in the static
  fields while you edit them, and they persist between OpMode runs, but each tuner
  start re-seeds from the builder — the final values must be pasted into your robot's
  `builder()`.

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

- The `CrawlerTuner` OpMode lives in `Teamcode/CrawlerOpModes/` and supplies the tuning
  robot through a `TuningRobotFactory` backed by `CrawlerRobotRegistry` — it builds
  whatever robot the team registered (see `docs/setup.md`), so there is no hard-coded
  robot class and no separate config file to keep in sync.
- `TuningSession` rebuilds the robot whenever a tuning value changes, calling the factory
  with the live values from `TuningConfig`.
- If your robot uses a different localizer or extra builder stages, update your robot's
  `builder(...)` — the tuner follows automatically.

## After tuning

1. Press **Square** (or finish Step 7) and copy the printed builder lines.
2. Paste them into the tuned section of your robot's `builder()`.
3. Rebuild, deploy, and run `CrawlerSmokeTest` to confirm odometry is reporting movement.

> ⚠️ Tuning values are **not permanent** until pasted into your robot's `builder()`.
