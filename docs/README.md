# Documentation Overview

**Welcome to Crawler.** You now have comprehensive, in-depth documentation to build a functioning FTC autonomous routine in under 30 minutes.

---

## What is Crawler?

Crawler is a **high-performance, low-complexity autonomous library** for FTC robots. It provides:

- **Dead-wheel odometry** (3-wheel or 2-wheel) for accurate position tracking
- **Imperative movement commands** (`drivePID`, `strafePID`, `turnPID`) for simple sequences
- **Pure pursuit path following** (field-oriented) for complex field navigation
- **Real-time tuning** via FTC Dashboard — no recompilation

**Goal:** Get autonomous running in < 30 minutes. Simpler than Road Runner, more powerful than manual teleoperation.

---

## Documentation Files

### Foundational Guides (Read in Order)

1. **[Getting Started & Project Setup](01-getting-started.md)** — 10 minutes
   - Prerequisites and dependencies
   - Creating `MyRobot.java`
   - Your first autonomous OpMode
   - Hardware verification

2. **[Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md)** — 15 minutes
   - Physics of dead-wheel odometry
   - Mechanical setup and wiring
   - Calibration procedure (critical!)
   - Troubleshooting encoder issues

3. **[Robot Configuration & FTC Dashboard](03-robot-configuration.md)** — 10 minutes
   - `RobotConfig.java` structure
   - Measuring physical constants
   - PID tuning via Dashboard
   - Real-time telemetry setup

### Movement System Guides (Choose One or Both)

4. **[Robot-Oriented Movement Commands](04-robot-oriented-movement.md)** — 20 minutes
   - Imperative commands: `drivePID()`, `strafePID()`, `turnPID()`, `arc()`
   - Building simple sequences
   - PID tuning and calibration
   - Best for: 3-5 waypoint sequences, interleaved actions

5. **[Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md)** — 20 minutes
   - Waypoint-based path following
   - The `FOFollower` interface
   - Building complex paths with `Waypoint` builder
   - Event callbacks (`onReach()`)
   - Best for: 6+ waypoint sequences, pre-planned field navigation

### Quick Reference & Troubleshooting

6. **[Quick Reference Guide](06-quick-reference.md)** — 2 minutes
   - Copy-paste templates
   - Hardware names checklist
   - PID tuning quick-fixes
   - Troubleshooting decision tree
   - Common values and profiles

---

## Recommended Reading Paths

### Path A: "I Just Want Autonomous Working ASAP"

1. [Getting Started & Project Setup](01-getting-started.md) — Setup only
2. [Robot-Oriented Movement Commands](04-robot-oriented-movement.md) — Simple commands
3. Build: drivePID → strafePID → turnPID sequences
4. **Time to autonomous: ~25 minutes**

### Path B: "I Want Smooth, Complex Path Following"

1. [Getting Started & Project Setup](01-getting-started.md) — Setup
2. [Understanding Odometry: 3-Dead-Wheel System](02-understanding-odometry.md) — Deep dive
3. [Robot Configuration & FTC Dashboard](03-robot-configuration.md) — Configuration
4. [Field-Oriented Pure Pursuit](05-field-oriented-pure-pursuit.md) — Waypoints + callbacks
5. Build: Complex field paths with precision placement
6. **Time to autonomous: ~60 minutes (includes calibration)**

### Path C: "I Need Both (Flexible Autonomous)"

Read all guides in order:
1. Getting Started
2. Odometry
3. Configuration
4. Robot-Oriented Movement (simple, fast sequences)
5. Field-Oriented Pure Pursuit (complex, smooth paths)
6. **Combine:** RO for pickup, FO for multi-delivery
7. **Time to autonomous: ~90 minutes (full mastery)**

---

## Quick Diagnostic: Which Movement System?

**Use Robot-Oriented if:**
- ✓ Autonomous has < 4 distinct waypoints
- ✓ You need to interleave manual subsystem actions (intake on/off)
- ✓ You prefer simple, readable code
- ✓ You want fastest setup time

**Use Field-Oriented if:**
- ✓ Autonomous has > 5 waypoints
- ✓ You need smooth continuous movement (no stop-start)
- ✓ Complex field geometry (obstacles, multiple delivery zones)
- ✓ You want fastest execution time
- ✓ You want visual path preview (on paper or in AutoGenerator)

