package org.firstinspires.ftc.teamcode.Crawler.core.errors;

import com.arcrobotics.ftclib.geometry.Pose2d;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.utils.Waypoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs every Crawler error check <b>before</b> the robot starts moving, so problems
 * are caught at the start of a path instead of mid-match.
 *
 * <p>Checks (each maps to a {@link CrawlerError} code):</p>
 * <ul>
 *   <li>{@code 107} — invalid tuned config values</li>
 *   <li>{@code 101} — no starting pose set before a path</li>
 *   <li>{@code 205} — localizer returns a non-finite pose</li>
 *   <li>{@code 201} — IMU not responding</li>
 *   <li>{@code 301–306} — malformed waypoint path</li>
 * </ul>
 *
 * <p>{@link #run(CrawlerRobot, List, Telemetry)} posts <b>every</b> problem found to
 * telemetry, then throws the first one so the OpMode stops with a clear red message.</p>
 */
public final class CrawlerPreflight {

    private CrawlerPreflight() {}

    /**
     * Full preflight for a field-oriented path — call from {@code FOFollower.follow()}
     * before the pursuit loop starts.
     *
     * @throws CrawlerErrorException the first error found (all errors are also posted
     *                               to {@code telemetry})
     */
    public static void run(CrawlerRobot robot, List<Waypoint> waypoints, Telemetry telemetry)
            throws CrawlerErrorException {
        List<CrawlerErrorException> errors = new ArrayList<>();
        collectConfig(robot.config, errors);
        if (robot.localisation != CrawlerRobot.Localisation.DevLocaliser) {
            collectPoseInitialized(robot, errors);
        }
        collectLocalizerHealth(robot, errors);
        collectImu(robot, errors);
        collectPath(waypoints, errors);
        raise(telemetry, errors);
    }

    /**
     * Preflight for the robot-relative movement engine (config + IMU). Called from the
     * {@code RobotOrientedDrive} constructor, so bad values surface at INIT, before the
     * OpMode starts moving.
     */
    public static void checkEngine(CrawlerRobot robot, Telemetry telemetry)
            throws CrawlerErrorException {
        List<CrawlerErrorException> errors = new ArrayList<>();
        collectConfig(robot.config, errors);
        collectImu(robot, errors);
        raise(telemetry, errors);
    }

    /** Config-only validation used by {@code CrawlerRobot.Builder.build()}. */
    public static void checkConfigOrThrow(CrawlerRobot.Config config) {
        List<CrawlerErrorException> errors = new ArrayList<>();
        collectConfig(config, errors);
        if (!errors.isEmpty()) throw errors.get(0);
    }

    private static void raise(Telemetry telemetry, List<CrawlerErrorException> errors) {
        if (errors.isEmpty()) return;
        for (CrawlerErrorException e : errors) {
            CrawlerErrors.postToTelemetry(telemetry, e);
        }
        throw errors.get(0);
    }

    private static void collectConfig(CrawlerRobot.Config c, List<CrawlerErrorException> errors) {
        List<String> bad = new ArrayList<>();
        if (!(c.defaultMoveSpeed > 0 && c.defaultMoveSpeed <= 1)) {
            bad.add("defaultMoveSpeed=" + c.defaultMoveSpeed);
        }
        if (!(c.defaultTurnSpeed > 0 && c.defaultTurnSpeed <= 1)) {
            bad.add("defaultTurnSpeed=" + c.defaultTurnSpeed);
        }
        if (!(c.maxDriveSpeed > 0 && c.maxDriveSpeed <= 1)) {
            bad.add("maxDriveSpeed=" + c.maxDriveSpeed);
        }
        if (!(c.minPower >= 0 && c.minPower <= 1)) {
            bad.add("minPower=" + c.minPower);
        }
        if (!(c.followDistanceCm > 0)) {
            bad.add("followDistanceCm=" + c.followDistanceCm);
        }
        if (!(c.arrivalThresholdCm > 0)) {
            bad.add("arrivalThresholdCm=" + c.arrivalThresholdCm);
        }
        if (!(c.orbitThresholdCm > 0)) {
            bad.add("orbitThresholdCm=" + c.orbitThresholdCm);
        }
        if (!(c.timeoutSecs > 0)) {
            bad.add("timeoutSecs=" + c.timeoutSecs);
        }
        if (!(c.wheelDiameterIn > 0)) {
            bad.add("wheelDiameterIn=" + c.wheelDiameterIn);
        }
        if (!(c.ticksPerRev > 0)) {
            bad.add("ticksPerRev=" + c.ticksPerRev);
        }
        if (!(c.trackWidthIn > 0)) {
            bad.add("trackWidthIn=" + c.trackWidthIn);
        }
        if (!bad.isEmpty()) {
            errors.add(CrawlerErrors.exception(CrawlerError.SETUP_INVALID_CONFIG, join(bad)));
        }
    }

    private static void collectPoseInitialized(CrawlerRobot robot,
                                               List<CrawlerErrorException> errors) {
        if (!robot.isPoseInitialized()) {
            errors.add(CrawlerErrors.exception(CrawlerError.SETUP_NO_START_POSE));
        }
    }

    private static void collectLocalizerHealth(CrawlerRobot robot,
                                               List<CrawlerErrorException> errors) {
        try {
            robot.update();
            Pose2d pose = robot.getPose();
            if (!Double.isFinite(pose.getX()) || !Double.isFinite(pose.getY())
                    || !Double.isFinite(pose.getHeading())) {
                errors.add(CrawlerErrors.exception(CrawlerError.ODO_NON_FINITE_POSE));
            }
        } catch (CrawlerErrorException e) {
            errors.add(e);
        } catch (RuntimeException e) {
            errors.add(CrawlerErrors.exception(CrawlerError.ODO_NON_FINITE_POSE));
        }
    }

    private static void collectImu(CrawlerRobot robot, List<CrawlerErrorException> errors) {
        try {
            if (robot.imu == null) {
                throw new IllegalStateException("imu is null");
            }
            robot.imu.getRobotYawPitchRollAngles();
        } catch (RuntimeException e) {
            errors.add(CrawlerErrors.exception(CrawlerError.ODO_IMU_NOT_RESPONDING));
        }
    }

    private static void collectPath(List<Waypoint> waypoints, List<CrawlerErrorException> errors) {
        if (waypoints == null || waypoints.isEmpty()) {
            errors.add(CrawlerErrors.exception(CrawlerError.PATH_EMPTY));
            return;
        }
        if (waypoints.size() < 2) {
            errors.add(CrawlerErrors.exception(CrawlerError.PATH_TOO_SHORT, waypoints.size()));
            return;
        }
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint w = waypoints.get(i);
            if (w == null) {
                errors.add(CrawlerErrors.exception(CrawlerError.PATH_NULL_WAYPOINT, i));
                continue;
            }
            if (!Double.isFinite(w.x) || !Double.isFinite(w.y)) {
                errors.add(CrawlerErrors.exception(
                        CrawlerError.PATH_NON_FINITE_WAYPOINT, w.x, w.y));
            }
            if (!(w.moveSpeed > 0 && w.moveSpeed <= 1)) {
                errors.add(CrawlerErrors.exception(CrawlerError.PATH_BAD_SPEED, w.moveSpeed));
            }
            if (i > 0 && waypoints.get(i - 1) != null) {
                Waypoint prev = waypoints.get(i - 1);
                if (Math.hypot(prev.x - w.x, prev.y - w.y) < 1e-9) {
                    errors.add(CrawlerErrors.exception(
                            CrawlerError.PATH_DUPLICATE_WAYPOINT,
                            prev.x, prev.y, w.x, w.y));
                }
            }
        }
    }

    private static String join(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i));
        }
        return sb.toString();
    }
}
