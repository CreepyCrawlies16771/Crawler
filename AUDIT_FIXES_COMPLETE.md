# Crawler — Docs Audit & Fixes

**Date:** 2026-08-06 · **Scope:** every code example and API claim in Crawler's docs,
verified line-by-line against the library source and its test suite.

## How the audit worked

1. **Ground truth.** The public API was mapped directly from
   `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Crawler/**` — classes,
   methods, parameter types/order, builder stages, `Config` defaults — plus the JVM
   unit tests under `TeamCode/src/test/...` (highest-confidence usage) and the
   running examples in `TeamscodeNotLibrary/`.
2. **Diff.** Every code block in the user-facing docs (`docs/*.md`), the in-repo
   Crawler docs, and the root-level project docs was checked against that inventory.
3. **Rewrite.** Flagged examples were replaced with real API usage; prose claims were
   corrected; nothing was "simplified" to look nicer.

The full verified API inventory now lives in `BUILD_SPECIFICATION.md`; architecture
and verified examples live in `ARCHITECTURE.md`.

---

## Findings: inaccurate examples

| # | Location | What was wrong |
|---|---|---|
| 1 | `docs/pure-pursuit.md` · "The Waypoint API" | `.heading(0.5)` documented as "optional target heading" and `.slowDown(0.5, 0.5)` as "fade turning… by this factor". **Neither is read by the follower** — `FOFollower.followToWaypoint()` passes `movement.getWorldHeading()` as the preferred angle and never touches `waypoint.heading` / `slowDown*`. Removed from the API block; added an honest note. |
| 2 | `docs/troubleshooting.md` · "Robot never starts moving" | "Min power test (Step 5, `X` to cycle)". **Triangle** cycles tests within Step 5; X goes back a step. Fixed. |
| 3 | `docs/ftc-dashboard.md` · "drawRobot in your own OpMode" | Missing `import com.acmerobotics.dashboard.FtcDashboard;` — snippet would not compile. Added. |
| 4 | `docs/ftc-dashboard.md` · field-view colors | Claimed "Green path / robot square". Reality: `RobotMovement` draws the path polyline in **blue** and the follow point (as a robot square) in **red**; the tuner draws the real robot in **green**. Fixed. |
| 5 | `docs/first-auto.md` · waypoint note | Claimed `Waypoint.at(x, y, robot.config)` is "required". The overload `Waypoint.at(x, y)` exists (uses default `Config`). Reworded to "use your robot's config… a bare overload exists but falls back to library defaults". |
| 6 | `FOFollower.java` javadoc ("Usage example") | `Waypoint.at(0, 0).at(24, 0).speed(0.8)…buildAll()` — `.at()` chaining and `.buildAll()` **do not exist**. Replaced with real `Waypoint.at(x, y, robot.config)…build()` usage. |
| 7 | `Crawler/RobotOrient/Documentaiton/` (9 files) | Wholesale stale: documents an `AutoEninge` package that doesn't exist, a `MovementEngine` with `arc()`, `moveToShoot(Team.BLUE)`, shooter/ball mechanisms, `robot.driveRobotRelative(...)`, `drawCircle(...)`, a 12-step tuner. **Deleted.** |
| 8 | `Crawler/TUNING_OPMODE_IMPLEMENTATION.md` | Documents the **deleted** `RobotConfig` class (`RobotConfig.Odometry.TRACK_WIDTH`, `RobotConfig.RobotOriented.Kp`, …), `robot.driveRobotRelative(...)` (never existed), field access `robot.leftEncoder`, `Waypoint.at(0,0)` no-config usage, dashboard `0.4.7`, `/sdcard/Crawler` JSON persistence. **Deleted.** |
| 9 | `ARCHITECTURE.md` | Described `RobotConfig`, `CrawlerAuto<R>`/`CrawlerTeleOp<R>` (never existed), `new Waypoint(0,0,0)` (no public constructor — private, builder-only), a `Pose` class (doesn't exist), `drawCircle` (doesn't exist), a 12-step tuner, `dashboard.sendTelemetry(...)`. **Rewritten** against the current API. |
| 10 | `BUILD_SPECIFICATION.md` | Spec'd `CrawlerAuto<R>`, `CrawlerTeleOp<R>`, `CrawlerStateMachine`, `arc(distanceInches, HeadingTimeline)`, `driveRobotRelative(Gamepad)`, `RobotConfig`. **Rewritten** as the verified API inventory. |
| 11 | `AUDIT_FIXES_COMPLETE.md` | Prior record referenced `RobotConfig`, `buildAll()`, `PleaseWorkd.java`, `Robot.java` era code. **Rewritten** as this audit's record. |