**Use Both if:**
- ✓ You have time to master both systems
- ✓ Your strategy mixes simple and complex phases
- ✓ Maximum flexibility (switch between strategies late in season)

---

## Architecture Overview

```
Crawler Library Structure
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

org/firstinspires/ftc/teamcode/Crawler

├── core/
│   ├── Robot/
│   │   ├── CrawlerRobot.java          ← Main robot abstraction
│   │   └── MyRobot.java               ← YOUR robot subclass (you create)
│   │
│   ├── Localizers/
│   │   ├── CrawlerLocaliser.java      ← Interface for position tracking
│   │   ├── ThreeDeadWheelLocaliser.java
│   │   ├── TwoWheelLocaliser.java
│   │   ├── PinpointLocaliser.java
│   │   ├── MotorEncoderLocaliser.java
│   │   └── DevLocaliser.java          ← Fallback (fixed position)
│   │
│   ├── RobotConfig.java               ← All @Config constants (tunable!)
│   │
│   └── utils/
│       ├── Waypoint.java              ← Path building
│       ├── CrawlerMath.java           ← Helpers
│       ├── Vector2d.java
│       └── Point.java
│
├── RobotOrient/                       ← Imperative movement
│   └── ROMovementEngine.java          ← drivePID, strafePID, turnPID, arc
│
├── FieldOrient/                       ← Pure pursuit
│   ├── FOFollower.java                ← High-level path execution
│   └── RobotMovement.java             ← Pure pursuit algorithm
│
├── Tuning/                            ← Calibration OpModes
│   ├── TuneOdometry.java
│   └── CrawlerTuner.java
│
├── Vision/                            ← Optional vision integration
│   └── AprilTagWebcam.java
│
└── Dashboard/                         ← FTC Dashboard utilities
    └── DashboardFieldViewUtils.java

KEY INSIGHT:
- CrawlerRobot is the central abstraction — all movement goes through it
- RobotConfig holds all constants — change values in Dashboard, not code
- Two movement systems (RO vs FO) share the same odometry & hardware layer
- Tuning OpModes let you calibrate without writing custom code
```

---

## Conceptual Flow

### Initialization (Startup)

```
OpMode.runOpMode()
    ↓
CrawlerRobot robot = MyRobot.build(hardwareMap)
    ↓
CrawlerRobot.__init__
    ├─ Initialize 4 drive motors
    ├─ Initialize IMU
    ├─ Load encoder config
    └─ Create Localiser (ThreeDeadWheelLocaliser, etc.)
    ↓
Ready for movement commands
```

### Autonomous Loop (Execution)

#### Robot-Oriented (Imperative)

```
robot.drivePID(12)
    ↓
Record current odometry position
    ↓
Loop:
    ├─ Update odometry (localiser.update())
    ├─ Calculate error: targetPos - currentPos
    ├─ Apply PID: power = Kp*error + Kd*dError
    ├─ Send motor command: robot.drive(power, 0, 0)
    └─ Sleep and retry
    ↓
Threshold met → return control to caller
    ↓
Next command (e.g., strafePID(6))
```

#### Field-Oriented (Pure Pursuit)

```
follower.follow(waypoints)
    ↓
For each waypoint segment:
    ├─ Interpolate path between waypoints
    ├─ Extend path beyond final waypoint
    │
    └─ Loop:
        ├─ Update odometry position
        ├─ Find closest point on path
        ├─ Look ahead 10" (follow distance)
        ├─ Command: drive toward follow point
        ├─ If near waypoint:
        │   └─ Fire onReach() callback
        └─ Move to next waypoint when arrival threshold met
    ↓
All waypoints complete or OpMode inactive → return
```

---

## Critical Parameters You Must Know

### Hardware Names (In Your Device Configuration)

```
"frontLeft", "frontRight", "backLeft", "backRight"  ← Drive motors
"imu"                                                ← IMU
"enc_l", "enc_r", "enc_c"                           ← Encoders
```

**Action:** Check these **exactly** in Android Studio or Rev Hardware Client before starting.

