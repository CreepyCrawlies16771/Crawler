package org.firstinspires.ftc.teamcode.Crawler;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Example robot implementation showing the Crawler builder pattern.
 *
 * <p>This class demonstrates how teams should extend {@code CrawlerRobot} to
 * add their own season-specific hardware and high-level action methods. All
 * hardware device names are defined here (not in {@code RobotConfig}), and
 * the builder is called in the constructor to initialize the base drivetrain.</p>
 *
 * <p>Teams should copy this class, rename it to their own robot name, and add
 * their own hardware device names and action methods.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * public class RedAuto extends CrawlerAuto<MyRobot> {
 *     @Override protected MyRobot buildRobot(HardwareMap hwMap) {
 *         return new MyRobot(hwMap);
 *     }
 *     @Override protected void runPath() throws InterruptedException {
 *         robot.openClaw();
 *         // follow waypoints...
 *     }
 * }
 * }</pre>
 *
 * @see CrawlerRobot
 */
public class MyRobot extends CrawlerRobot {
    // Hardware device names — teams must match these to their actual robot configuration
    private static final String CLAW_SERVO_NAME = "claw";
    private static final String LIFT_MOTOR_NAME = "lift";

    // Season-specific hardware
    public final Servo clawServo;
    public final DcMotor liftMotor;

    /**
     * Constructs a MyRobot instance and initializes the Crawler drivetrain.
     *
     * <p>This calls the builder to set up the four drive motors, IMU, and odometry
     * localiser. Season-specific hardware (servo, motor) is then retrieved from
     * the HardwareMap. Teams should update the hardware names and add more motors
     * or servos as needed for their season.</p>
     *
     * @param hwMap the hardware map provided by the FTC SDK
     */
    public MyRobot(HardwareMap hwMap) {
        super(
                (Builder) new Builder(hwMap)
                        .frontLeft("fl")
                    .frontRight("fr")
                    .backLeft("bl")
                    .backRight("br")
                    .motors()
                    .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
                    .setTrackWidth(33.02)
                    .setCenterWheelOffset(8.89)
        );
        // Get season hardware
        this.clawServo = hwMap.get(Servo.class, CLAW_SERVO_NAME);
        this.liftMotor = hwMap.get(DcMotor.class, LIFT_MOTOR_NAME);
    }

    /**
     * Opens the claw to release game elements.
     *
     * <p>Sets the servo to the open position (0.8). Teams should measure and
     * adjust this value based on their mechanical design.</p>
     */
    public void openClaw() {
        clawServo.setPosition(0.8);
    }

    /**
     * Closes the claw to grip game elements.
     *
     * <p>Sets the servo to the closed position (0.2). Teams should measure and
     * adjust this value based on their mechanical design.</p>
     */
    public void closeClaw() {
        clawServo.setPosition(0.2);
    }

    /**
     * Scores in the high basket — closes claw and raises lift.
     *
     * <p>This is a compound action: it raises the lift to position 800 ticks
     * and opens the claw simultaneously, depositing the game element in the
     * high basket. Teams should adjust these values.</p>
     */
    public void scoreHighBasket() {
        setLift(800);
        openClaw();
    }

    /**
     * Sets the lift motor to a target encoder position.
     *
     * <p>Uses the motor's built-in encoder to reach the specified position in
     * motor ticks. Teams should measure tick offsets for common positions like
     * floor, floor stack, and high basket.</p>
     *
     * @param targetTicks the target encoder position, in motor ticks
     */
    public void setLift(int targetTicks) {
        liftMotor.setTargetPosition(targetTicks);
        liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        liftMotor.setPower(0.8);
    }

    /**
     * Stops the lift motor immediately.
     */
    public void stopLift() {
        liftMotor.setPower(0);
    }
}