**Most common failure mode:** *never-tested, plausible-looking examples* — code that
followed the "right idea" (build a waypoint, follow a path) with wrong details
(`buildAll()` chains, `.heading()`/`.slowDown()` behavior claims, wrong button
mappings). Second most common: **docs describing functionality removed from the
library** (`RobotConfig`, `arc()`, `driveRobotRelative`, the old 12-step tuner).

## Functionality that no longer exists (removed, not corrected)

- `RobotConfig` global-statics config class (and its `@Config` inner classes)
- `CrawlerAuto<R>` / `CrawlerTeleOp<R>` base classes, `CrawlerStateMachine`
- `Waypoint` chained `.at().at()…buildAll()` API; `Waypoint` public constructor
- `robot.driveRobotRelative(...)`, `arc(...)`, `HeadingTimeline`-driven `arc()`
- `Pose`, `drawCircle(...)`, the "12-step" `CrawlerTuner`, `/sdcard/Crawler` persistence
- `Robot.java`, `PleaseWorkd.java`

## Still-present but unwired / deprecated (flagged, not guessed)

- `Waypoint.heading` / `Waypoint.slowDown(...)` — stored, never consumed by `FOFollower`
- `RobotOrient/HeadingTimeline.java` / `AnimationBuilder.java` / `IndexerRotation.java`
  — real code + tests, but no engine uses them
- `core/Robot/driveTrain.java` — unused; casts `MotorEx` → `DcMotor`
- `RobotOrient/Tuner.java` — `@Deprecated`, throws
- `RobotMovement.follow(List, double)` — builds a Dashboard `TelemetryPacket`
  (path + follow point) but never sends it, so the field view stays blank during
  paths; only the tuner's tests (`TuningDashboard.drawRobot`) actually draw live

## Real functionality with no doc coverage

- **`AprilTagWebcam` / `Vision.Rotation`** — working vision wrapper, undocumented in
  `docs/` (its exact API is now recorded in `BUILD_SPECIFICATION.md` §7)
- **`RobotMovement` low-level API** — `getFollowPointPath(...)`, path extension,
  dynamic look-ahead; only the follower is documented user-facing
- **`DashboardFieldViewUtils.drawPoint`** and **`FieldColor`** details
- **`annotations.Experimental`** marker + processor
- **`HeadingTimeline` keyframe interpolation** (tested but not wired anywhere)
- **`RobotMovement.follow(List, double)` field-view drawing** — the drawing code
  exists but the packet is never sent

## Fixes applied in this audit

- `docs/pure-pursuit.md`, `docs/troubleshooting.md`, `docs/ftc-dashboard.md`,
  `docs/first-auto.md` — corrected examples/prose
- `Crawler/FieldOrient/FOFollower.java` — corrected javadoc usage example
- Deleted `Crawler/RobotOrient/Documentaiton/` and `Crawler/TUNING_OPMODE_IMPLEMENTATION.md`
- Rewrote `ARCHITECTURE.md`, `BUILD_SPECIFICATION.md`, `AUDIT_FIXES_COMPLETE.md`

## What survived the audit unmodified