### Physical Constants (Measure Them!)

```
TRACK_WIDTH           13.0"    ← Side-to-side encoder spacing (CRITICAL)
CENTER_WHEEL_OFFSET   3.5"     ← Forward offset of center encoder
WHEEL_DIAMETER        1.37795" ← Dead wheel diameter (goBILDA standard)
TICKS_PER_REV         2000     ← Encoder counts per revolution
```

**Action:** Measure with ruler/calipers. These feed directly into odometry math.

### PID Gains (Tune via Dashboard)

```
Kp          0.05 → 0.10    ← Start low, increase until responsive
Ki          0.0             ← Usually leave at zero
Kd          0.0 → 0.05     ← Add after Kp is good
MIN_POWER   0.15            ← Friction compensation (increase if stops short)
```

**Action:** Start with defaults, tune via FTC Dashboard while autonomous runs.

---

## Success Checklist

### Before First Autonomous

- [ ] Clone Crawler library into `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`
- [ ] Create `MyRobot.java` with hardware names from your configuration
- [ ] Compile and upload
- [ ] Run `HardwareCheckAuto` (from Getting Started) — all 4 motors + encoders respond
- [ ] Measure physical constants (track width, center offset, wheel diameter)
- [ ] Run `TuneOdometer` OpMode — move robot forward 12", verify odometry reads 12 inches
- [ ] Create first simple autonomous: `drivePID(24)` then `strafePID(12)`
- [ ] Robot moves forward 24 inches, then left 12 inches

### Before Competition

- [ ] PID tuned to smooth behavior (no overshoot/oscillation) via Dashboard
- [ ] Multiple test runs on actual competition field (carpet/tile affects tuning)
- [ ] Autonomous time verified — runs consistently within ±2 seconds
- [ ] Telemetry shows correct final position (±2 inches)
- [ ] All callbacks (`onReach()`) fire at correct waypoints
- [ ] Backup `RobotConfig` JSON exported (save tuned values)
- [ ] Second robot profile created if you have multiple robots
- [ ] Team knows how to adjust PID on-the-fly if needed

---

## Common Starting Mistakes & Fixes

