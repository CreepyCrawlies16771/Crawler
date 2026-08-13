---
title: Full Example
description: A complete, real-world Crawler project — hardware, auto, TeleOp, and tuning
---

# Full Example

*From hardware names to a match-ready autonomous*

This page shows the complete set of files you'd write for a season. Copy them, change the device names and coordinates, tune, and you're racing.

```
Teamcode/
├── MyRobot.java           ← device names + builder config + mechanisms (one file)
├── RedAuto.java           ← autonomous path
├── Driver.java            ← TeleOp
└── ManualAdjust.java      ← precise robot-relative moves
```

## 1 · MyRobot.java

Everything — device names, localizer, tuned numbers, mechanisms — lives in one file:

```java
package org.firstinspires.ftc.teamcode.Teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobotRegistry;

public class MyRobot extends CrawlerRobot {

    // Registers this robot class so Crawler's tools (Tuner, System Test, Smoke Test)
    // can build it without hard-coding this class's name.
    static {
        CrawlerRobotRegistry.setProvider(
                MyRobot::new,
                (hwMap, config) -> builder(hwMap).withConfig(config).build()
        );
    }

    public static final String FRONT_LEFT = "fl";
    public static final String FRONT_RIGHT = "fr";
    public static final String BACK_LEFT = "bl";
    public static final String BACK_RIGHT = "br";
    public static final String IMU = "imu";
    public static final String ENC_LEFT = "enc_l";
    public static final String ENC_RIGHT = "enc_r";
    public static final String ENC_CENTER = "enc_c";

    public static final String CLAW_SERVO = "claw";
    public static final String LIFT_MOTOR = "lift";

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

    /** Drivetrain + odometry + tuned values — reused by the Crawler Tuner. */
    public static CrawlerRobot.Builder builder(HardwareMap hwMap) {
        return new CrawlerRobot.Builder(hwMap)
                .frontLeft(FRONT_LEFT)
                .frontRight(FRONT_RIGHT)
                .backLeft(BACK_LEFT)
                .backRight(BACK_RIGHT)
                .imu(IMU)
                .imuOrientation(IMU_LOGO, IMU_USB)
                .motors()
                .withThreeDeadWheels(ENC_LEFT, ENC_RIGHT, ENC_CENTER)
                // --- tuned values (from the Crawler Tuner, Square / Step 7) ---
                .setTrackWidth(13.0)
                .setCenterWheelOffset(3.5)
                .wheelDiameter(1.37795)
                .ticksPerRev(2000)
                .drivePid(0.05, 0.0, 0.0)
                .strafePid(0.05, 0.0, 0.0)
                .steerPid(0.03, 0.0, 0.0)
                .minPower(0.15)
                .pathDefaults(0.7, 0.4, 25.4)
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .timeoutSecs(5.0)
                .turnReferenceRadians(Math.toRadians(30))
                .maxDriveSpeed(1.0);
    }

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

## 2 · RedAuto.java — the autonomous

A specimen-scoring auto: start at the staging area, drive to the basket, drop the preload, come back for more.

```java
package org.firstinspires.ftc.teamcode.Teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Crawler.FieldOrient.FOFollower;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

@Autonomous(name = "Red Auto", group = "Main")
public class RedAuto extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);

        telemetry.addLine("Press PLAY to start");
        telemetry.update();
        waitForStart();

        // Start pose is required before following (CRWL-101 otherwise).
        robot.resetPose();

        // Preloaded specimen → high basket
        follower.follow(
                Waypoint.at(0, 0, robot.config).build(),

                Waypoint.at(80, 0, robot.config)
                        .speed(0.8)
                        .onReach(() -> {
                            telemetry.addData("Action", "Approaching basket");
                            telemetry.update();
                        })
                        .build(),

                Waypoint.at(80, 80, robot.config)
                        .slow(robot.config)
                        .onReach(() -> {
                            robot.scoreHighBasket();      // lift + open claw
                            sleep(400);                   // let the specimen drop
                            robot.closeClaw();
                        })
                        .build()
        );

        // Back to the staging area
        follower.follow(
                Waypoint.at(80, 40, robot.config).speed(0.7).build(),
                Waypoint.at(0, 0, robot.config).speed(0.7).build()
        );

        telemetry.addData("Final X (cm)", String.format("%.2f", robot.getPose().getX()));
        telemetry.addData("Final Y (cm)", String.format("%.2f", robot.getPose().getY()));
        telemetry.update();
        robot.stop();
    }
}
```

## 3 · Driver.java — the TeleOp

```java
package org.firstinspires.ftc.teamcode.Teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Driver", group = "Main")
public class Driver extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {
        MyRobot robot = new MyRobot(hardwareMap);
        waitForStart();

        while (opModeIsActive()) {
            robot.update();

            // Driver 1 — movement (field-relative)
            double slow = gamepad1.left_trigger > 0.1 ? 0.35 : 1.0;
            robot.driveFieldRelative(
                    -gamepad1.left_stick_y * slow,
                    gamepad1.left_stick_x * slow,
                    gamepad1.right_stick_x * slow);

            // Driver 1 — claw
            if (gamepad1.a) robot.openClaw();
            else if (gamepad1.b) robot.closeClaw();

            // Driver 2 — lift
            if (gamepad2.right_trigger > 0.1) robot.scoreHighBasket();
            else if (gamepad2.left_trigger > 0.1) {
                robot.liftMotor.setTargetPosition(0);
                robot.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                robot.liftMotor.setPower(0.8);
            }

            telemetry.addData("X (cm)", String.format("%.2f", robot.getPose().getX()));
            telemetry.addData("Y (cm)", String.format("%.2f", robot.getPose().getY()));
            telemetry.addData("Heading (°)", String.format("%.2f", Math.toDegrees(robot.getHeading())));
            telemetry.update();

            idle();
        }
        robot.stop();
    }
}
```

## 4 · ManualAdjust.java — precise final moves

For the last few centimeters before a mechanism action, robot-relative PID is more predictable than pure pursuit:

```java
package org.firstinspires.ftc.teamcode.Teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.ROMovementEngine;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

@Autonomous(name = "Manual Adjust", group = "Main")
public class ManualAdjust extends ROMovementEngine {

    @Override
    protected CrawlerRobot buildRobot(HardwareMap hwMap) {
        return new MyRobot(hwMap);
    }

    @Override
    public void runPath() throws InterruptedException {
        drivePID(0.30, 0);    // 30 cm forward, hold 0°
        turnPID(45);          // turn to absolute 45°
        strafePID(0.20, 45);  // 20 cm right, hold 45°
    }
}
```

## Putting it together

1. Clone the repo and open it in Android Studio
2. Fix the device names at the top of `MyRobot.java`
3. Run the **Crawler Tuner** → paste Square's builder lines into `MyRobot.builder()`
4. Run **Red Auto** — tune waypoint coordinates to your field
5. Run **Driver** for TeleOp

That's a full, working FTC project. 🚀

---

## Next Steps

- **[Troubleshooting →](troubleshooting.md)** when something doesn't behave
- **[Tuning Guide →](tuning-guide.md)** for a perfect first run
