# Crawler library (do not edit for normal team setup)

Teams configure robots only through `TeamscodeNotLibrary/MyRobot.java` and `RobotHardware.java`.

## Design rules

- All tunable values live on `CrawlerRobot.Config`, set via `CrawlerRobot.Builder`.
- No `RobotConfig` global statics — each robot instance carries its own config.
- Field poses use **centimeters**; odometry hardware sizes use **inches** in the builder.
- `ROMovementEngine` + `RobotOrientedDrive` use your `MyRobot` config for short PID moves.

## Team workflow

See `TeamscodeNotLibrary/SETUP.md`.
