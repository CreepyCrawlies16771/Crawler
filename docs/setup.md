---
title: Setup
description: Create MyRobot.java with the builder, pick a localizer, and fix motor directions
---

# Setup

*Telling Crawler what hardware your robot has*

Every Crawler project needs **one robot file** — `MyRobot.java`:

1. **`MyRobot`** — extends `CrawlerRobot`, with device names, localizer, and tuned numbers all in a single builder chain, plus your mechanisms (claw, lift, …)
2. **OpModes** — the autonomous and TeleOp that use `MyRobot`

There is no separate `RobotHardware.java` and no separate config file. Everything lives in `MyRobot.java`.

## MyRobot.java

`MyRobot` extends `CrawlerRobot` and passes a **builder** to `super()`. The builder is staged: motors → localizer → tuning values. <u>**You must keep this order!**</u>

> 💡 **`MyRobot` is just an example name.** Name your robot class anything you like — it only needs to `extends CrawlerRobot`. The tuner (via `TuningRobotFactory`) and `ROMovementEngine.buildRobot()` accept any `CrawlerRobot` subclass.

```java
package org.firstinspires.ftc.teamcode.TeamscodeNotLibrary;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

public class MyRobot extends CrawlerRobot {

    // ===== REQUIRED: change these to match your Driver Hub configuration names =====
    public static final String FRONT_LEFT  = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT   = "bl";
    public static final String BACK_RIGHT  = "br";
    public static final String IMU         = "imu";

    // ===== REQUIRED only if using 3 dead wheels (see localizer step in docs) =====
    // These are MOTOR PORT names from your Driver Station config, not separate
    // encoder devices — dead wheels read through whichever motor port they're
    // plugged into on the REV Hub. Name the (possibly unused) motor port in the
    // config app, and use that exact name here.
    public static final String ENC_LEFT    = "enc_l";
    public static final String ENC_RIGHT   = "enc_r";
    public static final String ENC_CENTER  = "enc_c";

    // ===== Your robot's own subsystems — rename/add as needed for your season =====
    public static final String CLAW_SERVO = "claw";
    public static final String LIFT_MOTOR = "lift";

    // Match the physical mounting of your REV Hub
    public static final RevHubOrientationOnRobot.LogoFacingDirection IMU_LOGO =
            RevHubOrientationOnRobot.LogoFacingDirection.UP;
    public static final RevHubOrientationOnRobot.UsbFacingDirection IMU_USB =
            RevHubOrientationOnRobot.UsbFacingDirection.FORWARD;

    public final Servo clawServo;
    public final DcMotor liftMotor;

    public MyRobot(HardwareMap hwMap) {
        super(builder(hwMap));
        this.clawServo = hwMap.get(Servo.class, CLAW_SERVO);
        this.liftMotor = hwMap.get(DcMotor.class, LIFT_MOTOR);
    }

    /** The full builder chain — names, localizer, and tuned values in one place. */
    public static CrawlerRobot.Builder builder(HardwareMap hwMap) {
        return new CrawlerRobot.Builder(hwMap)
                .frontLeft(FRONT_LEFT)
                .frontRight(FRONT_RIGHT)
                .backLeft(BACK_LEFT)
                .backRight(BACK_RIGHT)
                .imu(IMU)
                .imuOrientation(IMU_LOGO, IMU_USB)
                .motors()
                // ---- Localizer (pick one) ----
                .withThreeDeadWheels(ENC_LEFT, ENC_RIGHT, ENC_CENTER)
                // ---- Tuned values — paste the Crawler Tuner output here ----
                .setTrackWidth(13.0)                    // inches, left↔right odometry wheels
                .setCenterWheelOffset(3.5)               // inches, forward of center
                .wheelDiameter(1.37795)                 // inches (35 mm GoBILDA pod)
                .ticksPerRev(2000)
                .drivePid(0.05, 0.0, 0.0)               // per meter, drive PID
                .strafePid(0.05, 0.0, 0.0)              // per meter, strafe PID
                .steerPid(0.03, 0.0, 0.0)               // per degree, heading hold / turn
                .minPower(0.15)                         // friction deadband
                .pathDefaults(0.7, 0.4, 25.4)           // move, turn, follow distance (cm)
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .timeoutSecs(5.0)
                .maxDriveSpeed(1.0);
    }

    /* ADD YOUR OWN ROBOT CODE HERE OR YOUR SEASON-SPECIFIC ROBOT ACTIONS! */
    public void openClaw()  { clawServo.setPosition(0.8); }
    public void closeClaw() { clawServo.setPosition(0.2); }

    public void scoreHighBasket() {
        liftMotor.setTargetPosition(800);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.8);
    }
}
```

**Key points:**

- The builder is the **only** place your robot's values live — names *and* numbers
- The **Crawler Tuner** rebuilds `MyRobot.builder()` with live values (via `MyRobot.buildTuned`), so the tuning robot always matches your robot
- `.motors()` enforces that all four names are set
- Every later call is optional — the rest come from `CrawlerRobot.Config` defaults
- `super(builder(hwMap))` takes the builder directly — no `.build()` needed in a subclass

## Choosing a localizer

