# Crawler Robot - Tuning OpMode Implementation Guide

This guide explains how to **implement tuning opmodes** to systematically calibrate the Crawler FTC robot's physical hardware and pathing library. Rather than manually adjusting code and recompiling, these opmodes provide interactive real-time tuning via telemetry and gamepad controls.

**What You'll Learn:**
- How to structure a tuning opmode
- Interactive parameter adjustment with gamepad controls
- Real-time testing and validation
- Saving tuned values persistently
- Testing procedures for each parameter

---

## Table of Contents

1. [Core Architecture](#core-architecture)
2. [Odometry Calibration OpMode](#odometry-calibration-opmode)
3. [Robot-Oriented PID Tuning OpMode](#robot-oriented-pid-tuning-opmode)
4. [Field-Oriented Path Following OpMode](#field-oriented-path-following-opmode)
5. [Utilities and Testing](#utilities-and-testing)
6. [Complete Workflow](#complete-workflow)

---

## Core Architecture

### OpMode Structure Pattern

Every tuning opmode follows this pattern:

```java
@TeleOp(name = "Tune: [Component]", group = "Crawler Tuning")
public class TuneComponentName extends LinearOpMode {
    
    // 1. Robot and control objects
    private CrawlerRobot robot;
    private Telemetry dashboardTelemetry;
    private FtcDashboard dashboard;
    
    // 2. Tuning state
    private double currentValue;
    private double stepSize;
    private int testState = 0;
    
    @Override
    public void runOpMode() throws InterruptedException {
        initializeRobot();
        waitForStart();
        
        while (opModeIsActive()) {
            updateTelemetry();
            handleGamepadInput();
            performCurrentTest();
        }
    }
    
    private void initializeRobot() {
        robot = new MyRobot(hardwareMap);
        dashboard = FtcDashboard.getInstance();
        dashboardTelemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
    }
    
    private void updateTelemetry() {
        dashboardTelemetry.addData("Current Value", currentValue);
        dashboardTelemetry.addData("Step Size", stepSize);
        dashboardTelemetry.update();
    }
    
    private void handleGamepadInput() {
        if (gamepad1.dpad_up) {
            currentValue += stepSize;
            sleep(100);  // Debounce
        }
        if (gamepad1.dpad_down) {
            currentValue -= stepSize;
            sleep(100);
        }
        if (gamepad1.circle) {
            // Save and advance
            saveValue(currentValue);
            testState++;
        }
    }
    
    private void performCurrentTest() {
        switch (testState) {
            case 0:
                testOne();
                break;
            case 1:
                testTwo();
                break;
            // ... etc
        }
    }
    
    private void saveValue(double value) {
        // Write to RobotConfig and/or file
    }
}
```

### Key Principles

1. **Real-Time Updates:** Use FTC Dashboard for live parameter changes without recompilation
2. **Validation Testing:** Each parameter has a specific test that shows if tuning is correct
3. **Sequential Steps:** Force teams to tune in the correct order (prevents missing steps)
4. **Persistent Storage:** Save values to files so they survive app restarts

---

## Odometry Calibration OpMode

This opmode calibrates the physical measurements: TRACK_WIDTH, CENTER_WHEEL_OFFSET, WHEEL_DIAMETER, TICKS_PER_REV.

### Part 1: Motor Verification

```java
@TeleOp(name = "Tune: Odometry - Motor Test", group = "Crawler Tuning")
public class TuneOdometryMotors extends LinearOpMode {
    private CrawlerRobot robot;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        waitForStart();
        
        telemetry.addLine("=== Motor Verification ===");
        telemetry.addLine("RB: Spin FL  |  LB: Spin FR  |  RT: Spin BL  |  LT: Spin BR");
        telemetry.update();
        
        while (opModeIsActive()) {
            // Spin motors individually for visual inspection
            if (gamepad1.right_bumper) {
                robot.frontLeft.set(0.5);
                telemetry.addData("Spinning", "Front Left");
            } else {
                robot.frontLeft.set(0);
            }
            
            if (gamepad1.left_bumper) {
                robot.frontRight.set(0.5);
                telemetry.addData("Spinning", "Front Right");
            } else {
                robot.frontRight.set(0);
            }
            
            if (gamepad1.right_trigger > 0.1) {
                robot.backLeft.set(gamepad1.right_trigger);
                telemetry.addData("Spinning", "Back Left @ " + gamepad1.right_trigger);
            } else {
                robot.backLeft.set(0);
            }
            
            if (gamepad1.left_trigger > 0.1) {
                robot.backRight.set(gamepad1.left_trigger);
                telemetry.addData("Spinning", "Back Right @ " + gamepad1.left_trigger);
            } else {
                robot.backRight.set(0);
            }
            
            telemetry.addData("Status", "All motors spinning correctly? Press X to proceed");
            telemetry.update();
        }
    }
}
```

### Part 2: Wheel Diameter & Ticks Per Rev

```java
@TeleOp(name = "Tune: Odometry - Encoder Baseline", group = "Crawler Tuning")
public class TuneEncoderBaseline extends LinearOpMode {
    private CrawlerRobot robot;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        waitForStart();
        
        telemetry.addLine("=== Encoder Tick Count ===");
        telemetry.addLine("Spin each wheel exactly 1 complete revolution.");
        telemetry.addLine("Record the encoder tick count below.");
        telemetry.update();
        
        long ticksL = 0, ticksR = 0, ticksC = 0;
        
        while (opModeIsActive()) {
            // Display current encoder values
            if (robot.leftEncoder != null) {
                ticksL = robot.leftEncoder.getCurrentPosition();
                telemetry.addData("Left Encoder Ticks", ticksL);
            }
            if (robot.rightEncoder != null) {
                ticksR = robot.rightEncoder.getCurrentPosition();
                telemetry.addData("Right Encoder Ticks", ticksR);
            }
            if (robot.centerEncoder != null) {
                ticksC = robot.centerEncoder.getCurrentPosition();
                telemetry.addData("Center Encoder Ticks", ticksC);
            }
            
            // Manual spin test
            if (gamepad1.right_bumper) {
                robot.frontLeft.set(0.2);
                robot.frontRight.set(0.2);
                telemetry.addLine("Spinning wheels - turn exactly 1 revolution");
            } else {
                robot.frontLeft.set(0);
                robot.frontRight.set(0);
            }
            
            if (gamepad1.circle) {
                telemetry.addLine("Record these tick values in RobotConfig.TICKS_PER_REV");
                telemetry.addLine("If measured ticks != expected, recalibrate WHEEL_DIAMETER");
                telemetry.update();
                sleep(3000);
            }
            
            telemetry.update();
        }
    }
}
```

### Part 3: Track Width Calibration

```java
@TeleOp(name = "Tune: Odometry - Track Width", group = "Crawler Tuning")
public class TuneTrackWidth extends LinearOpMode {
    private CrawlerRobot robot;
    private double trackWidth = RobotConfig.Odometry.TRACK_WIDTH;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        
        waitForStart();
        
        while (opModeIsActive()) {
            telemetry.addLine("=== Track Width Tuning ===");
            telemetry.addLine("Robot will spin 10 complete rotations.");
            telemetry.addData("Current TRACK_WIDTH", trackWidth);
            telemetry.addLine("Up/Down: adjust | RB: run test | Circle: save & exit");
            
            // Gamepad input for adjustment
            if (gamepad1.dpad_up) {
                trackWidth += 0.1;
                sleep(100);
            }
            if (gamepad1.dpad_down) {
                trackWidth -= 0.1;
                sleep(100);
            }
            
            // Run test: spin robot 10 times
            if (gamepad1.right_bumper) {
                runTrackWidthTest(trackWidth);
            }
            
            // Save
            if (gamepad1.circle) {
                RobotConfig.Odometry.TRACK_WIDTH = trackWidth;
                telemetry.addLine("TRACK_WIDTH saved!");
                telemetry.update();
                sleep(1000);
                break;
            }
            
            telemetry.update();
        }
    }
    
    private void runTrackWidthTest(double trackWidth) throws InterruptedException {
        telemetry.addLine("Running 10 rotations...");
        telemetry.addData("Expected heading after 10 rotations", "3600°");
        telemetry.update();
        
        // Reset IMU
        robot.imu.resetYaw();
        
        // Spin 10 times (3600 degrees)
        double targetHeading = 3600;
        while (opModeIsActive() && Math.abs(robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES)) < targetHeading) {
            robot.frontLeft.set(0.3);
            robot.backLeft.set(0.3);
            robot.frontRight.set(-0.3);
            robot.backRight.set(-0.3);
            
            double currentHeading = robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            telemetry.addData("Current Heading", currentHeading);
            telemetry.update();
        }
        
        // Stop and read final heading
        robot.frontLeft.set(0);
        robot.backLeft.set(0);
        robot.frontRight.set(0);
        robot.backRight.set(0);
        
        double finalHeading = robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double error = Math.abs(finalHeading - 0);  // Should be back at 0 after 10 rotations
        
        telemetry.addData("Final Heading", finalHeading);
        telemetry.addData("Heading Error", error);
        if (error < 5) {
            telemetry.addLine("✓ TRACK_WIDTH is correct!");
        } else if (error > 5 && error < 20) {
            telemetry.addLine("△ TRACK_WIDTH is close, minor adjustment needed");
        } else {
            telemetry.addLine("✗ TRACK_WIDTH is way off, increase and try again");
        }
        telemetry.update();
        sleep(3000);
    }
}
```

### Part 4: Center Wheel Offset Calibration

```java
@TeleOp(name = "Tune: Odometry - Center Wheel Offset", group = "Crawler Tuning")
public class TuneCenterWheelOffset extends LinearOpMode {
    private CrawlerRobot robot;
    private double centerWheelOffset = RobotConfig.Odometry.CENTER_WHEEL_OFFSET;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        
        waitForStart();
        
        while (opModeIsActive()) {
            telemetry.addLine("=== Center Wheel Offset Tuning ===");
            telemetry.addLine("Robot will strafe 1 meter and measure Y drift.");
            telemetry.addData("Current CENTER_WHEEL_OFFSET", centerWheelOffset);
            telemetry.addLine("Up/Down: adjust | RB: run test | Circle: save & exit");
            
            if (gamepad1.dpad_up) {
                centerWheelOffset += 0.1;
                sleep(100);
            }
            if (gamepad1.dpad_down) {
                centerWheelOffset -= 0.1;
                sleep(100);
            }
            
            if (gamepad1.right_bumper) {
                runCenterWheelTest(centerWheelOffset);
            }
            
            if (gamepad1.circle) {
                RobotConfig.Odometry.CENTER_WHEEL_OFFSET = centerWheelOffset;
                telemetry.addLine("CENTER_WHEEL_OFFSET saved!");
                telemetry.update();
                sleep(1000);
                break;
            }
            
            telemetry.update();
        }
    }
    
    private void runCenterWheelTest(double centerWheelOffset) throws InterruptedException {
        telemetry.addLine("Strafing left 1 meter...");
        telemetry.addLine("Robot should return to same X position with 0° rotation.");
        telemetry.update();
        
        // Reset localiser
        robot.update();
        Pose2d startPose = robot.getPose();
        
        // Strafe left (negative X) for 1 meter
        while (opModeIsActive()) {
            robot.update();
            Pose2d currentPose = robot.getPose();
            double distanceTraveled = Math.abs(currentPose.getY() - startPose.getY());
            
            if (distanceTraveled >= 100) {  // 100 cm = 1 meter
                break;
            }
            
            robot.driveRobotRelative(-0.5, 0, 0);  // Strafe left
            
            telemetry.addData("Distance", distanceTraveled);
            telemetry.addData("Rotation", currentPose.getHeading());
            telemetry.update();
        }
        
        robot.driveRobotRelative(0, 0, 0);
        robot.update();
        Pose2d endPose = robot.getPose();
        
        double headingDrift = Math.abs(endPose.getHeading());
        
        telemetry.addData("Final Heading", headingDrift);
        if (headingDrift < 2) {
            telemetry.addLine("✓ CENTER_WHEEL_OFFSET is correct!");
        } else if (headingDrift < 5) {
            telemetry.addLine("△ Minor adjustment needed");
        } else {
            telemetry.addLine("✗ Offset is way off, adjust and retry");
        }
        telemetry.update();
        sleep(3000);
    }
}
```

---

## Robot-Oriented PID Tuning OpMode

This opmode tunes the PID constants for robot-oriented movement (drive, strafe, turn).

```java
@TeleOp(name = "Tune: PID - Robot Oriented", group = "Crawler Tuning")
public class TuneRobotOrientedPID extends LinearOpMode {
    private CrawlerRobot robot;
    private Telemetry dashboardTelemetry;
    
    private enum TuneMode {
        DRIVE, STRAFE, TURN
    }
    
    private TuneMode currentMode = TuneMode.DRIVE;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        FtcDashboard dashboard = FtcDashboard.getInstance();
        dashboardTelemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        
        waitForStart();
        
        while (opModeIsActive()) {
            displayCurrentMode();
            
            // Mode selection
            if (gamepad1.x) {
                currentMode = TuneMode.values()[(currentMode.ordinal() + 1) % TuneMode.values().length];
                sleep(300);
            }
            
            // Run test
            if (gamepad1.right_bumper) {
                switch (currentMode) {
                    case DRIVE:
                        testDrivePID();
                        break;
                    case STRAFE:
                        testStrafePID();
                        break;
                    case TURN:
                        testTurnPID();
                        break;
                }
            }
            
            dashboardTelemetry.update();
        }
    }
    
    private void displayCurrentMode() {
        dashboardTelemetry.addLine("=== Robot-Oriented PID Tuning ===");
        dashboardTelemetry.addData("Mode", currentMode);
        dashboardTelemetry.addLine("X: cycle mode | RB: run test");
        
        switch (currentMode) {
            case DRIVE:
                dashboardTelemetry.addData("Drive Kp", RobotConfig.RobotOriented.Kp);
                dashboardTelemetry.addData("Drive Ki", RobotConfig.RobotOriented.Ki);
                dashboardTelemetry.addData("Drive Kd", RobotConfig.RobotOriented.Kd);
                break;
            case STRAFE:
                dashboardTelemetry.addData("Strafe Kp", RobotConfig.RobotOriented.strafe_Kp);
                dashboardTelemetry.addData("Strafe Ki", RobotConfig.RobotOriented.strafe_Ki);
                dashboardTelemetry.addData("Strafe Kd", RobotConfig.RobotOriented.strafe_Kd);
                break;
            case TURN:
                dashboardTelemetry.addData("STEER_P", RobotConfig.RobotOriented.STEER_P);
                break;
        }
    }
    
    private void testDrivePID() throws InterruptedException {
        dashboardTelemetry.addLine("Testing: Drive 1 meter forward");
        dashboardTelemetry.update();
        
        // Robot will drive 1 meter using PID
        // Display telemetry: position, error, power
        // Let teams observe behavior and adjust Kp/Ki/Kd via FTC Dashboard
        
        robot.update();
        Pose2d startPose = robot.getPose();
        
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < 5) {
            robot.update();
            Pose2d currentPose = robot.getPose();
            double distanceTraveled = Math.hypot(
                currentPose.getX() - startPose.getX(),
                currentPose.getY() - startPose.getY()
            );
            
            dashboardTelemetry.addData("Distance (cm)", distanceTraveled);
            dashboardTelemetry.addData("Error (cm)", 100 - distanceTraveled);
            dashboardTelemetry.addData("Kp", RobotConfig.RobotOriented.Kp);
            
            robot.driveRobotRelative(0.5, 0, 0);  // Drive forward
            dashboardTelemetry.update();
        }
        
        robot.driveRobotRelative(0, 0, 0);
        dashboardTelemetry.addLine("Test complete. Adjust values and try again.");
        dashboardTelemetry.update();
        sleep(2000);
    }
    
    private void testStrafePID() throws InterruptedException {
        dashboardTelemetry.addLine("Testing: Strafe 1 meter right");
        dashboardTelemetry.update();
        
        robot.update();
        Pose2d startPose = robot.getPose();
        
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && timer.seconds() < 5) {
            robot.update();
            Pose2d currentPose = robot.getPose();
            double distanceTraveled = Math.abs(currentPose.getX() - startPose.getX());
            
            dashboardTelemetry.addData("Distance (cm)", distanceTraveled);
            dashboardTelemetry.addData("Error (cm)", 100 - distanceTraveled);
            dashboardTelemetry.addData("Heading Drift", currentPose.getHeading());
            dashboardTelemetry.addData("strafe_Kp", RobotConfig.RobotOriented.strafe_Kp);
            
            robot.driveRobotRelative(0, 0.5, 0);  // Strafe right
            dashboardTelemetry.update();
        }
        
        robot.driveRobotRelative(0, 0, 0);
        dashboardTelemetry.addLine("Test complete.");
        dashboardTelemetry.update();
        sleep(2000);
    }
    
    private void testTurnPID() throws InterruptedException {
        dashboardTelemetry.addLine("Testing: Turn 90 degrees");
        dashboardTelemetry.update();
        
        double targetHeading = 90;
        ElapsedTime timer = new ElapsedTime();
        
        while (opModeIsActive() && timer.seconds() < 5) {
            robot.update();
            double currentHeading = robot.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double headingError = targetHeading - currentHeading;
            
            dashboardTelemetry.addData("Target Heading", targetHeading);
            dashboardTelemetry.addData("Current Heading", currentHeading);
            dashboardTelemetry.addData("Error", headingError);
            dashboardTelemetry.addData("STEER_P", RobotConfig.RobotOriented.STEER_P);
            
            robot.driveRobotRelative(0, 0, 0.3);  // Rotate
            dashboardTelemetry.update();
        }
        
        robot.driveRobotRelative(0, 0, 0);
        dashboardTelemetry.addLine("Test complete.");
        dashboardTelemetry.update();
        sleep(2000);
    }
}
```

---

## Field-Oriented Path Following OpMode

This opmode tunes the field-oriented path following parameters (speed, follow distance, arrival threshold).

```java
@TeleOp(name = "Tune: Field Oriented - Path Following", group = "Crawler Tuning")
public class TuneFieldOrientedPath extends LinearOpMode {
    private CrawlerRobot robot;
    private FOFollower follower;
    private Telemetry dashboardTelemetry;
    
    @Override
    public void runOpMode() throws InterruptedException {
        robot = new MyRobot(hardwareMap);
        follower = new FOFollower(robot, telemetry, this::opModeIsActive);
        
        FtcDashboard dashboard = FtcDashboard.getInstance();
        dashboardTelemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
        
        waitForStart();
        
        while (opModeIsActive()) {
            displayParameters();
            
            if (gamepad1.right_bumper) {
                testSquarePath();
            }
            
            dashboardTelemetry.update();
        }
    }
    
    private void displayParameters() {
        dashboardTelemetry.addLine("=== Field-Oriented Path Following ===");
        dashboardTelemetry.addData("Move Speed", RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED);
        dashboardTelemetry.addData("Turn Speed", RobotConfig.FieldOriented.DEFAULT_TURN_SPEED);
        dashboardTelemetry.addData("Follow Distance (in)", RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE);
        dashboardTelemetry.addData("Arrival Threshold (in)", RobotConfig.FieldOriented.ARRIVAL_THRESHOLD);
        dashboardTelemetry.addLine("RB: Run square path test");
    }
    
    private void testSquarePath() throws InterruptedException {
        dashboardTelemetry.addLine("Running 1m square path...");
        dashboardTelemetry.update();
        
        // Create a simple square path
        List<Waypoint> squarePath = Arrays.asList(
            Waypoint.at(0, 0)
                .speed(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED)
                .turnSpeed(RobotConfig.FieldOriented.DEFAULT_TURN_SPEED)
                .build(),
            
            Waypoint.at(100, 0)  // 1 meter right
                .speed(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED)
                .onReach(() -> dashboardTelemetry.addLine("✓ Waypoint 1 reached"))
                .build(),
            
            Waypoint.at(100, 100)  // 1 meter forward
                .speed(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED)
                .onReach(() -> dashboardTelemetry.addLine("✓ Waypoint 2 reached"))
                .build(),
            
            Waypoint.at(0, 100)  // 1 meter left
                .speed(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED)
                .onReach(() -> dashboardTelemetry.addLine("✓ Waypoint 3 reached"))
                .build(),
            
            Waypoint.at(0, 0)  // Back to start
                .speed(RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED)
                .onReach(() -> dashboardTelemetry.addLine("✓ Returned home!"))
                .build()
        );
        
        follower.follow(squarePath);
        
        dashboardTelemetry.addLine("Square test complete!");
        dashboardTelemetry.addLine("Observe:");
        dashboardTelemetry.addLine("- Smooth path without jerking");
        dashboardTelemetry.addLine("- Proper corner handling");
        dashboardTelemetry.addLine("- Accurate waypoint arrival");
        dashboardTelemetry.update();
        sleep(3000);
    }
}
```

---

## Utilities and Testing

### Parameter Persistence Helper

```java
public class TuningPersistence {
    private static final String TUNING_FILE = "/sdcard/Crawler/tuning_values.txt";
    
    public static void saveAllValues() {
        try {
            File dir = new File("/sdcard/Crawler/");
            if (!dir.exists()) dir.mkdirs();
            
            FileWriter writer = new FileWriter(TUNING_FILE);
            
            // Odometry
            writer.write("# Odometry Calibration\n");
            writer.write("TRACK_WIDTH=" + RobotConfig.Odometry.TRACK_WIDTH + "\n");
            writer.write("CENTER_WHEEL_OFFSET=" + RobotConfig.Odometry.CENTER_WHEEL_OFFSET + "\n");
            writer.write("WHEEL_DIAMETER=" + RobotConfig.Odometry.WHEEL_DIAMETER + "\n");
            
            // Robot-Oriented PID
            writer.write("\n# Robot-Oriented PID\n");
            writer.write("drive_Kp=" + RobotConfig.RobotOriented.Kp + "\n");
            writer.write("drive_Ki=" + RobotConfig.RobotOriented.Ki + "\n");
            writer.write("drive_Kd=" + RobotConfig.RobotOriented.Kd + "\n");
            writer.write("strafe_Kp=" + RobotConfig.RobotOriented.strafe_Kp + "\n");
            writer.write("strafe_Ki=" + RobotConfig.RobotOriented.strafe_Ki + "\n");
            writer.write("strafe_Kd=" + RobotConfig.RobotOriented.strafe_Kd + "\n");
            writer.write("STEER_P=" + RobotConfig.RobotOriented.STEER_P + "\n");
            
            // Field-Oriented
            writer.write("\n# Field-Oriented Path Following\n");
            writer.write("DEFAULT_MOVE_SPEED=" + RobotConfig.FieldOriented.DEFAULT_MOVE_SPEED + "\n");
            writer.write("DEFAULT_TURN_SPEED=" + RobotConfig.FieldOriented.DEFAULT_TURN_SPEED + "\n");
            writer.write("DEFAULT_FOLLOW_DISTANCE=" + RobotConfig.FieldOriented.DEFAULT_FOLLOW_DISTANCE + "\n");
            writer.write("ARRIVAL_THRESHOLD=" + RobotConfig.FieldOriented.ARRIVAL_THRESHOLD + "\n");
            
            writer.close();
        } catch (IOException e) {
            Log.e("TuningPersistence", "Save failed", e);
        }
    }
}
```

### Telemetry Helper for Live Display

```java
public class TuningTelemetry {
    private final Telemetry dashboardTelemetry;
    
    public TuningTelemetry(Telemetry telemetry) {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        this.dashboardTelemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
    }
    
    public void displayMovementDebug(Pose2d pose, double power, double error) {
        dashboardTelemetry.addData("Pose X (cm)", String.format("%.2f", pose.getX()));
        dashboardTelemetry.addData("Pose Y (cm)", String.format("%.2f", pose.getY()));
        dashboardTelemetry.addData("Heading (°)", String.format("%.2f", pose.getHeading()));
        dashboardTelemetry.addData("Error", String.format("%.2f", error));
        dashboardTelemetry.addData("Motor Power", String.format("%.3f", power));
    }
    
    public void update() {
        dashboardTelemetry.update();
    }
}
```

---

## Complete Workflow

### Recommended Tuning Sequence

1. **Create Motor Verification OpMode**
   - Spin each motor individually
   - Verify rotation direction
   - Location: `Tuning/TuneOdometryMotors.java`

2. **Run Encoder Baseline OpMode**
   - Spin wheels 1 revolution, record tick count
   - Calculate TICKS_PER_REV
   - Location: `Tuning/TuneEncoderBaseline.java`

3. **Run Track Width Tuning OpMode**
   - Spin robot 10 rotations
   - Measure heading error
   - Adjust TRACK_WIDTH until error < 5°
   - Location: `Tuning/TuneTrackWidth.java`

4. **Run Center Wheel Offset Tuning OpMode**
   - Strafe 1 meter
   - Measure heading drift
   - Adjust CENTER_WHEEL_OFFSET until drift < 2°
   - Location: `Tuning/TuneCenterWheelOffset.java`

5. **Run Robot-Oriented PID Tuning OpMode**
   - Test drive with various Kp values
   - Test strafe with various strafe_Kp values
   - Test turn with various STEER_P values
   - Location: `Tuning/TuneRobotOrientedPID.java`

6. **Run Field-Oriented Path Following OpMode**
   - Test square path
   - Adjust speed, follow distance, arrival threshold
   - Verify smooth path following
   - Location: `Tuning/TuneFieldOrientedPath.java`

### Testing Checklist

- [ ] All motors spin in correct directions
- [ ] Encoder tick counts match motor specs
- [ ] Robot spins and returns to heading < 5° error
- [ ] Robot strafes without rotating > 2°
- [ ] Robot drives 1m with < 2% distance error
- [ ] Robot strafes 1m with < 2% distance error
- [ ] Robot turns to target heading with < 1° error
- [ ] Square path follows smoothly without oscillation
- [ ] Waypoints trigger at correct distance
- [ ] All values saved to `/sdcard/Crawler/tuning_values.txt`

---

## Tips for Implementation

1. **Use FTC Dashboard**: Configure parameters in real-time without recompiling
   ```gradle
   implementation 'com.acmerobotics.dashboard:dashboard:0.4.7'
   ```

2. **Add @Config Annotations**: Allow Dashboard to modify RobotConfig values
   ```java
   @Config
   public static class RobotOriented {
       public static double Kp = 0.05;
   }
   ```

3. **Enable Live Telemetry**: Wrap with MultipleTelemetry for both Driver Station and Dashboard
   ```java
   Telemetry dashboardTelemetry = new MultipleTelemetry(telemetry, dashboard.getTelemetry());
   ```

4. **Test in Multiple Scenarios**: Different distances, speeds, and field conditions

5. **Document All Values**: Save to file with timestamp for version history

