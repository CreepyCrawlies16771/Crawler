# Crawler — 30 minute robot setup

Teams only edit files in this folder (`TeamscodeNotLibrary`). Do not modify the `Crawler` library package.

## 1. Robot Configuration (5 min)

1. Open the FTC Robot Configuration.
2. Name devices to match `RobotHardware.java` (or change the strings there to match your config).
3. Required: four drive motors, IMU, three odometry pods (`enc_l`, `enc_r`, `enc_c`).

## 2. Crawler Tuner (15–20 min)

1. Sync motor names in `Crawler/Tuning/TuningRobotConfig.java` with `RobotHardware.java` (same strings).
2. On the Driver Station, run **Crawler Tuner**.
3. Complete steps 1–7 (Circle = next, Square = copy code).
4. Paste the printed builder lines into `MyRobot.java`.
5. Stop the OpMode and run again after any odometry change.

## 3. Automated smoke test (2 min)

Run **Crawler Smoke Test** (Autonomous). It must show `RESULT: PASS`.

## 4. System test (10 min)

Run **Crawler System Test** (TeleOp). Complete all six steps.

## 5. Competition autos

Copy `ExampleAuto.java` for field paths (`FOFollower` + `Waypoint.at(..., robot.config)`).

For **short robot-relative moves** (nudge forward, align heading), extend `ROMovementEngine` — see `ManualAdjustExample.java`:

```java
@Override protected CrawlerRobot buildRobot(HardwareMap hw) { return new MyRobot(hw); }

drivePID(0.30, 0);   // 30 cm forward, hold 0°
turnPID(45);         // turn to 45°
strafePID(0.20, 45); // 20 cm right, hold 45°
```

PID values come from your `MyRobot` builder (`drivePid`, `strafePid`, `steerPid`).

## Checklist

- [ ] `RobotHardware` matches DS config
- [ ] `MyRobot` builder has tuned values from Crawler Tuner
- [ ] Crawler Smoke Test: PASS
- [ ] Crawler System Test: all steps pass
- [ ] Example Auto runs on the field

## Troubleshooting

| Problem | Fix |
|--------|-----|
| OpMode crashes on init | Wrong device name in `RobotHardware` |
| Odometry barely changes | `ticksPerRev` / `wheelDiameter` in `MyRobot` |
| Strafe spins | `setCenterWheelOffset` |
| Turn drift | `setTrackWidth` |
| Path overshoots | Lower `pathDefaults` move speed or `arrivalThresholdCm` |