<div class="diagram" role="img" aria-label="Top-down robot showing track width and center wheel offset">
<svg viewBox="0 0 640 360" xmlns="http://www.w3.org/2000/svg" font-family="'JetBrains Mono', monospace">
  <defs>
    <linearGradient id="ch" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="#1f2937"/><stop offset="1" stop-color="#0f1720"/>
    </linearGradient>
  </defs>
  <rect x="120" y="60" width="400" height="240" rx="18" fill="url(#ch)" stroke="#4ADE80" stroke-width="2"/>
  <g fill="#4ADE80">
    <circle cx="150" cy="90"  r="26"/><circle cx="490" cy="90"  r="26"/>
    <circle cx="150" cy="270" r="26"/><circle cx="490" cy="270" r="26"/>
  </g>
  <g fill="#0f1720" font-size="11" text-anchor="middle">
    <text x="150" y="86">FL</text><text x="490" y="86">FR</text>
    <text x="150" y="266">BL</text><text x="490" y="266">BR</text>
  </g>
  <g fill="#22C55E">
    <circle cx="150" cy="180" r="12"/><circle cx="490" cy="180" r="12"/>
    <circle cx="320" cy="60"  r="12"/>
  </g>
  <g stroke="#9CA3AF" stroke-width="1.5" stroke-dasharray="5 4" fill="none">
    <line x1="320" y1="180" x2="320" y2="60"/>
    <line x1="150" y1="180" x2="490" y2="180"/>
  </g>
  <g fill="#4ADE80" font-size="13" text-anchor="middle">
    <text x="320" y="120">center offset</text>
    <text x="320" y="212">track width</text>
  </g>
  <g fill="#E5E7EB" font-size="12">
    <text x="20" y="30">L/R = parallel odometry wheels (left + right)</text>
    <text x="20" y="50">C = perpendicular center wheel (forward of center)</text>
    <text x="20" y="330">Track width: inches between the L/R wheels</text>
    <text x="20" y="350">Center offset: inches from robot center to the C wheel</text>
  </g>
</svg>
</div>

> ⚠️ **Dead wheel encoder names are motor port names, not separate devices.**
> On the REV Hub, encoder inputs share the same physical port as motor outputs.
> There's no separate "encoder" entry in the config app — a dead wheel plugged
> into a motor port reads through whatever **motor name** is assigned to that
> port, even if no motor is actually connected there (or if one is, and it's
> also driving power).
>
> So `ENC_LEFT`, `ENC_RIGHT`, `ENC_CENTER` must exactly match the **motor
> names** you gave those ports in the Driver Station config app — not a
> separate "encoder" label. If you have a spare unused motor port, name it
> something like `enc_l` in the config app itself, and use that same name here.

| Localizer | Builder call | Accuracy | Hardware | Best for |
|---|---|---|---|---|
| **Motor encoders** | `.withMotorEncoders()` | ±2–4 cm | Built into your motors | Learning, quick tests |
| **Two dead wheels** | `.withTwoDeadWheels("enc_l", "enc_c")` | ±2–3 cm | Two shaft encoders | Simple robots |
| **Three dead wheels** | `.withThreeDeadWheels("l", "r", "c")` | ±1 cm | Three shaft encoders | Most competitive robots |
| **GoBILDA Pinpoint** | `.withPinpoint("odo")` | ±0.5 cm | GoBILDA Pinpoint v1/v2 | Precision, less wiring |

**Pinpoint example** (offsets are the distance from the Pinpoint to the robot center, in your chosen unit):

```java
.motors()
.withPinpoint("odo")
.setConfig(3.0, 2.5, DistanceUnit.CM,
        GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD,
        GoBildaPinpointDriver.EncoderDirection.REVERSED,
        GoBildaPinpointDriver.EncoderDirection.FORWARD)
.wheelDiameter(1.37795)
```

> 💡 **Not sure?** Start with motor encoders (zero extra hardware), then upgrade to dead wheels later — the rest of your code doesn't change.

## Fixing motor directions

If your robot drives backwards or spins when you test it, invert the offending motors in the builder:

```java
.frontLeft("fl").invertFrontLeft()
.frontRight("fr")
.backLeft("bl")
.backRight("br").invertBackRight()
```

Dead-wheel encoders that read backwards get inverted on the localizer stage:

```java
.withThreeDeadWheels("enc_l", "enc_r", "enc_c")
.setTrackWidth(13.0)
.invertLeftEncoder()
.setCenterWheelOffset(3.5)
```

> 💡 Don't guess — the tuner's **Motors** step (Step 1) tells you exactly which motors need inverting.

## Units cheat sheet

| What | Unit | Convert with |
|---|---|---|
| Track width, center offset, wheel diameter | **inches** | `UnitConverter.inToCm(...)` |
| Waypoint coordinates, follow distance, arrival/orbit thresholds | **centimeters** | `UnitConverter.cmToIn(...)` |
| `drivePID` / `strafePID` distances | **meters** | `UnitConverter.mToCm(...)` |
| Speeds (move, turn) | 0.0 – 1.0 power | — |
| PID gains | per **meter** (drive/strafe), per **degree** (steer) | — |

Crawler's field geometry is always **cm**, but odometry hardware sizes are **inches** in the builder. The [UnitConverter](api-reference.md#unitconverter) utility bridges the two:

```java
import org.firstinspires.ftc.teamcode.Crawler.core.utils.UnitConverter;

.setTrackWidth(UnitConverter.inToCm(13.0))   // 13 in → 33.02 cm
```

---

## Next Steps

**[Your First Autonomous →](first-auto.md)** Write a three-waypoint path
