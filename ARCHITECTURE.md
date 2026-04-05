# Crawler Robot Architecture & Integration Guide

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Layers](#architecture-layers)
3. [Core Components](#core-components)
4. [Data Flow & Integration](#data-flow--integration)
5. [Module Details](#module-details)
6. [Configuration System](#configuration-system)
7. [Development Workflow](#development-workflow)
8. [Integration Examples](#integration-examples)

---

## Project Overview

**Crawler** is an advanced FTC (FIRST Tech Challenge) robot control system built on the FTC SDK (DECODE 2025-2026). It provides a modular, reusable architecture for:

- **Hardware Abstraction**: Unified interface for motors, sensors, and IMU
- **Localization System**: Multiple odometry solutions (Motor Encoder, Dead Wheels, Pinpoint)
- **Movement Control**: Both Robot-Oriented and Field-Oriented drive systems
- **Vision Integration**: AprilTag detection and camera calibration
- **Real-time Tuning**: FTC Dashboard integration for on-the-fly parameter adjustment
- **Autonomous Navigation**: Pure Pursuit path following and waypoint-based movement

### Project Structure
```
Crawler/
├── TeamCode/
│   └── src/main/java/org/firstinspires/ftc/teamcode/Crawler/
│       ├── core/                  # Core system logic
│       │   ├── Robot/            # Hardware abstraction layer
│       │   ├── Localizers/       # Odometry & localization
│       │   ├── utils/            # Math utilities & data structures
│       │   └── RobotConfig.java   # Centralized configuration
│       ├── RobotOrient/          # Robot-oriented movement
│       ├── FieldOrient/          # Field-oriented movement
│       ├── Tuning/               # Guided calibration system
│       │   └── CrawlerTuner.java # Complete 12-step tuning OpMode
│       ├── Vision/               # Vision processing (AprilTags)
│       ├── Dashboard/            # Telemetry visualization
│       ├── TuningRobotOrient/    # Tuning utilities (legacy)
│       └── PleaseWorkd.java      # Main entry point example
├── FtcRobotController/          # SDK samples & framework
├── docs/                         # GitBook documentation
├── wiki/                         # GitHub Wiki documentation
└── [configuration files]
```

---

## Architecture Layers

The system is built in **4 distinct abstraction layers**, each with a specific responsibility:

### Layer 1: Hardware Abstraction (`core/Robot/`)
**Purpose**: Isolate hardware details from the rest of the system

```
Physical Hardware (Motors, IMU, Encoders)
              ↓
    ┌─────────────────────┐
    │   CrawlerRobot      │  ← Unified hardware interface
    │  (Builder Pattern)   │
    └─────────────────────┘
              ↓
    - Motor objects (FL, FR, BL, BR)
    - IMU (Inertial Measurement Unit)
    - Encoder references
    - Localizer instance
```

**Key Classes**:
- `CrawlerRobot`: Main hardware wrapper with builder pattern
- `Robot`: Alternative/legacy hardware interface
- Handles motor initialization, configuration, and basic drive commands

### Layer 2: Localization System (`core/Localizers/`)
**Purpose**: Determine robot position and heading in the field

```
              Odometry Data (Encoder ticks, motor positions)
                        ↓
    ┌─────────────────────────────────────┐
    │      CrawlerLocaliser (Abstraction)  │
    └─────────────────────────────────────┘
                        ↓
        ┌─────────────────────────────────────┐
        │     Localizer Implementation Selector │
        ├─────────────────────────────────────┤
        │ • MotorEncoderLocaliser             │
        │ • TwoWheelLocaliser                 │
        │ • ThreeDeadWheelLocaliser           │
        │ • PinpointLocaliser                 │
        └─────────────────────────────────────┘
                        ↓
                 Pose Object (x, y, heading)
```

**Available Localizers**:
1. **MotorEncoderLocaliser**: Uses motor encoder data directly (least accurate)
2. **TwoWheelLocaliser**: Uses 2 odometry pod wheels (legacy)
3. **ThreeDeadWheelLocaliser**: Uses 3 deadwheel pods (most accurate for dead wheels)
4. **PinpointLocaliser**: Uses GoBilda Pinpoint device (GPS-like accuracy)

### Layer 3: Movement Control (`RobotOrient/` & `FieldOrient/`)
**Purpose**: Convert high-level movement commands to motor outputs

```
        User Command (Move forward, turn, strafe)
                        ↓
        ┌───────────────────────────────┐
        │  Movement Engine Selection    │
        │  (Robot-Oriented or Field-    │
        │   Oriented)                   │
        └───────────────────────────────┘
                        ↓
        ┌───────────────────────────────┐
        │    PID Controllers / Motion   │
        │    Math (vector rotation,     │
        │    path following)            │
        └───────────────────────────────┘
                        ↓
        Motor Power Output (FL, FR, BL, BR)
```

**Two Movement Paradigms**:
- **Robot-Oriented (ROMovementEngine)**: Movements relative to robot's current orientation
- **Field-Oriented (RobotMovement)**: Movements relative to fixed field coordinates

### Layer 4: Vision & Perception (`Vision/`)
**Purpose**: Process camera input for positioning and navigation

```
    Camera Feed
        ↓
    AprilTagWebcam Processing
        ↓
    AprilTag Detection (x, y, rotation)
        ↓
    Used by localization & autonomous navigation
```

---

## Core Components

### 1. CrawlerRobot - Hardware Abstraction Layer

**Location**: `core/Robot/CrawlerRobot.java`

**Purpose**: Unified interface to all robot hardware with builder pattern for easy configuration.

**Architecture**:
```java
public class CrawlerRobot {
    // Hardware References
    public final MotorEx frontRight, frontLeft, backRight, backLeft;
    public final IMU imu;
    public final Localisation localisation;
    public final CrawlerLocaliser localiser;
    
    // Private encoder references (for odometry)
    final MotorEx leftEncoder, rightEncoder, centerEncoder;
    
    // Physical configuration
    final double trackWidth, centerWheelOffset;
    final String pinpointDeviceName;
}
```

**Key Features**:
- **Builder Pattern**: Fluent API for configuration
- **Type-Safe Hardware Access**: No casting, compile-time safety
- **Localizer Auto-Selection**: Automatically instantiates correct localizer type
- **Unified Drive Methods**: `drive()` and `driveFieldRelative()` handle all motor math

**Example Usage**:
```java
CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
    .frontLeft("frontLeft")
    .frontRight("frontRight")
    .backLeft("backLeft")
    .backRight("backRight")
    .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
    .setTrackWidth(13.0)
    .setCenterWheelOffset(3.5)
    .imu("imu")
    .build();
```

**Drive Methods**:
```java
// Robot-relative movement (forward/back, strafe, rotation)
robot.drive(forward, strafe, rotate);

// Field-relative movement (accounts for heading)
robot.driveFieldRelative(forward, strafe, rotate);

// Stop all motors
robot.stop();
```

### 2. Localization System - Odometry

**Location**: `core/Localizers/`

**Purpose**: Calculate the robot's position (x, y) and heading (θ) at any moment.

**Data Flow**:
```
Encoder Ticks (from dead wheels or motors)
          ↓
    Δ Position Calculation
          ↓
    Δ Heading Calculation
          ↓
    Update Pose (x, y, θ)
          ↓
    getCurrPose() → Used by movement systems
```

**Localizer Comparison**:

| Localizer | Accuracy | Setup | Cost | Best For |
|-----------|----------|-------|------|----------|
| MotorEncoderLocaliser | Low | Simple | Free | Testing, basic auton |
| TwoWheelLocaliser | Medium | Medium | $60 | Budget builds |
| ThreeDeadWheelLocaliser | High | Complex | $180 | Competitive teams |
| PinpointLocaliser | Highest | Simple | $300 | Championship level |

**Example - Three Dead Wheel Setup**:
```java
// Hardware layout:
// leftOdo: Parallel to drive motors (forward/back)
// rightOdo: Parallel to drive motors (forward/back)
// centerOdo: Perpendicular to drive motors (strafe)

// Configuration:
builder.withThreeDeadWheels("enc_l", "enc_r", "enc_c")
       .setTrackWidth(13.0)           // distance between left/right wheels
       .setCenterWheelOffset(3.5)     // distance from center to left/right
       .build();
```

**Pose Output**:
```java
Pose currPose = robot.localiser.getPose();
double x = currPose.getX();          // inches
double y = currPose.getY();          // inches
double heading = currPose.getHeading(); // radians
```

### 3. Robot-Oriented Movement - ROMovementEngine

**Location**: `RobotOrient/ROMovementEngine.java`

**Purpose**: Movement relative to robot's current orientation (like a gamepad-controlled car).

**Key Characteristics**:
- **Local Coordinates**: "Forward" always means toward the front of the robot
- **Immediate Response**: No path planning, direct motor control
- **Easy Learning Curve**: Natural for drivers
- **TeleOp-Friendly**: Works well with gamepad input

**Components**:
```java
public abstract class ROMovementEngine extends LinearOpMode {
    protected Robot robot;                    // Robot hardware
    protected AprilTagWebcam aprilTagWebcam;  // Vision
    
    public abstract void runPath();           // OpMode must implement
}
```

**Sub-Components**:
- **AnimationBuilder**: Sequences complex movements over time
- **HeadingTimeline**: Controls rotation during movement
- **Sorter & BALLCOLOR**: Game-specific logic (if applicable)
- **Tuner**: Real-time parameter adjustment during autonomous

**Movement Example**:
```java
// Move relative to robot's current facing
robot.drive(
    forward,   // positive = toward front
    strafe,    // positive = to the right
    rotate     // positive = counter-clockwise
);
```

### 4. Field-Oriented Movement - RobotMovement

**Location**: `FieldOrient/RobotMovement.java`

**Purpose**: Movement relative to fixed field coordinates using Pure Pursuit path following.

**Key Characteristics**:
- **Absolute Coordinates**: Only field coordinates matter, not robot orientation
- **Path Planning**: Waypoint-based navigation with look-ahead
- **Precision**: Smooth curves through waypoints
- **Autonomous-Friendly**: No driver input needed

**Core Algorithm - Pure Pursuit**:
```
1. Read current robot pose (x, y, heading)
2. Given waypoint path, find look-ahead point
3. Calculate vector from robot to look-ahead point
4. Compute desired direction and speed
5. Command motors → robot follows path smoothly
```

**Data Flow**:
```
Waypoint List (x₁, y₁) → (x₂, y₂) → ... → (xₙ, yₙ)
                        ↓
        RobotMovement.follow(waypoints, angle)
                        ↓
        Pure Pursuit Algorithm
                        ↓
        Extract look-ahead point from path
                        ↓
        Calculate movement vector
                        ↓
        Apply rotation (field-relative to robot-relative)
                        ↓
        robot.drive(forward, strafe, rotate)
```

**Example Usage**:
```java
List<Waypoint> path = new ArrayList<>();
path.add(new Waypoint(0, 0, 0));           // Start at origin
path.add(new Waypoint(24, 0, 0));          // Move right 24 inches
path.add(new Waypoint(24, 24, Math.PI/2)); // Move up 24 inches
path.add(new Waypoint(0, 24, Math.PI));    // Return to left side

RobotMovement movement = new RobotMovement(robot);
movement.follow(path, 0.0); // Follow path at angle 0 radians
```

**Configurable Parameters** (in `RobotConfig.FieldOriented`):
```java
DEFAULT_MOVE_SPEED = 0.7;        // 0-1, where 1 is max
DEFAULT_TURN_SPEED = 0.4;        // Rotation speed
DEFAULT_FOLLOW_DISTANCE = 10.0;  // Look-ahead distance in inches
ARRIVAL_THRESHOLD = 2.0;         // "Close enough" to waypoint
```

### 5. Vision System - AprilTag Detection

**Location**: `Vision/AprilTagWebcam.java`

**Purpose**: Detect AprilTags for absolute positioning and game piece identification.

**Components**:
- **AprilTagWebcam**: Main vision processor
- **Rotation**: Converts raw detections to robot-relative values
- **Calibration Data**: Per-camera configuration in `teamwebcamcalibrations.xml`

**Data Flow**:
```
Camera Frame
    ↓
AprilTag Detection (library)
    ↓
Extract: [x, y, z, rotation, id]
    ↓
Transform to robot coordinates
    ↓
available via aprilTagWebcam.getDetections()
```

**Usage Example**:
```java
AprilTagWebcam vision = new AprilTagWebcam();
vision.init(hardwareMap, telemetry);

// In loop:
List<AprilTagDetection> detections = vision.getDetections();
for (AprilTagDetection detection : detections) {
    int id = detection.id;              // Which AprilTag
    double range = detection.ftmRange;  // Distance in feet
    double bearing = detection.ftmRbearing;  // Angle
    // Use for positioning or game logic
}
```

### 6. Dashboard Integration - FTC Dashboard

**Location**: `Dashboard/DashboardFieldViewUtils.java`

**Purpose**: Real-time visualization and parameter tuning without redeploying code.

**Features**:
- **Live Field View**: See robot, waypoints, and trajectories in real-time
- **Parameter Adjustment**: Change `@Config` values on-the-fly via phone UI
- **Telemetry Graphing**: Plot variables over time
- **Debug Information**: Display internal state (pose, encoders, etc.)

**How It Works**:
```
FTC Dashboard App (phone)
        ↓
WiFi Network
        ↓
Robot Controller
        ↓
OpMode (draw.line(), telemetry.addData())
        ↓
Physics simulation or live robot
```

**Example - Drawing on Dashboard**:
```java
TelemetryPacket packet = new TelemetryPacket();

// Draw the planned path (blue line)
DashboardFieldViewUtils.drawLine(packet,
    x1, y1, x2, y2,
    DashboardFieldViewUtils.FieldColor.BLUE);

// Draw robot (red circle)
DashboardFieldViewUtils.drawCircle(packet,
    robotX, robotY,
    DashboardFieldViewUtils.FieldColor.RED);

// Send to dashboard
dashboard.sendTelemetry(packet);
```

**Configuration Values with Live Tuning**:
```java
@Config  // Marks class as tunable
public static class FieldOriented {
    public static double DEFAULT_MOVE_SPEED = 0.7;
    public static double DEFAULT_TURN_SPEED = 0.4;
    // Change these via FTC Dashboard → click robot → adjust values
    // Changes apply immediately to running OpMode
}
```

---

## Data Flow & Integration

### Complete System Integration Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   FTC OpMode (TeleOp/Autonomous)            │
│  (Entry Point - extends LinearOpMode or iterative OpMode)   │
└────────────┬────────────────────────────────────────────────┘
             │ Calls initializations
             ↓
┌────────────────────────────────────────────────────────────┐
│  CrawlerRobot.Builder → .build()                           │
│  ├─ Motors (FL, FR, BL, BR)                               │
│  ├─ IMU initialization                                     │
│  ├─ Encoder configuration (for localizer)                 │
│  └─ Localizer instantiation based on config               │
└────────────┬─────────────────────────────────────────────┘
             │
             ├──────────────────────┬──────────────────┐
             ↓                      ↓                  ↓
    ┌─────────────────┐   ┌────────────────┐  ┌──────────────┐
    │ CrawlerLocaliser│   │ AprilTagWebcam │  │ RobotMovement│
    │ (Odometry)      │   │ (Vision)       │  │ (Path Follow)│
    │ Continuously    │   │ On Detection   │  │ Continuous   │
    │ tracks pose     │   │ provides pose  │  │ path tracking│
    └────────┬────────┘   └────────┬───────┘  └──────┬───────┘
             │                     │                 │
             └─────────────────────┼─────────────────┘
                                   ↓
                        ┌──────────────────────┐
                        │  CURRENT POSE INFO   │
                        │ (x, y, heading)      │
                        │ Available to all     │
                        │ subsystems via       │
                        │ robot.localiser      │
                        │ .getPose()           │
                        └──────────┬───────────┘
                                   │
                ┌──────────────────┼──────────────────┐
                ↓                  ↓                  ↓
    ┌────────────────────┐ ┌──────────────┐ ┌──────────────────┐
    │ ROMovementEngine   │ │ RobotMovement│ │ TeleOp Controls  │
    │ Robot-relative     │ │ Field-rel    │ │ Direct gamepad   │
    │ movement, tuning   │ │ path follow  │ │ motors via       │
    │                    │ │              │ │ robot.drive()    │
    └────────┬───────────┘ └──────┬───────┘ └─────────┬────────┘
             │                    │                   │
             └────────────────────┼───────────────────┘
                                  ↓
                    ┌─────────────────────────┐
                    │  robot.drive(...)       │
                    │  or drive(...) methods  │
                    │  Converts request to    │
                    │  motor power (-1 to 1)  │
                    └────────────┬────────────┘
                                 ↓
                    ┌─────────────────────────┐
                    │  Motor Control (FL, FR, │
                    │  BL, BR) send power to  │
                    │  physical motors        │
                    └────────────┬────────────┘
                                 ↓
                    ┌─────────────────────────┐
                    │  Physical Motion        │
                    │  (robot moves)          │
                    └─────────────────────────┘
                                 ↑
                                 │ Motor encoders
                                 │ update position
                ┌────────────────┘
                │
                ↓
        Localizer updates
        Uses encoder data to
        recalculate pose
        (Closing the loop)
```

### Typical Operating Cycle (per loop iteration)
```
Time (ms) | Action
──────────┼──────────────────────────────────────────────────
0         | OpMode.runOpMode() iteration starts
1-2       | robot.localiser.update() - read encoders, calc pose
2-3       | aprilTagWebcam.detectAprilTags() - process image
3-4       | User code: RobotMovement.follow() or gamepad input
4-5       | Calculate desired motor powers based on current pose
5-6       | robot.drive(fwd, str, rot) - set motor powers
6-7       | Motors begin moving, encoders generate new ticks
7-10      | Telemetry update (add to dashboard, print to driver)
10-20     | Dashboard JSON serialization & WiFi transmission
20        | Next iteration (typically 50-60 Hz = 16-20ms cycle)
```

---

## Module Details

### RobotConfig - Centralized Configuration

**Location**: `core/RobotConfig.java`

**Purpose**: Single source of truth for all tunable parameters.

**Structure**:
```java
public class RobotConfig {
    // 1. Hardware names (internal, set via Builder)
    static String FRONT_LEFT = "frontLeft";
    static String FRONT_RIGHT = "frontRight";
    // ... etc
    
    // 2. Physical constants (must measure)
    @Config
    public static class Odometry {
        public static double TRACK_WIDTH = 13.0;           // inches
        public static double CENTER_WHEEL_OFFSET = 3.5;    // inches
        public static double WHEEL_DIAMETER = 1.37795;     // inches
        public static double TICKS_PER_REV = 2000;
    }
    
    // 3. PID controllers (for robot-relative movement)
    @Config
    public static class RobotOriented {
        public static double Kp = 0.05;      // Proportional
        public static double Ki = 0.0;       // Integral
        public static double Kd = 0.0;       // Derivative
        public static double MIN_POWER = 0.15;  // Friction compensation
    }
    
    // 4. Field-oriented parameters (tunable via dashboard)
    @Config
    public static class FieldOriented {
        public static double DEFAULT_MOVE_SPEED = 0.7;
        public static double DEFAULT_TURN_SPEED = 0.4;
        public static double DEFAULT_FOLLOW_DISTANCE = 10.0;
        public static double ARRIVAL_THRESHOLD = 2.0;
    }
    
    // 5. Hardware limits
    public static class RobotBase {
        public static double MAX_DRIVE_SPEED = 1.0;
        public static double MIN_DRIVE_SPEED = 0.1;
    }
}
```

**How to Measure Physical Constants**:

1. **TRACK_WIDTH**: Distance between left and right drive wheels (perpendicular to forward direction)
   - Measure center-to-center on drive wheels
   - Typical: 12-15 inches

2. **CENTER_WHEEL_OFFSET**: Distance from center of left/right wheels to center odometry wheel
   - Measure along forward direction
   - Typical: 2-5 inches

3. **WHEEL_DIAMETER**: Size of odometry pod wheels
   - Common: 1.37795 inches (35mm goBILDA pods)
   - Check your specific pods

4. **TICKS_PER_REV**: Encoder resolution
   - Common: 2000 (goBILDA motors)
   - Check motor datasheet

### 7. CrawlerTuner - Guided Calibration System

**Location**: `Tuning/CrawlerTuner.java`

**Purpose**: Complete guided 12-step tuning OpMode that walks teams through odometry calibration, PID tuning, and pure pursuit parameter adjustment without requiring advanced knowledge.

**Architecture**:
```java
@TeleOp(name = "Crawler Tuner", group = "Crawler Tuning")
public class CrawlerTuner extends LinearOpMode {
    private int currentStep;        // 0-11, strict progression
    private TuneValues values;      // Holds all tuning state
    private CrawlerRobot robot;     // Hardware reference
    private RobotMovement movement; // Pure pursuit for FO tests
}
```

**Tuning Sequence**:

| Step | Name | Tests | Tunes | Validation |
|------|------|-------|-------|------------|
| 0 | Hardware Verification | Spin motors | — | Visual inspection |
| 1 | IMU Verification | Manual rotation | — | Heading read |
| 2 | Track Width | 10 spinning rotations | `TRACK_WIDTH` | Heading error |
| 3 | Center Wheel Offset | 10 spinning rotations | `CENTER_WHEEL_OFFSET` | Y drift analysis |
| 4 | Odometry Accuracy | 48×48" square by hand | — | EXCELLENT/PASS/MARGINAL/FAIL |
| 5 | Drive PID | 48" straight forward | `Kp` | Distance error |
| 6 | Strafe PID | 48" straight right | `strafe_Kp` | Distance error |
| 7 | Turn PID | 720° in-place spin | `STEER_P` | Heading error |
| 8 | Pure Pursuit Follow Distance | L-shaped path | `DEFAULT_FOLLOW_DISTANCE` | Corner behavior |
| 9 | Pure Pursuit Move Speed | 96" straight | `DEFAULT_MOVE_SPEED` | Smoothness & accuracy |
| 10 | Integration Test | Full RO + FO circuit | — | Return-to-origin error |
| 11 | Complete | — | — | Summary display |

**Gamepad Controls**:
- **Right Bumper** — Run current step test
- **D-pad Up/Down** — Adjust current value (step-specific granularity)
- **Circle (B)** — Accept & advance to next step
- **Triangle (Y)** — Go back to previous step (preserves all tuned values)
- **X (2-sec hold)** — Skip current step (warning displayed)

**Key Features**:
- **Sequential Flow**: No skipping ahead, each step validates before advancement
- **Live Sidebar**: Displays all 7 tuned values every loop
- **FTC Dashboard Integration**: Field view + motor power graphs
- **Auto-Correction**: Track width and center offset suggest adjustments
- **JSON Persistence**: Saves to `/sdcard/Crawler/tune.json` and `tune_summary.txt`
- **Full Validation**: EXCELLENT/PASS/MARGINAL/FAIL thresholds for odometry test
- **Step Timers**: Elapsed time per step + total elapsed

**Values Tuned**:
```java
// From RobotConfig
Odometry.TRACK_WIDTH
Odometry.CENTER_WHEEL_OFFSET
RobotOriented.Kp           (drive forward PID)
RobotOriented.strafe_Kp    (drive strafe PID)
RobotOriented.STEER_P      (turn in-place PID)
FieldOriented.DEFAULT_FOLLOW_DISTANCE
FieldOriented.DEFAULT_MOVE_SPEED
```

**Integration with Development Workflow**:
1. Team deploys `CrawlerTuner` to robot
2. Follows 12-step process with guidance at each step
3. System automatically saves final values to SD card
4. Team copies values from `tune_summary.txt` into `RobotConfig.java`
5. Redeploy with tuned values for autonomous/TeleOp

**Example Usage**:
```java
// CrawlerTuner runs as a complete OpMode
// No additional code needed — just deploy and run
// Gamepad controls everything
```

### Utils Module - Math & Data Structures

**Location**: `core/utils/`

**Key Classes**:

```java
// Represents position and rotation
class Pose {
    double x, y, heading;  // heading in radians
}

// 2D point (x, y)
class Point { double x, y; }

// Direction vector with magnitude
class Vector2d { double x, y; }

// Waypoint with optional speed/heading constraints
class Waypoint extends Point {
    double heading;
    double speed;
}

// Math utilities
class CrawlerMath {
    // Distance formula, angle wrapping, vector rotation, etc.
}
```

**Usage**:
```java
// Create a pose
Pose startPose = new Pose(0, 0, 0);  // x, y, heading

// Do math
double distance = CrawlerMath.distance(pose1, pose2);
double angle = CrawlerMath.atan2(dy, dx);

// Build path
List<Waypoint> path = new ArrayList<>();
path.add(new Waypoint(0, 0, 0));          // Type 1: Point
path.add(new Waypoint(24, 24, Math.PI/4));  // Type 2: Point with heading
```

### Annotations Module

**Location**: `annotations/`

**Markers for Code Organization**:
```java
@Experimental  // New, not fully tested
@Deprecated    // Old, use alternative
@TuningRequired  // Needs parameter adjustment
```

---

## Configuration System

### How to Configure Your Robot

**Step 1: Physical Measurement**
1. Measure track width (wheel separation)
2. Measure center wheel offset
3. Determine your odometry system type

**Step 2: Update RobotConfig**
```java
// Edit RobotConfig.java
public static class Odometry {
    public static double TRACK_WIDTH = 13.5;      // Your measurement
    public static double CENTER_WHEEL_OFFSET = 3.2;  // Your measurement
}
```

**Step 3: Initialize Robot in OpMode**
```java
// Choose your localization method
CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
    .frontLeft("frontLeft")
    .frontRight("frontRight")
    .backLeft("backLeft")
    .backRight("backRight")
    .withThreeDeadWheels("leftOdo", "rightOdo", "centerOdo")  // Or choose another
    .setTrackWidth(13.0)
    .setCenterWheelOffset(3.5)
    .imu("imu")
    .build();
```

**Step 4: Tune via FTC Dashboard**

While OpMode is running:
1. Connect to robot via WiFi
2. Go to FTC Dashboard address (ask team lead)
3. Select your robot
4. Modify values in `RobotConfig` → `FieldOriented` or `RobotOriented` sections
5. **Values update instantly** - no restart needed
6. Save final tuned values back to `RobotConfig.java`

### FTC Dashboard Installation

```bash
# Only needs to be done once on computer
npm install -g @acmerobotics/ftc-dashboard

# Start before running any autonomously code with dashboard
ftc-dashboard connect <ROBOT_IP>
```

---

## Development Workflow

### Tuning Your Robot (Pre-Autonomous Workflow)

**Required Step Before Autonomous Development**:

1. **Deploy CrawlerTuner** to robot controller
   ```
   Build → Deploy CrawlerTuner to Android Device
   ```

2. **Run the Tuner OpMode** (12 steps, ~10-15 minutes total)
   ```
   Select "Crawler Tuner" from OpMode list
   Press Play
   Follow on-screen instructions via gamepad
   ```

3. **Retrieve Tuned Values**
   ```
   ADB: adb pull /sdcard/Crawler/tune_summary.txt
   OR: Connect to robot, manually read values from telemetry
   ```

4. **Update RobotConfig**
   ```java
   // Copy these lines from tune_summary.txt into RobotConfig.java
   public static class Odometry {
       public static double TRACK_WIDTH = 13.42;           // From tuner
       public static double CENTER_WHEEL_OFFSET = 3.68;    // From tuner
   }
   public static class RobotOriented {
       public static double Kp = 0.058;                    // From tuner
       public static double strafe_Kp = 0.062;             // From tuner
       public static double STEER_P = 0.031;               // From tuner
   }
   public static class FieldOriented {
       public static double DEFAULT_FOLLOW_DISTANCE = 9.8; // From tuner
       public static double DEFAULT_MOVE_SPEED = 0.72;     // From tuner
   }
   ```

5. **Redeploy with Tuned Values**
   ```
   Build → Deploy [Your Autonomous OpMode]
   Now ready for autonomous development and competition
   ```

### Creating a New Autonomous OpMode

```java
// Step 1: Extend ROMovementEngine or create custom LinearOpMode
@Autonomous(name = "Blue Left Auto", group = "Autonomous")
public class BlueLeftAuto extends ROMovementEngine {
    
    @Override
    public void runPath() throws InterruptedException {
        // Step 2: Your autonomous logic here
        // Robot is initialized in parent class
        
        // Example: Move forward 24 inches, turn 90°, move right 12 inches
        AnimationBuilder anim = new AnimationBuilder();
        anim.addMovement(0, 24);  // forward
        anim.turn(Math.PI / 2);   // 90° left
        anim.addMovement(0, 12);  // forward again
        anim.execute(robot);
    }
}
```

### Creating a Field-Oriented Autonomous

```java
@Autonomous(name = "Pure Path Auto", group = "Autonomous")
public class PurePathAuto extends LinearOpMode {
    
    @Override
    public void runOpMode() throws InterruptedException {
        // Initialize
        CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
            // ... configuration ...
            .build();
        
        RobotMovement movement = new RobotMovement(robot);
        
        waitForStart();
        
        if (isStopRequested()) return;
        
        // Define waypoint path
        List<Waypoint> path = new ArrayList<>();
        path.add(new Waypoint(0, 0, 0));      // Start
        path.add(new Waypoint(24, 0, 0));     // Move forward
        path.add(new Waypoint(24, 24, 0));    // Turn and move
        path.add(new Waypoint(0, 24, Math.PI)); // Loop back
        
        // Follow it
        movement.follow(path, 0);
        
        robot.stop();
    }
}
```

### Integration Checklist for New Features

- [ ] Does it need hardware? Add to `CrawlerRobot`
- [ ] Does it track position? Integrate with `CrawlerLocaliser`
- [ ] Does it command movement? Use `robot.drive()` or `driveFieldRelative()`
- [ ] Does it need tuning? Add `@Config` class in `RobotConfig`
- [ ] Does it need visualization? Use `DashboardFieldViewUtils`
- [ ] Does it interact with vision? Integrate `AprilTagWebcam`
- [ ] Is it a tunable parameter? Ensure it's in `RobotConfig` (not hardcoded)
- [ ] Is the robot needs recalibration? Document in `CrawlerTuner` § for future tuning

### Calibration Checklist Before Competition

- [ ] Robot hardware builds completed
- [ ] All motors spin correctly (verify via `CrawlerTuner` step 0)
- [ ] IMU reads correctly (verify via `CrawlerTuner` step 1)
- [ ] Run full `CrawlerTuner` (12 steps, save results)
- [ ] Copy tuned values into `RobotConfig.java`
- [ ] Test autonomous paths on practice field
- [ ] Verify integration test passes (`CrawlerTuner` step 10)
- [ ] Re-tune if robot performance degrades

---

## Integration Examples

### Example 1: Simple TeleOp with Field Orientation

```java
@TeleOp(name = "Field Oriented TeleOp", group = "TeleOp")
public class FieldOrientedTeleOp extends LinearOpMode {
    
    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Initialize robot
        CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
            .frontLeft("fl").frontRight("fr")
            .backLeft("bl").backRight("br")
            .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
            .imu("imu")
            .build();
        
        waitForStart();
        
        while (opModeIsActive()) {
            // 2. Read input from gamepad
            double strafe = -gamepad1.left_stick_x;  // A/D keys
            double forward = gamepad1.left_stick_y;  // W/S keys
            double rotate = gamepad1.right_stick_x;  // Left/Right
            
            // 3. Apply field-relative movement
            robot.driveFieldRelative(forward, strafe, rotate);
            
            // 4. Update telemetry
            Pose currPose = robot.localiser.getPose();
            telemetry.addData("X", currPose.getX());
            telemetry.addData("Y", currPose.getY());
            telemetry.addData("Heading (deg)", Math.toDegrees(currPose.getHeading()));
            telemetry.update();
        }
        
        robot.stop();
    }
}
```

### Example 2: Path Following With Vision

```java
@Autonomous(name = "Score + Vision", group = "Autonomous")
public class ScoringAuto extends LinearOpMode {
    
    @Override
    public void runOpMode() throws InterruptedException {
        // 1. Initialize all subsystems
        CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
            // ... robot config ...
            .build();
        
        RobotMovement pathFollower = new RobotMovement(robot);
        AprilTagWebcam vision = new AprilTagWebcam();
        vision.init(hardwareMap, telemetry);
        
        waitForStart();
        
        // 2. Phase 1: Autonomous path
        List<Waypoint> pathToBoard = new ArrayList<>();
        pathToBoard.add(new Waypoint(0, 0, 0));       // Start
        pathToBoard.add(new Waypoint(36, 0, 0));      // Go to board
        
        pathFollower.follow(pathToBoard, 0);
        
        // 3. Phase 2: Vision-aided positioning
        List<AprilTagDetection> detections = vision.getDetections();
        for (AprilTagDetection tag : detections) {
            if (tag.id == 1) {  // Looking for tag 1
                // Align based on tag position
                double error = tag.ftmRbearing;
                // Use error to fine-tune position
            }
        }
        
        // 4. PhaseLinear 3: Return
        List<Waypoint> pathBack = new ArrayList<>();
        pathBack.add(new Waypoint(36, 0, 0));
        pathBack.add(new Waypoint(0, -12, Math.PI));  // Return
        
        pathFollower.follow(pathBack, 0);
        
        robot.stop();
    }
}
```

### Example 3: Real-Time Dashboard Tuning

```java
@Autonomous(name = "Tunable Movement", group = "Tuning")
public class TunableMovement extends LinearOpMode {
    
    @Override
    public void runOpMode() throws InterruptedException {
        CrawlerRobot robot = new CrawlerRobot.Builder(hardwareMap)
            // ... config ...
            .build();
        
        RobotMovement movement = new RobotMovement(robot);
        
        waitForStart();
        
        // Parameters are updated LIVE from FTC Dashboard
        List<Waypoint> path = new ArrayList<>();
        path.add(new Waypoint(0, 0, 0));
        path.add(new Waypoint(
            RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE * 2,
            0,
            0
        ));
        
        while (opModeIsActive()) {
            // Update uses current values from dashboard
            movement.follow(path, 0);
            
            // Display updated parameters
            telemetry.addData("Move Speed (Dashboard)", 
                RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED);
            telemetry.addData("Turn Speed (Dashboard)", 
                RobotConfig.FieldOriented.DEFAULT_TURN_SPEED);
            telemetry.update();
        }
    }
}
```

---

## Troubleshooting Integration Issues

### Problem: Robot drifts in autonomous

**Diagnosis**:
1. Is localization being updated? Check `robot.localiser.getPose()` in telemetry
2. Are encoder ticks increasing? Check encoder values in telemetry
3. Is physical track width correct? Remeasure wheels

**Fix**:
```java
// Add telemetry for debugging
Pose p = robot.localiser.getPose();
telemetry.addData("X", p.getX());
telemetry.addData("Y", p.getY());
telemetry.addData("L Encoder", robot.leftEncoder.getCurrentPosition());
telemetry.addData("R Encoder", robot.rightEncoder.getCurrentPosition());
telemetry.update();
```

### Problem: Path following overshoots waypoints

**Diagnosis**:
- Follow distance too large?
- PID constants wrong?
- Max speed too high?

**Fix**:
```java
// In RobotConfig.FieldOriented:
public static double DEFAULT_FOLLOW_DISTANCE = 5.0;  // Reduce from 10
public static double DEFAULT_MOVE_SPEED = 0.5;       // Reduce from 0.7
```

### Problem: Vision detections not updating

**Diagnosis**:
1. Is camera calibration file present?
2. Is `vision.init()` called before loop?
3. Are AprilTags visible to camera?

**Fix**:
```java
AprilTagWebcam vision = new AprilTagWebcam();
vision.init(hardwareMap, telemetry);  // Required!

// In loop:
vision.updateDetections();
if (vision.getDetections().isEmpty()) {
    telemetry.addData("WARNING", "No AprilTags detected!");
}
```

---

## Summary: How Everything Integrates

```
Developer writes OpMode
    ↓
    ├─→ Creates CrawlerRobot (builder pattern)
    │       ↓
    │       ├─→ Initializes motors, IMU, encoders
    │       └─→ Selects and initializes Localizer
    │
    ├─→ Creates movement system
    │       ↓
    │       ├─→ RobotMovement for field-oriented
    │       └─→ ROMovementEngine for robot-oriented
    │
    ├─→ Creates vision processor (optional)
    │       ↓
    │       └─→ AprilTagWebcam for detection
    │
    └─→ In a loop:
            ├─→ Robot.localiser continuously updates pose
            ├─→ Vision processes images (if enabled)
            ├─→ User code requests movement (follow path or drive)
            ├─→ Movement system converts to motor commands
            ├─→ Motors move, encoders capture motion
            └─→ Pose updates for next iteration
```

All components communicate through the **CrawlerRobot** instance and its **public methods** and **public accessors**. This creates a clean abstraction boundary where:

- **Lower layers** (Hardware, Localization) manage details
- **Middle layers** (Movement) use abstractions
- **Upper layers** (OpModes) focus on behavior and strategy

This separation allows easy testing, swapping components (e.g., different localizers), and scaling to larger projects.

---

## More Information

- **FTC SDK Documentation**: https://ftc-docs.firstinspires.org/
- **FTC Dashboard**: https://acmerobotics.github.io/ftc-dashboard/
- **RoadRunner**: Advanced autonomous library (if used)
- **WPILib**: Similar patterns used by FIRST Robotics