The `docs/` site was otherwise accurate: the `CrawlerRobot.Builder` chain (all builder
methods and their `Config` defaults), the `FOFollower`/`Waypoint` examples,
`RobotOrientedDrive`/`ROMovementEngine` patterns, the tuner's 7 steps and gamepad map,
the Dashboard config panel, the installation Gradle deps, and the `npm` scripts all
verified against source.

---

# Round 2 — Config-in-the-builder rework

A follow-up review of the user-facing pattern found that the docs presented the tuned
config as a separate `tunedConfig()` function returning a `Config`, while the real
library carries all values **in the builder chain**. The shipped example and docs were
reworked so the config lives where the library actually keeps it.

## Changes

| Area | Change |
|---|---|
| `MyRobot.java` | Rewritten as an **all-in-one** robot: device-name constants, IMU orientation, localizer, and every tuned number live in a single `builder(HardwareMap)` chain. `tunedConfig()` removed. Added `buildTuned(hwMap, config)` for the Crawler Tuner to rebuild with live values. |
| `RobotHardware.java` | **Deleted** — its constants were folded into `MyRobot.java`. Nothing else to keep in sync. |
| `MyRobotSnippet.java` | Prints **builder lines** (`.setTrackWidth(13.0)`, `.drivePid(...)`, …) to paste into `MyRobot.builder()`, not `c.field = …` assignments for a `tunedConfig()` function. |
| `CrawlerTuner.java` | Factory is now `config -> MyRobot.buildTuned(hardwareMap, config)`. Javadoc + telemetry text updated. |
| `CrawlerError.java` | CRWL-104 and CRWL-107 fixes now point at `MyRobot.java` / `MyRobot.builder()`. |
| `CrawlerSystemTest.java` | CONFIG_REVIEW help text updated. |
| `CrawlerErrorsTest.java` | Frame example updated to `builder`. |
| Docs site | `setup.md`, `example.md`, `configuration.md`, `index.md`, `tuning.md`, `tuning-guide.md`, `tuning-overview.md`, `ftc-dashboard.md`, `troubleshooting.md`, `installation.md`, `USER_GUIDE.md`, `errors.md` all updated to the one-file `MyRobot.builder()` pattern with the builder-line snippet. |
| `docs/api-reference.md` | **New** — full public-API reference covering every feature (builder stages, all `Config` fields, PID loop internals, `DebugSink`, localizers, followers, tuner, Dashboard, Vision, utils, errors). Wired into the site nav. |
| `UnitConverter` | **New** `core/utils` class (in↔cm↔m↔mm↔ft) + `UnitConverterTest` — the library's field geometry is cm while builder odometry sizes are inches. |
| Run scripts | `start-docs.sh` (Linux) verified; `start-docs.bat` (Windows) **added** for the docs dev server. |
| Root docs | `ARCHITECTURE.md`, `BUILD_SPECIFICATION.md`, `AUDIT_FIXES_COMPLETE.md` updated to the new pattern. |

## Why

- The docs' `MyRobot` used a separate `tunedConfig()` function that the current library
doesn't use — the config is part of the builder, so the example code was wrong.
- The tuner printed `c.field = …` assignments; it now prints the builder lines the
team actually pastes.
- `RobotHardware.java` was redundant with `MyRobot` and was removed so there is exactly
one place to edit.
- The PID loops were under-documented; `api-reference.md` now explains `drivePID` /
`strafePID` / `turnPID` with their real gain semantics, deadband, and `DebugSink`.

## Verification

- Library JVM tests: `./gradlew :TeamCode:testDebugUnitTest` (includes new
`UnitConverterTest`)
- Docs site rebuild: `npm run build`
- Dev server: `./start-docs.sh` (Linux) / `start-docs.bat` (Windows)

## Verification

- Library JVM tests: `./gradlew :TeamCode:testDebugUnitTest`
- Docs site rebuild: `npm run build` (regenerates `docs-html/`)
