package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import static org.junit.Assert.assertTrue;

import com.arcrobotics.ftclib.geometry.Pose2d;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;
import org.firstinspires.ftc.teamcode.Crawler.sim.FakeTelemetry;
import org.firstinspires.ftc.teamcode.Crawler.sim.SimulatedHardwareMap;
import org.firstinspires.ftc.teamcode.Crawler.sim.SimulatedImu;
import org.firstinspires.ftc.teamcode.Crawler.sim.SimulatedMotor;
import org.junit.Test;

import java.util.Arrays;

/**
 * Integration test — runs the <b>real</b> path-following stack on the JVM with
 * simulated motors and the {@code SimulatedLocaliser}: builder → robot → preflight →
 * pure pursuit → field-relative drive → odometry update. No robot or hardware needed.
 *
 * <p>If this test fails, movement regressed somewhere in the pipeline — you can debug
 * the whole thing on a PC before touching a robot.</p>
 */
public class SimulatedPathIntegrationTest {

    // Sim motor speed: ticks per second at full power (≈200 RPM × 2000 CPR). The robot
    // then covers ~25 cm/s, so the square below completes within the waypoint timeout.
    private static final double TICKS_PER_UNIT_POWER = 6667;

    private static CrawlerRobot buildSimulatedRobot(double timeoutSecs) {
        SimulatedHardwareMap hw = new SimulatedHardwareMap(new SimulatedImu());
        for (String name : new String[]{"fl", "fr", "bl", "br"}) {
            hw.putMotor(name, new SimulatedMotor(TICKS_PER_UNIT_POWER));
        }
        return new CrawlerRobot.Builder(hw)
                .frontLeft("fl").frontRight("fr").backLeft("bl").backRight("br")
                .imu("imu")
                .motors()
                .withSimulatedLocaliser()
                .setTrackWidth(13.0)
                .wheelDiameter(1.37795)
                .ticksPerRev(2000)
                .drivePid(0.05, 0.0, 0.0)
                .strafePid(0.05, 0.0, 0.0)
                .steerPid(0.03, 0.0, 0.0)
                .minPower(0.15)
                .pathDefaults(0.7, 0.4, 25.4)
                .arrivalThresholdCm(5.0)
                .orbitThresholdCm(25.4)
                .timeoutSecs(timeoutSecs)
                .turnReferenceRadians(Math.toRadians(30))
                .maxDriveSpeed(1.0)
                .build();
    }

    @Test
    public void squarePath_drivesAcrossTheFieldAndCompletes() throws Exception {
        CrawlerRobot robot = buildSimulatedRobot(4.0);
        robot.resetPose();

        // A probe thread samples the pose while follow() blocks, so we can assert the
        // robot genuinely crossed the field even though follow() never returns control.
        // It samples for up to 6 s (300 × 20 ms) — long enough, because the robot leaves
        // the origin during the first leg, which is exactly what this test verifies.
        final double[] maxDist = {0.0};
        Thread probe = new Thread(() -> {
            try {
                for (int i = 0; i < 300 && !Thread.currentThread().isInterrupted(); i++) {
                    Thread.sleep(20);
                    double d = Math.hypot(robot.getPose().getX(), robot.getPose().getY());
                    synchronized (maxDist) {
                        maxDist[0] = Math.max(maxDist[0], d);
                    }

                }
            } catch (InterruptedException ignored) {}
        });

        probe.start();

        FOFollower follower = new FOFollower(robot, new FakeTelemetry(), () -> true);
        try {
            // A 30 cm square with modest speed keeps the whole integration test fast
            // (the crude pure-pursuit engine orbits corners instead of slowing for
            // them, so a 60 cm square burns the per-waypoint timeout).
            follower.follow(Arrays.asList(
                    Waypoint.at(0, 0, robot.config).speed(0.5).build(),
                    Waypoint.at(30, 0, robot.config).speed(0.5).build(),
                    Waypoint.at(30, 30, robot.config).speed(0.5).build(),
                    Waypoint.at(0, 30, robot.config).speed(0.5).build(),
                    Waypoint.at(0, 0, robot.config).speed(0.5).build()
            ));
        } finally {
            probe.interrupt();
            probe.join();
        }

        // The path is a 30 cm square, so the robot must leave the origin by far more
        // than the 5 cm arrival threshold — otherwise odometry never fed the follower.
        synchronized (maxDist) {
            assertTrue("robot never left the origin (max dist " + String.format("%.1f", maxDist[0])
                            + " cm) — odometry/drive did not connect",
                    maxDist[0] > 25.0);
        }
    }

    @Test
    public void straightLine_reachesTheTarget() throws Exception {
        // 80 cm at ~25 cm/s needs ~3.2 s of driving; keep the timeout well clear of
        // that so a loaded CI machine doesn't trip RUNTIME_LEG_TIMEOUT spuriously.
        CrawlerRobot robot = buildSimulatedRobot(8.0);
        robot.resetPose();

        FOFollower follower = new FOFollower(robot, new FakeTelemetry(), () -> true);
        follower.follow(Arrays.asList(
                Waypoint.at(0, 0, robot.config).build(),
                Waypoint.at(80, 0, robot.config).build()
        ));

        Pose2d end = robot.getPose();
        assertTrue("ended " + String.format("%.1f", Math.hypot(end.getX(), end.getY()))
                        + " cm from target",
                Math.hypot(end.getX() - 80, end.getY()) < 10.0);
    }
}