| Mistake | Fix | Doc Reference |
|---------|-----|----------------|
| "Device not found" exception | Check hardware names in `MyRobot.java` | [01-04](01-getting-started.md#step-3-create-your-robot-class) |
| Odometry reports 0,0,0 always | Verify encoder I2C connections | [02-41](02-understanding-odometry.md#common-pitfalls) |
| Robot moves but in wrong direction | Invert motor: `.frontLeftInverted(true)` | [01-17](01-getting-started.md#step-3-create-your-robot-class) |
| Autonomous doesn't start | Make sure `waitForStart()` is called | [01-27](01-getting-started.md#step-4-create-your-first-autonomous-opmode) |
| Robot moves but trajectory curves | Calibrate track width via `TuneOdometry` | [02-25](02-understanding-odometry.md#phase-2-tune-via-movement-test) |
| PID changes in Dashboard don't work | Read values dynamically in loop | [03-38](03-robot-configuration.md#troubleshooting-configuration) |
| Pure pursuit path jerky | Decrease `DEFAULT_FOLLOW_DISTANCE` | [05-18](05-field-oriented-pure-pursuit.md#common-pitfalls) |

---

## Learning Time Estimates

For a team new to Crawler:

| Phase | Time | Deliverable |
|-------|------|-------------|
| Setup | 10-15 min | `MyRobot.java` created, hardware verified |
| Calibration | 20-30 min | Odometry tuned via `TuneOdometry` OpMode |
| Tuning | 20-30 min | PID gains dialed in via Dashboard |
| Autonomous Dev | 30-60 min | Full autonomous routine tested |
| **Total** | **80-135 min** | **Production-ready autonomous** |

**With two team members:** Parallelize setup + calibration → reduce to 60-90 minutes.

---

## Key Insights (Aha! Moments)

1. **Odometry is Everything** — If position tracking is wrong, everything fails. Spend time calibrating.

2. **@Config is Powerful** — You never recompile to tune. Change values in Dashboard while autonomous runs.

3. **Pure Pursuit is Elegant** — One algorithm handles any path shape. More waypoints = smoother trajectory (usually).

4. **Start Simple, Iterate** — Begin with `drivePID(distance)`. Add complexity (turns, strafes) one at a time.

5. **Test on Real Field** — Practice field tuning ≠ competition field. Friction changes, recalibrate.

6. **PID is Tuning, Not Magic** — Start conservative. Increase proportional gain until responsive. Add derivative for smoothing.

7. **Callbacks Are Your Friends** — Fire `onReach()` to activate subsystems (intake, depositor, arm) at the right moment.

---

## Beyond Crawler (Advanced Topics)

Once you master Crawler, explore:

- **Vision Integration** — Detect AprilTags, adjust autonomous paths dynamically
- **State Machines** — Manage complex multi-phase autonomous (`CrawlerStateMachine<S>`)
- **Hot Reload** — Send JSON paths over WiFi without APK rebuild
- **Motion Profiling** — Advanced path generation with velocity constraints (via Road Runner)
- **Localization Fusion** — Combine odometry + vision for robust positioning

These are all **documented separately** (see `Vision/`, `Tuning/` folders).

---

## Support & Troubleshooting

### Check These First

1. **Logcat errors** → `adb logcat | grep Exception`
2. **FTC Dashboard telemetry** → Real-time debugging
3. **This documentation** → Search keywords
4. **Quick Reference** → [06-quick-reference.md](06-quick-reference.md)

### Troubleshooting Flowchart

```
Does robot move?
├─ NO  → [01-getting-started.md#step-5](01-getting-started.md#step-5-first-run--hardware-verification)
└─ YES → Odometry working?
         ├─ NO  → [02-understanding-odometry.md#common-pitfalls](02-understanding-odometry.md#common-pitfalls)
         └─ YES → Movement smooth?
                  ├─ NO  → [03-robot-configuration.md#pid-tuning](03-robot-configuration.md#pid-tuning)
                  └─ YES → Production ready!
```

---

## FAQ

**Q: Can I use a differential drive (tank tread)?**  
A: RO movement won't strafe. Use RO for `drivePID()` + `turnPID()` only. FO pure pursuit still works.

**Q: What if I don't have dead wheels?**  
A: Use `MotorEncoderLocaliser` or `PinpointLocaliser`. See [01-Appendix](01-getting-started.md#appendix-alternative-localizers).

**Q: How accurate is dead wheel odometry?**  
A: ±2% with good calibration. Better than drive motor encoders (which suffer wheel slip).

**Q: Can I change tuning during season?**  
A: Absolutely. Dashboard values are read at runtime. Export config as JSON to preserve between seasons.

**Q: Does pure pursuit always take the shortest path?**  
A: No, it follows waypoints in order. For path optimization, use separate tool (AutoGenerator web app).

**Q: What's the maximum number of waypoints?**  
A: No hard limit. 100+ waypoints fine. Pure pursuit stays smooth.

**Q: How do I handle obstacles?**  
A: Add intermediate waypoints to navigate around them. Pure pursuit will smooth the path.

---

## Credits & Sources

- **Pure Pursuit Algorithm** — Original paper by R. Craig Coulter (CMU)
- **FTCLib** — Open source FTC utilities (holonomic odometry, PID, etc.)
- **FTC Dashboard** — AcmeRobotics real-time telemetry
- **GoBilda** — Hardware, odometry pods, Pinpoint sensor
- **FIRST & FTC Community** — Collective robotics wisdom

---

## Next Steps

**Ready to build?** 

1. Start with [Getting Started & Project Setup](01-getting-started.md)
2. Choose your path: Robot-Oriented vs Field-Oriented
3. Name your autonomous files: `SimpleScoring.java`, `FullDelivery.java`, etc.
4. Test on your practice field
5. Tune PID on competition field

**Questions?** Check [Quick Reference](06-quick-reference.md) for templates and common problems.

**Your autonomous. Under 30 minutes. Let's go.**

---

**Documentation last updated:** April 2026  
**Crawler version:** 1.0 (1st Documentation Release)  
**Target audience:** High school roboticists in FTC (Gauteng, non-profit teams)

For the latest updates, see: https://github.com/CreepyCrawlies16771/Crawler
