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

> 💡 **`MyRobot` is just an example name.** Name your robot class anything you like — it only needs to `extends CrawlerRobot`. The tuner, System Test and Smoke Test build whatever robot you register with `CrawlerRobotRegistry` (below), and `ROMovementEngine.buildRobot()` accepts any `CrawlerRobot` subclass.

```java
package org.firstinspires.ftc.teamcode.Teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobotRegistry;

public class MyRobot extends CrawlerRobot {

    // Registers your robot class so Crawler's tools (Tuner, System Test, Smoke Test) can
    // build it without hard-coding this class's name — see "Register your robot" below.
    static {
        CrawlerRobotRegistry.setProvider(
                MyRobot::new,
                (hwMap, config) -> builder(hwMap).withConfig(config).build()
        );
    }

    // ===== REQUIRED: change these to match your Driver Hub configuration names =====
    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";
    public static final String IMU = "imu";

    // ===== REQUIRED only if you use dead wheels (see the hardware picker below) =====
    // These are MOTOR PORT names from your Driver Station config, not separate
    // encoder devices — dead wheels read through whichever motor port they're
    // plugged into on the REV Hub. Name the (possibly unused) motor port in the
    // config app, and use that exact name here.
    public static final String ENC_LEFT = "enc_l";
    public static final String ENC_RIGHT = "enc_r";
    public static final String ENC_CENTER = "enc_c";

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
                // ---- Localizer (pick one — copy your hardware's lines below) ----
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
                .turnReferenceRadians(Math.toRadians(30))
                .maxDriveSpeed(1.0);
    }

    /* ADD YOUR OWN ROBOT CODE HERE OR YOUR SEASON-SPECIFIC ROBOT ACTIONS! */
    public void openClaw() {
        clawServo.setPosition(0.8);
    }

    public void closeClaw() {
        clawServo.setPosition(0.2);
    }

    public void setLift(int height) {
        liftMotor.setTargetPosition(height * 100);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.5);
    }

    public void stopLift() {
        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMotor.setPower(0);
    }

    public void scoreHighBasket() {
        liftMotor.setTargetPosition(800);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.8);
    }
}
```

## Register your robot with the Crawler tooling

The static block at the top of `MyRobot` (in the example above) is what lets the
Crawler Tuner, System Test and Smoke Test build **your** robot — they never hard-code
the shipped example class. If you write your own robot class from scratch, copy the
block and point it at your own builder:

```java
static {
    CrawlerRobotRegistry.setProvider(
        MyRobot::new,
        (hwMap, config) -> builder(hwMap).withConfig(config).build()
    );
}
```

Run any OpMode that builds your robot once after deploying (your TeleOp does this), and
the tooling will find it for the rest of the session. If you forget, the tooling shows a
clear error telling you exactly what to add.

**Key points:**

- The builder is the **only** place your robot's values live — names *and* numbers
- There are **no library defaults**: every tuned value must be set in the builder chain, or the robot refuses to build
- The **Crawler Tuner** rebuilds your registered robot with live values (seeded from the values already in your builder), so the tuning robot always matches your robot
- `.motors()` enforces that all four names are set
- `super(builder(hwMap))` takes the builder directly — no `.build()` needed in a subclass
- The **localizer line** and the tuning values it needs change with your hardware — pick yours below and copy the exact lines

---

## What odometry hardware do you have?

<div class="hw-picker" data-hw-picker>
  <label class="hw-picker-label" for="hwSelect">hardware</label>
  <select class="hw-select" id="hwSelect" data-hw-select aria-label="What odometry hardware do you have?">
    <option value="all" selected>Show all setups (compare)</option>
    <option value="motor">Motor encoders — built into your drive motors</option>
    <option value="two-dead-wheel">Two dead wheels — 2 shaft encoders</option>
    <option value="three-dead-wheel">Three dead wheels — 3 shaft encoders</option>
    <option value="pinpoint">GoBILDA Pinpoint — I2C module + 2 pods</option>
  </select>
  <p class="hw-hint" data-hw-hint aria-live="polite">Pick your hardware to focus the page on just its setup — every localizer's full details are below.</p>
