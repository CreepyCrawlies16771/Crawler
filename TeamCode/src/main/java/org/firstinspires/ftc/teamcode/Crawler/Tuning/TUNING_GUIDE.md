# Crawler Robot - Physical Hardware & Pathing Library Tuning Guide

This document explains the parameters that need to be tuned for the Crawler FTC robot's **physical hardware and pathing library** to work effectively. The tuning is organized by category and includes measurement methods, expected values, and debugging strategies.

**Scope:** This guide covers odometry calibration and movement control (robot-oriented and field-oriented). It does not cover season-specific hardware mechanisms or teleoperator controls.


---

## Table of Contents

1. [Physical Hardware Constants (Odometry)](#physical-hardware-constants-odometry)
2. [Robot-Oriented Movement (PID Control)](#robot-oriented-movement-pid-control)
3. [Field-Oriented Movement (Path Following)](#field-oriented-movement-path-following)
4. [Tuning Workflow](#tuning-workflow)

---

## Physical Hardware Constants (Odometry)

These are physical measurements of the robot that form the foundation for all movement calculations.

**Location:** `RobotConfig.Odometry` class

### Parameters to Tune

#### 1. **TRACK_WIDTH** (inches)
- **Current Value:** 13.0
- **What It Is:** The distance between the left and right odometry wheels
- **Why It Matters:** Used to calculate rotation radius; incorrect values cause drift when turning
- **How to Measure:**
  1. Place the robot on a flat surface
  2. Mark the center point of the left odometry wheel
  3. Mark the center point of the right odometry wheel
  4. Measure the distance between these two points (in inches)
  5. Verify by measuring multiple times at different points along the wheel
- **Tuning Signs:**
  - Robot drifts left/right during straight movement → needs adjustment
  - Turns are not circular → TRACK_WIDTH is incorrect
  - Robot spins more/less than expected → directly affects turn accuracy
- **Expected Range:** 10.0 - 20.0 inches (typical for FTC robots)

#### 2. **CENTER_WHEEL_OFFSET** (inches)
- **Current Value:** 3.5
- **What It Is:** The forward/backward distance from the center of the robot to the center odometry wheel
- **Why It Matters:** Used to correct heading during strafe movements; incorrect values cause spinning while strafing
- **How to Measure:**
  1. Identify the robot's geometric center (midpoint between left/right wheels)
  2. Find the center of the forward odometry wheel
  3. Measure the forward distance from robot center to the forward wheel center
  4. Measure in inches along the robot's forward axis
- **Tuning Signs:**
  - Robot rotates while strafing → CENTER_WHEEL_OFFSET is incorrect
  - Straight lines curve unexpectedly → check this value
  - Pure pursuit paths have systematic curvature → likely needs tuning
- **Expected Range:** 2.0 - 6.0 inches (depending on robot design)

#### 3. **WHEEL_DIAMETER** (inches)
- **Current Value:** 1.37795 (35mm pod wheels)
- **What It Is:** The diameter of the odometry wheel pods
- **Why It Matters:** Converts encoder ticks into distance measurements; critical for all distance calculations
- **How to Measure:**
  1. Remove the odometry wheel pod from the robot
  2. Measure the diameter with calipers or ruler (in inches)
  3. If using standard parts, verify against manufacturer specs
  4. Common values: 
     - 1.37795 inches (35mm wheels)
     - 0.984 inches (25mm wheels)
     - 1.968 inches (50mm wheels)
- **Tuning Signs:**
  - Robot moves more/less distance than commanded → incorrect WHEEL_DIAMETER
  - PID overshoots/undershoots consistently → likely a scaling issue from this value
  - Autonomous paths are significantly off distance → measure this first
- **Formula for calculation:** If using custom wheels, measure the circumference and divide by π

#### 4. **TICKS_PER_REV** (encoder counts)
- **Current Value:** 2000
- **What It Is:** Number of encoder ticks per complete wheel revolution
- **Why It Matters:** Converts raw encoder counts into useful measurements
- **How to Find:**
  1. Check the encoder/motor datasheet
  2. Common values for FTC:
     - 2000 ticks (GoBILDA 5203 motors)
     - 560 ticks (REV motors)
     - 1440 ticks (some other motors)
  3. Verify by spinning the wheel manually and reading encoder output
- **Tuning Signs:**
  - Distance calculations are consistently wrong by a fixed ratio → wrong TICKS_PER_REV
  - If TICKS_PER_REV is wrong, WHEEL_DIAMETER compensation won't help

### Combined Effect: TICKS_PER_METER

The system calculates:
```
TICKS_PER_METER = TICKS_PER_REV / (WHEEL_DIAMETER × π)
```

If this is wrong, all distance-based movements fail. Use this formula to verify your odometry setup.

---

## Robot-Oriented Movement (PID Control)

These parameters control how the robot responds to movement commands in robot-relative coordinates (used primarily in autonomous programs with `drivePID()`, `strafePID()`, `turnPID()`).

**Location:** `RobotConfig.RobotOriented` class

### Drive Movement PID (Forward/Backward)

#### **Kp** (Proportional Gain)
- **Current Value:** 0.05
- **What It Controls:** How aggressively the robot responds to distance error
- **Formula:** `power = Kp × (error / TICKS_PER_METER)`
- **Tuning Guide:**
  - **Too Low (< 0.03):** Robot moves slowly, undershoots target
  - **Optimal (0.05 - 0.15):** Reaches target smoothly in 1-2 seconds
  - **Too High (> 0.2):** Robot oscillates, jerky movement, overshoots
- **How to Tune:**
  1. Start with 0.05
  2. Run `drivePID(1.0, 0)` (move 1 meter forward)
  3. Observe behavior:
     - Undershoots → increase Kp
     - Overshoots → decrease Kp
     - Oscillates → may need Kd instead
  4. Adjust in 0.01 increments
  5. Test multiple distances (1m, 2m) to ensure stability

#### **Ki** (Integral Gain)
- **Current Value:** 0.0
- **What It Controls:** Accumulates small errors over time to eliminate steady-state error
- **When to Use:** If robot consistently stops slightly short of target
- **Tuning Guide:**
  - Start at 0.0, add only if Kp alone isn't sufficient
  - Typical range: 0.001 - 0.005
  - Too high Ki causes oscillation and integrator windup
- **How to Tune:**
  1. First, tune Kp until movement is mostly correct
  2. If robot stops 2-3 inches short consistently, add Ki
  3. Start with Ki = 0.001
  4. Test with `drivePID(1.0, 0)` multiple times
  5. Watch telemetry for integral accumulation
  6. Anti-windup: Ki only activates when error < 10% of target distance

#### **Kd** (Derivative Gain)
- **Current Value:** 0.0
- **What It Controls:** Dampens rapid changes; prevents oscillation
- **When to Use:** If robot oscillates around target
- **Tuning Guide:**
  - Start at 0.0
  - Typical range: 0.0 - 0.1
  - Add if movement is jerky or overshoots heavily
- **How to Tune:**
  1. Set Kp first (tune until slight oscillation)
  2. Add Kd = 0.01
  3. Increase Kd until oscillation stops (usually 0.02 - 0.05)
  4. Too high Kd makes motor control sluggish

### Strafe Movement PID (Left/Right)

Strafe typically requires different PID values because sideways wheels have different friction and grip characteristics.

#### **strafe_Kp**
- **Current Value:** 0.05
- **Why Different:** Sideways motion may have more/less friction than forward
- **Tuning Method:** Same as Kp, but run `strafePID(1.0, 0)` instead
- **Expected Value:** Often 1.5-2.5x higher than drive Kp (e.g., 0.08 - 0.15)

#### **strafe_Ki**
- **Current Value:** 0.0
- **Tuning:** Same principles as drive Ki, but for strafe errors
- **Expected Value:** Often higher than drive Ki (e.g., 0.0002 - 0.001)

#### **strafe_Kd**
- **Current Value:** 0.0
- **Tuning:** Same as drive Kd, add if oscillation occurs

### Turn Control PID

#### **STEER_P** (Steering Proportional Gain)
- **Current Value:** 0.03
- **What It Controls:** Heading correction during forward/strafe movement
- **Formula:** `steer_power = STEER_P × (heading_error)`
- **Tuning Guide:**
  - **Too Low (< 0.01):** Robot doesn't correct heading; drifts
  - **Optimal (0.02 - 0.04):** Maintains heading smoothly
  - **Too High (> 0.06):** Wobbles side-to-side
- **How to Tune:**
  1. Run `drivePID(1.0, 45)` (move forward while maintaining 45-degree heading)
  2. Observe robot's path:
     - Drifts away from target heading → increase STEER_P
     - Wobbles left/right → decrease STEER_P
  3. Check telemetry for heading error and correction power
  4. Adjust in 0.005 increments

#### **STEER_I** (Steering Integral)
- **Current Value:** 0.0
- **When to Use:** If robot consistently drifts despite STEER_P tuning
- **Tuning:** Add small value (0.0001 - 0.001) if needed

#### **STEER_D** (Steering Derivative)
- **Current Value:** 0.0
- **When to Use:** If steering oscillates
- **Tuning:** Add small value (0.01 - 0.05) if wobbling occurs

### Minimum Power Threshold

#### **MIN_POWER**
- **Current Value:** 0.15
- **What It Controls:** Minimum motor power required to overcome static friction and start movement
- **Tuning Guide:**
  - **Too Low:** Motor may not move despite command
  - **Too High:** Robot moves jerkily even at small error values
  - **Optimal:** Lowest power where motor consistently starts moving
- **How to Tune:**
  1. Lower MIN_POWER to 0.1
  2. Run `drivePID(0.1, 0)` (move only 10cm)
  3. If robot doesn't move, increase MIN_POWER to 0.15
  4. If jerky, decrease MIN_POWER to 0.12
  5. Test at different motor power levels (e.g., 0.08, 0.12, 0.15)

---

## Field-Oriented Movement (Path Following)

These parameters control the high-level path following behavior in autonomous using `FOFollower` and `Waypoint`.

**Location:** `RobotConfig.FieldOriented` class

### Default Speed Parameters

#### **DEFAULT_MOVE_SPEED**
- **Current Value:** 0.7
- **What It Controls:** Default power sent to wheels during normal waypoint transit
- **Range:** 0.0 - 1.0
- **Tuning Considerations:**
  - Lower (0.4 - 0.6): Smoother, more precise paths, slower cycle time
  - Higher (0.7 - 0.9): Faster paths, less precise, may overshoot waypoints
  - Match with robot weight and wheel grip
- **How to Tune:**
  1. Use 0.7 as starting point
  2. Run a simple autonomous path with 3-4 waypoints
  3. Observe overshooting:
     - Overshoots significantly → reduce to 0.6
     - Undershoots or too slow → increase to 0.8
  4. Target: Robot reaches waypoint smoothly without overshooting

#### **DEFAULT_TURN_SPEED**
- **Current Value:** 0.4
- **What It Controls:** Power used for heading correction during movement
- **Tuning Guide:**
  - Should be lower than DEFAULT_MOVE_SPEED (0.3 - 0.5 typical)
  - Too high: Jerky heading corrections
  - Too low: Doesn't correct heading properly
- **How to Tune:**
  1. Start with 0.4
  2. Run path with sharp turns between waypoints
  3. Adjust to achieve smooth turning without wobble

### Follow Distance (Pure Pursuit Look-Ahead)

#### **DEFAULT_FOLLOW_DISTANCE** (inches)
- **Current Value:** 10.0
- **What It Controls:** How far ahead the robot looks when calculating steering
- **Impact:**
  - **Too Small (< 5):** Robot follows waypoint path closely but jerks at waypoints
  - **Optimal (8-12):** Smooth curves that transition between waypoints
  - **Too Large (> 15):** Robot cuts corners and misses waypoints
- **How to Tune:**
  1. Start with 10.0
  2. Create a path with a 90-degree corner
  3. Observe corner behavior:
     - Cuts corner sharply → decrease to 8.0
     - Overshoots waypoint → increase to 12.0
  4. Test with different path curvatures

#### **DEFAULT_FOLLOW_ANGLE** (radians)
- **Current Value:** 0.0
- **What It Controls:** Angle offset for look-ahead point (usually not needed)
- **Typical Value:** 0.0 (keep as-is)

### Arrival Detection

#### **ARRIVAL_THRESHOLD** (inches)
- **Current Value:** 2.0
- **What It Controls:** Distance at which a waypoint is considered "reached"
- **Tuning Considerations:**
  - **Too Small (< 1):** May never trigger, robot keeps trying to reach exact point
  - **Too Large (> 3):** Triggers early, skips waypoint actions
  - **Optimal:** 1.5 - 2.5 inches
- **How to Tune:**
  1. Start with 2.0
  2. Run autonomous, watch telemetry for arrival detection
  3. If onReach() callbacks trigger too early → decrease to 1.5
  4. If callbacks don't trigger → increase to 2.5 or check odometry calibration

### Orbit and Slow-Down Thresholds

#### **ORBIT_THRESHOLD** (inches)
- **Current Value:** 10.0
- **What It Controls:** Distance from waypoint where turning begins to fade out
- **Impact:** Smooth transition from forward movement to pure turning at waypoint
- **Tuning:** Usually keep at 1.5x ARRIVAL_THRESHOLD (e.g., if ARRIVAL_THRESHOLD = 2.0, set to 3.0)

#### **DEFAULT_SLOW_DOWN_TURN_AMOUNT**
- **Current Value:** 0.5
- **What It Controls:** Factor by which movement speed reduces as robot approaches waypoint
- **Formula:** `adjusted_speed = move_speed × (1 - slow_down_amount)`
- **Tuning:** Range 0.3 - 0.7; higher values slow down more aggressively

#### **DEFAULT_SLOW_DOWN_TURN_RADIANS** 
- **Current Value:** 0.5
- **What It Controls:** Heading error (in radians) where slow-down activates
- **Tuning:** Range 0.3 - 1.0 radians (~17° - 57°)

### Slow Movement Mode

These parameters are used when `.slow()` is called on a waypoint.

#### **SLOW_MOVE_SPEED**
- **Current Value:** 0.3
- **What It Controls:** Reduced speed for precision movements (e.g., scoring)
- **Typical Use:** Scoring actions where accuracy matters more than speed

#### **SLOW_TURN_SPEED**
- **Current Value:** 0.2
- **What It Controls:** Reduced heading correction speed in slow mode

#### **SLOW_FOLLOW_DISTANCE**
- **Current Value:** 5.0
- **What It Controls:** Reduced look-ahead distance for tighter path tracking in slow mode

---

## Tuning Workflow

This section describes the recommended sequence and tools for tuning the robot.

### Phase 1: Physical Hardware Validation (Before Any Tuning)

1. **Verify Odometry Wheel Calibration:**
   - Measure TRACK_WIDTH and CENTER_WHEEL_OFFSET physically
   - Record motor encoder values when wheels spin 360°
   - Calculate TICKS_PER_REV if not known
   - **Telemetry Tool:** Print raw encoder values and calculate TICKS_PER_METER

2. **Verify Motor Wiring:**
   - Test each motor direction
   - Ensure IMU is mounted correctly
   - Check that all encoders are connected

### Phase 2: Odometry Calibration (Critical First Step)

Use the FTC Dashboard or telemetry to verify odometry is working:

```
Run ExampleAuto or simple movement test:
1. Mark starting position on field
2. Run robot forward 1 meter
3. Check odometry reading vs. actual distance
4. If off, calculate correction factor: actual_distance / reported_distance
5. Apply correction to WHEEL_DIAMETER or TICKS_PER_METER
```

### Phase 3: Robot-Oriented PID Tuning

Use `TuneRobotOrientPID` OpMode (in Tuning folder) for systematic tuning:

1. **Tune Drive (Kp):**
   - Run forward tune, test 1-meter movements
   - Adjust Kp until reaching target smoothly
   - Record final Kp value

2. **Tune Strafe (strafe_Kp):**
   - Run side tune, test 1-meter strafes
   - Adjust strafe_Kp independently
   - Record final value

3. **Tune Turn (STEER_P):**
   - Run turn tune, test heading maintenance during movement
   - Adjust STEER_P until smooth heading control
   - Record final value

4. **Fine-Tune with Ki/Kd if Needed:**
   - Only add if Kp alone insufficient
   - Add Ki if undershooting consistently
   - Add Kd if overshooting/oscillating

### Phase 4: Validation and Testing

1. **Run Full Autonomous Programs:**
   - Test ExampleAuto with tuned values
   - Run multiple laps to verify consistency
   - Check for drift over multiple runs

2. **Stress Test with Payload:**
   - Run with game elements
   - Test movement accuracy under load
   - Verify odometry accuracy with heavy robot

3. **Document All Values:**
   - Create a tuning log with all final values
   - Note any special conditions (battery voltage, field surface, etc.)
   - Store backup of tuned RobotConfig.java

---

## Tuning Checklist

- [ ] TRACK_WIDTH measured and verified
- [ ] CENTER_WHEEL_OFFSET measured and verified  
- [ ] WHEEL_DIAMETER verified against motor specs
- [ ] TICKS_PER_REV confirmed
- [ ] Odometry validation test passed (within 2% error)
- [ ] Drive Kp tuned (smooth 1m movement)
- [ ] Strafe Kp tuned (smooth 1m strafe)
- [ ] STEER_P tuned (smooth heading maintenance)
- [ ] DEFAULT_MOVE_SPEED optimized (0.6 - 0.8)
- [ ] DEFAULT_TURN_SPEED optimized (0.3 - 0.5)
- [ ] DEFAULT_FOLLOW_DISTANCE tuned (8.0 - 12.0)
- [ ] ARRIVAL_THRESHOLD set (1.5 - 2.5)
- [ ] Full autonomous path tested
- [ ] Multi-lap consistency verified
- [ ] Payload testing completed
- [ ] Final values documented

---

## Common Issues and Solutions

| Issue | Likely Cause | Solution |
|-------|-------------|----------|
| Robot drifts left/right during forward movement | TRACK_WIDTH incorrect | Re-measure track width |
| Robot rotates while strafing | CENTER_WHEEL_OFFSET incorrect | Re-measure center wheel offset |
| All distances 30% too short | WHEEL_DIAMETER too large | Reduce WHEEL_DIAMETER or reduce TICKS_PER_REV |
| Robot overshoots target by large margin | Kp too high | Decrease Kp in 0.02 increments |
| Robot oscillates around target | Kp too high or Kd missing | Decrease Kp or add Kd (start with 0.02) |
| Robot stops short of target | Kp too low or MIN_POWER too high | Increase Kp or decrease MIN_POWER |
| Autonomous path has curved paths instead of straight | TRACK_WIDTH, CENTER_WHEEL_OFFSET, or odometry incorrect | Verify odometry calibration |
| Waypoints overshoot | DEFAULT_MOVE_SPEED too high | Reduce to 0.6 |
| Waypoints unreached or very slow | DEFAULT_MOVE_SPEED too low | Increase to 0.8 |
| Corner-cutting between waypoints | DEFAULT_FOLLOW_DISTANCE too large | Reduce to 8.0 |
| Jerky path following | DEFAULT_FOLLOW_DISTANCE too small | Increase to 12.0 |

---

## Tips for Efficient Tuning

1. **Use FTC Dashboard:** Real-time config changes without recompiling
2. **Enable Telemetry:** Print error values, power output, and encoder readings
3. **Test Incrementally:** Make one change at a time, test, then adjust
4. **Document Everything:** Keep detailed notes of changes and results
5. **Test Multiple Scenarios:** Different distances, speeds, and directions
6. **Use the Tuning OpMode:** `TuneRobotOrientPID` is designed specifically for PID tuning
7. **Battery Voltage:** Tune when battery is at competition voltage (12-13V)
8. **Repeat on Competition Field:** Final tuning on actual field if possible
9. **Team Communication:** Share tuned values with entire programming team
10. **Backup Values:** Save tuned config before major changes

