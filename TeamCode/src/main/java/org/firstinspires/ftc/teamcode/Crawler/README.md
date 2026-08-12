# Crawler library (do not edit for normal team setup)

Teams configure robots only through `Teamcode/Examples/MyRobot.java` (device names, localizer, and tuned values all live in one builder chain).

## Design rules

- All tunable values live on `CrawlerRobot.Config`, set via `CrawlerRobot.Builder`.
- No `RobotConfig` global statics — each robot instance carries its own config.
- Field poses use **centimeters**; odometry hardware sizes use **inches** in the builder.
- `ROMovementEngine` + `RobotOrientedDrive` use your `MyRobot` config for short PID moves.

## Team workflow

See `Teamcode/SETUP.md`.