</div>

<div class="hw-panel" data-hw-panel="all">

### Compare localizers

| Localizer | Builder call | Accuracy | Hardware | Best for |
|---|---|---|---|---|
| **Motor encoders** | `.withMotorEncoders()` | ±2–4 cm | Built into your motors | Learning, quick tests |
| **Two dead wheels** | `.withTwoDeadWheels("enc_l", "enc_c")` | ±2–3 cm | Two shaft encoders | Simple robots |
| **Three dead wheels** | `.withThreeDeadWheels("l", "r", "c")` | ±1 cm | Three shaft encoders | Most competitive robots |
| **GoBILDA Pinpoint** | `.withPinpoint("odo")` | ±0.5 cm | GoBILDA Pinpoint v1/v2 | Precision, less wiring |

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

> 📌 **So `ENC_LEFT`, `ENC_RIGHT`, `ENC_CENTER` must exactly match the motor
> names you gave those ports in the Driver Station config app** — not a
> separate "encoder" label. If you have a spare unused motor port, name it
> something like `enc_l` in the config app itself, and use that same name here.

> 💡 **Not sure?** Start with motor encoders (zero extra hardware), then upgrade to dead wheels later — the rest of your code doesn't change.

</div>

<div class="hw-panel" data-hw-panel="motor">

### 1 · Motor encoders (built-in)

*No extra hardware — uses the encoders already inside your four drive motors.*

#### What you need

Nothing extra. Your four drive motors (`fl`, `fr`, `bl`, `br`) and the IMU are all the odometry this uses — they're already set up in the builder.

#### How it works

Crawler averages the left pair of drive-motor encoders and the right pair, then turns the difference between the two sides into heading using the **track width** (the distance between the left and right drive wheels).

#### Wiring & mounting

None — the encoders are inside the motors. Just make sure Step 1 (motor directions) is correct first.

#### Driver Station config

Nothing extra to add. Your drive motors are already named. You can delete the `ENC_LEFT` / `ENC_RIGHT` / `ENC_CENTER` constants from `MyRobot.java`.

#### MyRobot.java

Swap the localizer line in `builder()`:

```java
.motors()
// ---- Localizer (zero extra hardware) ----
.withMotorEncoders()
// ---- Tuned values ----
.wheelDiameter(3.78)      // your DRIVE wheel diameter in inches (96 mm ≈ 3.78")
.ticksPerRev(560)         // REV HD Hex motor = 560 ticks/rev
```

**Track width:** for motor encoders, that's the distance between the left and right **drive wheels** (not pods). Just run the Crawler Tuner's Step 3 and paste its output — the tuner writes `trackWidth` for you, and rebuilding your robot applies it.

#### Tuning

- **Step 1 (Motors)** — applies, do it first
- **Step 2 (Encoders)** — applies: wheel diameter = your **drive wheel** diameter, ticks/rev = your **motor's** ticks. The tuner shows no live tick counts (motor encoders have no separate encoder objects), but the diameter/ticks values still drive the math
- **Step 3 (Track width)** — applies (left↔right drive wheels)
- **Step 4 (Center offset)** — does **not** apply: there's no center wheel

#### Common issues

- Mecanum wheels slip, so this is the least accurate option (±2–4 cm) — sideways travel is estimated from wheel rotation, not measured
- Field-relative autos can drift sideways over time because strafe isn't measured directly
- Great for learning and quick tests — upgrade to dead wheels later and the rest of your code doesn't change

</div>

<div class="hw-panel" data-hw-panel="two-dead-wheel">

### 2 · Two dead wheels

*Two shaft encoders — one parallel pod, one perpendicular pod.*

#### What you need

- **2 shaft encoders:** REV through-bore (8192 ticks/rev) or GoBILDA 5203 odometry pods (2000 ticks/rev)
- Mounting brackets or 3D prints
- **2 free motor ports** on your REV Hub

#### How it works

The **left pod** is mounted parallel to the drive direction and measures forward travel; the **center pod** is mounted perpendicular and measures sideways travel. As the robot rotates, the two pods spin at different rates — that difference, divided by the distance between them (**track width**), gives heading.

