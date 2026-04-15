package org.firstinspires.ftc.teamcode.Crawler.FieldOrient;

import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.RobotConfig;
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
 * FOFollower follower = new FOFollower(robot, opMode.telemetry, opMode);
 * follower.follow(
 *     Waypoint.at(0, 0)
 *         .at(24, 0).speed(0.8).onReach(() -> robot.openClaw())
 *         .at(24, 24).slow()
 *         .buildAll()
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
     * read configuration from {@code RobotConfig.FieldOriented}.</p>
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
        // Validate waypoint list
        if (waypoints == null || waypoints.isEmpty()) {
            telemetry.addLine("[Crawler] WARNING: follow() called with empty waypoint list. Ignoring.");
            telemetry.update();
            return;
        }

        if (waypoints.size() < 2) {
            telemetry.addLine("[Crawler] WARNING: follow() needs at least 2 waypoints.");
            telemetry.update();
            return;
        }

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
        robot.driveTrain.stop();
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
        double timeout = RobotConfig.RobotBase.timeoutSecs;

        while (opModeProxy.isActive()) {
            // Timeout safety check
            if (waypointTimer.seconds() > timeout) {
                telemetry.addData("WARNING", "Waypoint timeout: %.1f seconds", waypointTimer.seconds());
                telemetry.update();
                robot.driveTrain.stop();  // Stop motors on timeout
                break;
            }

            // Update robot position from localiser
            robot.localiser.update();

            // Check arrival
            double distanceToTarget = Math.hypot(
                    waypoint.x - robot.localiser.getPose().getX(),
                    waypoint.y - robot.localiser.getPose().getY()
            );

            if (distanceToTarget < RobotConfig.FieldOriented.ARRIVAL_THRESHOLD) {
                // Arrived
                break;
            }

            // Compute and execute movement using pure pursuit
            // Call goToPosition directly instead of wrapping in follow()
            movement.goToPosition(
                    waypoint.x, waypoint.y,
                    waypoint.moveSpeed,
                    movement.getWorldHeading(),
                    waypoint.turnSpeed
            );

            // Telemetry
            telemetry.addData("Target (cm)", "%.1f, %.1f", waypoint.x, waypoint.y);
            telemetry.addData("Distance (cm)", "%.2f cm", distanceToTarget);
            telemetry.addData("Elapsed", "%.2f s", waypointTimer.seconds());
            telemetry.update();

            Thread.yield();
        }
    }
}
