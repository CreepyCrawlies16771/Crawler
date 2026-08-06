package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.RobotOrient.RobotOrientedDrive;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

/**
 * Runs the tuner's PID tests <b>through the real movement engine</b>
 * ({@link RobotOrientedDrive}), so the values you tune behave identically when
 * {@code drivePID} / {@code strafePID} / {@code turnPID} run in a real match.
 *
 * <p>Every control cycle is streamed to the Driver Station and FTC Dashboard
 * (error, power, P/I/D terms) and the robot's pose is drawn on the Dashboard
 * field view.</p>
 */
final class TuningPidRunner {

    private static final double TARGET_DISTANCE_M = 1.0;   // 100 cm, matches engine meters
    private static final double TARGET_TURN_DEG   = 90.0;

    private final CrawlerRobot robot;
    private final Telemetry telemetry;
    private final TuningActiveCheck active;
    private final RobotOrientedDrive engine;

    /** Unit of the current test's error — set before each engine call. */
    private String errorUnit = "cm";

    TuningPidRunner(CrawlerRobot robot, Telemetry telemetry, TuningActiveCheck active) {
        this.robot = robot;
        this.telemetry = telemetry;
        this.active = active;
        // Same engine the library uses at runtime — tuned gains are the real gains.
        this.engine = new RobotOrientedDrive(robot, active::isActive, telemetry);
        engine.setDebugSink(this::onEngineTick);
    }

    /** Drive 1 m forward while holding the starting heading. */
    void testDrive() throws InterruptedException {
        int hold = (int) Math.round(TuningUtil.imuYawDeg(robot.imu));
        errorUnit = "cm";
        engine.drivePID(TARGET_DISTANCE_M, hold);
        sleepBrief();
    }

    /** Strafe 1 m right while holding the starting heading. */
    void testStrafe() throws InterruptedException {
        int hold = (int) Math.round(TuningUtil.imuYawDeg(robot.imu));
        errorUnit = "cm";
        engine.strafePID(TARGET_DISTANCE_M, hold);
        sleepBrief();
    }

    /** Turn 90° clockwise from the current heading (absolute, via the engine). */
    void testTurn() throws InterruptedException {
        double target = TuningUtil.imuYawDeg(robot.imu) + TARGET_TURN_DEG;
        errorUnit = "deg";
        engine.turnPID((int) Math.round(target));
        sleepBrief();
    }

    /**
     * Automatic min-power (friction deadband) search. This is a tuner measurement,
     * not an engine command, so it drives raw motor power directly.
     */
    void testMinPower() throws InterruptedException {
        robot.resetPose();
        robot.update();
        double power = 0.05;
        double moved = 0.0;

        while (active.isActive() && power <= 0.6) {
            robot.drive(power, 0, 0);
            robot.update();
            TuningDashboard.drawRobot(robot);
            moved = Math.hypot(robot.getPose().getX(), robot.getPose().getY());
            if (moved > 0.75) break;
            power += 0.02;
            Thread.yield();
        }
        robot.stop();

        double recommended = Math.min(0.4, Math.max(0.05,
                Math.round((power + 0.03) * 100) / 100.0));
        TuningConfig.minPower = recommended;

        telemetry.addData("Starts moving at", String.format("%.2f power", power));
        telemetry.addData("Recommended minPower", String.format("%.2f", recommended));
        telemetry.addLine("minPower updated — tweak in Dashboard / D-pad if needed");
        telemetry.update();
    }

    /**
     * Engine callback: adds live loop data (the engine flushes telemetry once per
     * cycle with its own lines) and draws the robot on the Dashboard field view.
     */
    private void onEngineTick(double error, double power, double p, double i, double d) {
        telemetry.addData("Error (" + errorUnit + ")", String.format("%.1f", error));
        telemetry.addData("Power", String.format("%.3f", power));
        telemetry.addData("P/I/D", String.format("%.3f / %.4f / %.4f", p, i, d));
        TuningDashboard.drawRobot(robot);
    }

    private void sleepBrief() throws InterruptedException {
        ElapsedTime wait = new ElapsedTime();
        while (active.isActive() && wait.milliseconds() < 1500) Thread.yield();
    }
}