#### Wiring & mounting

1. Mount the left pod parallel to the drive direction on one side of the chassis
2. Mount the center pod perpendicular to the drive direction, offset to the other side of the chassis
3. Plug each encoder into a free motor port on the REV Hub (any port you aren't using for a drive motor)

#### Driver Station config

Name the **two motor ports** — even though no motor is attached to them — exactly as your code expects. Dead wheels read through the motor port they're plugged into:

| Port | Name in config app |
|---|---|
| Left pod | `enc_l` |
| Center pod | `enc_c` |

#### MyRobot.java

Add the encoder constants at the top of `MyRobot.java`:

```java
public static final String ENC_LEFT   = "enc_l";   // parallel pod
public static final String ENC_CENTER = "enc_c";   // perpendicular pod
```

Then swap the localizer block in `builder()`:

```java
.motors()
// ---- Localizer ----
.withTwoDeadWheels(ENC_LEFT, ENC_CENTER)
.setTrackWidth(13.0)              // inches, perpendicular distance between the two pods
// ---- Tuned values ----
.wheelDiameter(1.37795)           // 35 mm GoBILDA pod; REV 25 mm = 0.984"
.ticksPerRev(8192)                // REV through-bore; GoBILDA 5203 = 2000
```

If a pod reads backwards, invert it (after `.withTwoDeadWheels(...)`):

```java
.invertLeftEncoder()              // left pod spins the wrong way
.invertCenterEncoder()            // center pod spins the wrong way
```

#### Tuning

- **Step 2 (Encoders)** — applies: pod diameter + ticks/rev (table above)
- **Step 3 (Track width)** — applies: the perpendicular distance between the two pods' contact lines
- **Step 4 (Center offset)** — does **not** apply (two-wheel robots have no center offset stage)

#### Common issues

- ±2–3 cm — a solid middle ground between built-in encoders and three dead wheels
- Robot "spins" in its pose estimate → track width is wrong (Step 3)
- Robot doesn't strafe correctly → center pod direction or track width (Steps 1–3)

</div>

<div class="hw-panel" data-hw-panel="three-dead-wheel">

### 3 · Three dead wheels

*Three shaft encoders — the competitive standard.*

#### What you need

- **3 shaft encoders:** two parallel pods (left + right) and one perpendicular center pod
- Mounting brackets or 3D prints
- **3 free motor ports** on your REV Hub

#### How it works

The left and right pods measure forward travel and heading; the center pod (mounted perpendicular, forward of the robot's center) measures sideways travel. The **center wheel offset** converts the center pod's rotation into strafe distance — that's why it can sit forward of center.

#### Wiring & mounting

1. Mount the **left** pod on the left side and the **right** pod on the right side, both parallel to the drive direction
2. Mount the **center** pod perpendicular to the drive direction, forward of the robot's center (it may also sit behind center — the offset is signed)
3. Plug each encoder into a free motor port on the REV Hub

#### Driver Station config

Name the **three motor ports** exactly as your code expects:

| Port | Name in config app |
|---|---|
| Left pod | `enc_l` |
| Right pod | `enc_r` |
| Center pod | `enc_c` |

#### MyRobot.java

Add the encoder constants at the top of `MyRobot.java`:

```java
public static final String ENC_LEFT   = "enc_l";
public static final String ENC_RIGHT  = "enc_r";
public static final String ENC_CENTER = "enc_c";
```

The top-of-page example already uses this localizer — swap it in if yours doesn't:

```java
.motors()
// ---- Localizer ----
.withThreeDeadWheels(ENC_LEFT, ENC_RIGHT, ENC_CENTER)
.setTrackWidth(13.0)              // inches, left↔right pod distance
.setCenterWheelOffset(3.5)        // inches, C pod forward of center (signed)
// ---- Tuned values ----
.wheelDiameter(1.37795)           // 35 mm GoBILDA pod; REV 25 mm = 0.984"
.ticksPerRev(2000)                // GoBILDA 5203 = 2000; REV through-bore = 8192
```

Invert any pod that reads backwards:

```java
.invertLeftEncoder()
.invertRightEncoder()
.invertCenterEncoder()
```

#### Tuning

- **Step 2 (Encoders)** — applies: pod diameter + ticks/rev (table above)
- **Step 3 (Track width)** — applies: distance between the left and right pods
- **Step 4 (Center offset)** — applies: how far the center pod sits forward of the robot's center

#### Common issues

- ±1 cm — the most accurate option and the standard for competitive robots
- Robot "spins" in its pose estimate → track width (Step 3)
- Robot appears to rotate while strafing → center offset (Step 4)
- One pod reads backwards → invert just that one (don't guess — the tuner's Steps 1–2 show you which)

</div>

<div class="hw-panel" data-hw-panel="pinpoint">

### 4 · GoBILDA Pinpoint

*A self-contained odometry module that does the math on-board.*

#### What you need

- **GoBILDA Pinpoint** module (v1 or v2)
- **2 GoBILDA odometry pods** — the 4-bar pod (`goBILDA_4_BAR_POD`) is the common choice
- 1 **I2C cable** (module → REV Hub) and 2 **encoder cables** (pods → module)
- Mounting screws for the module and pods

#### How it works

The Pinpoint fuses its two pods and its built-in IMU on-board and reports a pose over I2C — Crawler just reads it. Because the module does the math, the builder values `wheelDiameter`, `ticksPerRev`, `setTrackWidth`, and `setCenterWheelOffset` are **not used** by this localizer; the pod type in `.setConfig` sets the resolution instead.

#### Wiring & mounting

1. Connect the Pinpoint to **any I2C port** on the REV Hub
2. Plug the two pods into the **two encoder ports** on the Pinpoint (they connect directly, no motor ports needed)
3. Mount the Pinpoint on the robot; the pods go wherever they can roll — the **x/y offsets** in `.setConfig` tell Crawler where they are relative to the robot center

> ⚠️ **Pinpoint v2 needs power.** The v1 module is powered over the I2C cable; the v2 has a separate power connection — check its documentation and wire it before testing, or the module won't initialize.

#### Driver Station config

Add an **I2C device** named `odo` with type **GoBILDA Pinpoint** (the REV Hub driver appears as `GoBildaPinpointDriver`). Keep the name exactly as your code expects.

#### MyRobot.java

Add the device constant at the top of `MyRobot.java`:

```java
public static final String PINPOINT = "odo";
```

Swap the localizer block in `builder()`:

```java
.motors()
// ---- Localizer ----
.withPinpoint(PINPOINT)
.setConfig(
        3.0, 2.5,                              // x, y offset from Pinpoint to robot center
        DistanceUnit.CM,                       // the unit those offsets are in
        GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD,
        GoBildaPinpointDriver.EncoderDirection.REVERSED,
        GoBildaPinpointDriver.EncoderDirection.FORWARD)
```

Add the two imports at the top of the file:

```java
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
```

**Offsets:** `x` is how far the Pinpoint sits **forward** of the robot center, `y` how far to the **right** (negative = left of center). Measure from the chip center to the robot's center, in whatever unit you pass.

#### Tuning

The Crawler Tuner's encoder steps (2–4) **don't drive the Pinpoint** — the module is self-contained. Tune it in `.setConfig` instead:

1. Set the **pod type** to match the pods you mounted (4-bar pod for GoBILDA pods)
2. Set the **x/y offsets** to the measured distances
3. Push the robot around and watch the **FTC Dashboard** field view: the pose should follow the robot — forward = forward, strafe = strafe
4. If forward and strafe are swapped (robot "drives sideways"), swap the two pods or flip the `xDirection`/`yDirection` values

#### Common issues

- ±0.5 cm — the most accurate option with the least wiring, but you have to buy the module
- Robot drives sideways when it should go forward → pods or `xDirection`/`yDirection` are wrong
- Pose doesn't move at all → module not powered (v2!) or the I2C device name doesn't match
- `wheelDiameter` / `ticksPerRev` have no effect on the Pinpoint — don't fight the tuner's Step 2, set the pod type in `.setConfig`

</div>

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
