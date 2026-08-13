package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrors;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerPreflight;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.Arrays;
import java.util.List;

/**
 * Blocking field-oriented path follower using pure pursuit.
 *
 * <p>This class wraps {@code RobotMovement} to provide a high-level interface for
 * following a sequence of waypoints. It updates the localiser each cycle, fires
 * {@code onReach()} callbacks when waypoints are reached, and maintains proper
 * heading control throughout the path.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * FOFollower follower = new FOFollower(robot, telemetry, this::opModeIsActive);
 * follower.follow(
 *     Waypoint.at(0, 0, robot.config).build(),
 *     Waypoint.at(24, 0, robot.config).speed(0.8).onReach(() -> robot.openClaw()).build(),
 *     Waypoint.at(24, 24, robot.config).slow(robot.config).build()
 * );
 * }</pre>
 *
 * @see RobotMovement
 * @see Waypoint
 */
public class FOFollower {
    private final CrawlerRobot robot;
    private final RobotMovement movement;
    private final Telemetry telemetry;
    private final OpModeProxy opModeProxy;

    /** Guards against overlapping {@code follow()} calls (CRWL-401). */
    private boolean following;


    /**
     * Interface to access LinearOpMode methods without direct dependency.
     *
     * <p>This allows FOFollower to check {@code opModeIsActive()} and access
     * telemetry without extending LinearOpMode.</p>
     */
    public interface OpModeProxy {
        /**
         * Checks if the current operation mode is still active.
         *
         * @return true if the OpMode should continue running, false otherwise
         */
        boolean isActive();
    }

    /**
     * Creates a new field-oriented follower.
     *
     * <p>The follower will use the robot's localiser to track position and
     * read configuration from {@code robot.config}.</p>
     *
     * @param robot the {@code CrawlerRobot} instance to control
     * @param telemetry the telemetry object for debugging output
     * @param opModeProxy proxy to check if the OpMode is still active
     */
    public FOFollower(CrawlerRobot robot, Telemetry telemetry, OpModeProxy opModeProxy) {
        this.robot = robot;
        this.movement = new RobotMovement(robot);
        this.telemetry = telemetry;
        this.opModeProxy = opModeProxy;
    }

    /**
     * Follows a list of waypoints sequentially.
     *
     * <p>This method blocks until all waypoints are reached or the OpMode becomes
     * inactive. For each waypoint segment, it runs the pure pursuit control loop
     * until the robot arrives (position within {@code ARRIVAL_THRESHOLD} centimeters).
     * When a waypoint is reached, its {@code onReach()} callback is executed
     * if one was set.</p>
     *
     * @param waypoints the path to follow
     * @throws InterruptedException if the OpMode is stopped during execution
     */
    public void follow(List<Waypoint> waypoints) throws InterruptedException {
        if (following) {
            CrawlerErrors.throwError(CrawlerError.RUNTIME_OVERLAPPING_FOLLOW);
        }
        following = true;
        try {
            // Early error detection: every config, pose, localizer, IMU and path
            // check runs here, before the first motor spins. All problems are
            // posted to telemetry, then the first one is thrown (CRWL-101..306).
            CrawlerPreflight.run(robot, waypoints, telemetry);

            for (int i = 0; i < waypoints.size(); i++) {
                if (!opModeProxy.isActive()) {
                    throw new InterruptedException("OpMode stopped");
                }

                Waypoint target = waypoints.get(i);
                followToWaypoint(target);

                // Execute onReach callback if present
                if (target.onReach != null) {
                    target.onReach.run();
                }
            }

            // Ensure motors are stopped after path completion
            robot.stop();
        } finally {
            following = false;
        }
    }

    /**
     * Follows a variable number of waypoints.
     *
     * <p>Convenience overload that converts varargs to a list.</p>
     *
     * @param waypoints the waypoints to follow
     * @throws InterruptedException if the OpMode is stopped during execution
     */
    public void follow(Waypoint... waypoints) throws InterruptedException {
        if(!opModeProxy.isActive()) return;
        follow(Arrays.asList(waypoints));
    }

    /**
     * Follows a single waypoint until arrival.
     *
     * <p>This is the core loop: update localiser, compute desired motor powers
     * using pure pursuit, and repeat until the robot is within the arrival
     * threshold of the target position.</p>
     *
     * @param waypoint the target waypoint
     * @throws InterruptedException if the OpMode is stopped
     */
    private void followToWaypoint(Waypoint waypoint) throws InterruptedException {
        ElapsedTime waypointTimer = new ElapsedTime();
        double timeout = robot.config.timeoutSecs;

        // Stall detector for CRWL-202: if the robot is commanded to move but the
        // localizer reports no motion for a few seconds, the encoders aren't
        // seeing movement (wrong port / unplugged / reversed setup).
        //
        // Movement is ACCUMULATED over the stall window (not required per loop
        // iteration), so the detector works at any loop rate — a 50 Hz OpMode and
        // a 100 kHz simulated test both behave identically.
        ElapsedTime stallTimer = new ElapsedTime();
        double lastX = robot.getPose().getX();
        double lastY = robot.getPose().getY();
        double accumulatedMotion = 0.0;

        while (opModeProxy.isActive()) {
            // Update robot position from localiser
            robot.localiser.update();

            // Check arrival
            double distanceToTarget = Math.hypot(
                    waypoint.x - robot.localiser.getPose().getX(),
                    waypoint.y - robot.localiser.getPose().getY()
            );

            if (distanceToTarget < robot.config.arrivalThresholdCm) {
                // Arrived
                break;
            }

            // Stall detection (only while we still have distance to cover)
            double x = robot.localiser.getPose().getX();
            double y = robot.localiser.getPose().getY();
            accumulatedMotion += Math.hypot(x - lastX, y - lastY);
            lastX = x;
            lastY = y;
            if (accumulatedMotion > 0.2) {
                stallTimer.reset();
                accumulatedMotion = 0.0;
            } else if (stallTimer.seconds() > 3.0) {
                robot.stop();
                CrawlerErrors.throwError(CrawlerError.ODO_ENCODERS_NOT_MOVING);
            }

            // Compute and execute movement using pure pursuit
            movement.goToPosition(
                    waypoint.x, waypoint.y,
                    waypoint.moveSpeed,
                    movement.getWorldHeading(),
                    waypoint.turnSpeed
            );

            // Timeout safety check
            if (waypointTimer.seconds() > timeout) {
                robot.stop();  // Stop motors on timeout
                CrawlerErrors.postToTelemetry(
                        telemetry,
                        CrawlerError.RUNTIME_LEG_TIMEOUT,
                        waypointTimer.seconds());
                break;
            }

            // Telemetry
            telemetry.addData("Target (cm)", "%.1f, %.1f", waypoint.x, waypoint.y);
            telemetry.addData("Distance (cm)", "%.2f cm", distanceToTarget);
            telemetry.addData("Elapsed", "%.2f s", waypointTimer.seconds());
            telemetry.update();

            Thread.yield();
        }
    }
}
