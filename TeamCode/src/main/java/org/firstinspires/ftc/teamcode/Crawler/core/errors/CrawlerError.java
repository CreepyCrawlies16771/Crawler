package org.firstinspires.ftc.teamcode.Crawler.core.errors;

/**
 * The Crawler error catalog.
 *
 * <p>Every error message Crawler can produce lives here so that codes stay unique
 * and searchable (Discord, Chief Delphi, docs), every message is written for a
 * first-time student, and every fix is stated as an action with a code snippet
 * where possible.</p>
 *
 * <p>Rendering follows the spec in {@code docs/errors.md}:</p>
 * <pre>{@code
 * CRWL-<CATEGORY><NUMBER>: <what happened, in plain English>
 * → <why it likely happened + exact fix, with a code snippet if the fix is code>
 * → Error found in <filename> at <Linenumber>! (IN RED)
 * }</pre>
 *
 * <p>Each entry has an {@code args} contract: message/fix templates containing
 * {@code %s}/{@code %d}/{@code %.1f} placeholders must be passed the matching
 * runtime values when reported (see {@link CrawlerErrors#throwError}).</p>
 */
public enum CrawlerError {

    // ------------------------------------------------------------------ 1xx
    // Setup — hardware map names, missing init calls, config errors.
    // ------------------------------------------------------------------

    SETUP_NO_START_POSE(Category.SETUP, 101,
            "Can't start following a path — the robot's starting position was never set.",
            "Call robot.startPose(x, y, heading) — or robot.resetPose() — once in init() before running any path.",
            true),

    SETUP_MOTOR_NAMES_MISSING(Category.SETUP, 102,
            "Can't build the robot — a drive motor name is missing.",
            "Call .frontLeft(\"fl\").frontRight(\"fr\").backLeft(\"bl\").backRight(\"br\") in your builder before .motors() or .build().",
            true),

    SETUP_IMU_NAME_MISSING(Category.SETUP, 103,
            "Can't build the robot — no IMU name was set.",
            "Call .imu(\"imu\") in your builder before .build().",
            true),

    SETUP_DEVICE_NOT_FOUND(Category.SETUP, 104,
            "Can't find hardware device \"%s\" in the configuration.",
            "Open the Driver Hub's Configure Robot screen, fix the name spelling/case of \"%s\", and keep MyRobot.java in sync.",
            true),

    SETUP_LOCALIZER_CONFIG_MISSING(Category.SETUP, 105,
            "Can't build the %s localizer — required tuning values are missing or invalid.",
            "Call .setTrackWidth(...), .wheelDiameter(...) and .ticksPerRev(...) in your builder (plus .setCenterWheelOffset(...) for three dead wheels) before .build().",
            true),

    SETUP_PINPOINT_CONFIG_MISSING(Category.SETUP, 106,
            "Can't build the Pinpoint localizer — its device name or config is missing.",
            "Call .withPinpoint(\"odo\") then .setConfig(x, y, DistanceUnit.CM, GoBildaOdometryPods.GO_BILDA_4_BAR, ...) in your builder before .build().",
            true),

    SETUP_INVALID_CONFIG(Category.SETUP, 107,
            "Your robot config has an invalid value: %s",
            "Open MyRobot.builder() (or the Crawler Tuner Dashboard panel) and set speeds between 0 and 1 and distances/timeouts greater than 0, then rebuild.",
            true),

    // ------------------------------------------------------------------ 2xx
    // Odometry — disconnected/unmoving encoders, uncalibrated IMU,
    // reversed wheel direction, drift.
    // ------------------------------------------------------------------

    ODO_IMU_NOT_RESPONDING(Category.ODOMETRY, 201,
            "The IMU is not responding — heading and path following will not work.",
            "Check the .imu(\"...\") device name and wiring, then hold the robot still for 2 seconds after power-on before running autonomous.",
            true),

    ODO_ENCODERS_NOT_MOVING(Category.ODOMETRY, 202,
            "Odometry reports no movement — the robot was told to drive but the pose is frozen.",
            "Check the odometry pod wiring and encoder names (run the Crawler Tuner Step 2 and confirm tick counts change when you push the robot), and make sure the robot isn't blocked or the pods aren't slipping.",
            true),

    ODO_REVERSED_DIRECTION(Category.ODOMETRY, 203,
            "Reversed odometry direction — the pose estimate moves opposite to the real robot.",
            "Invert the offending encoder in your builder, e.g. .withThreeDeadWheels(\"enc_l\", \"enc_r\", \"enc_c\").setTrackWidth(13.0).invertLeftEncoder() — see the tuner's Motors/Encoders steps.",
            false),

    ODO_DRIFT_DETECTED(Category.ODOMETRY, 204,
            "Odometry drifts during the spin test — track width or center-pod offset is off.",
            "Adjust trackWidthIn (Step 3) or centerWheelOffsetIn (Step 4) in the tuner until the drift is under 5 degrees.",
            false),

    ODO_NON_FINITE_POSE(Category.ODOMETRY, 205,
            "The localizer produced a non-finite pose (NaN or infinity) — odometry data is bad.",
            "Check the encoder wiring and the wheelDiameterIn / ticksPerRev values, then call robot.resetPose() and retry.",
            true),

    // ------------------------------------------------------------------ 3xx
    // Path definition — empty paths, invalid/unreachable waypoints, bad units.
    // ------------------------------------------------------------------

    PATH_EMPTY(Category.PATH, 301,
            "follow() was called with a null or empty path.",
            "Pass at least two waypoints: follower.follow(Waypoint.at(0, 0, robot.config).build(), Waypoint.at(60, 0, robot.config).build());",
            true),

    PATH_TOO_SHORT(Category.PATH, 302,
            "follow() needs at least 2 waypoints, got %d.",
            "Add a second waypoint to the follow(...) call — a path needs a start and an end.",
            true),

    PATH_NON_FINITE_WAYPOINT(Category.PATH, 303,
            "Waypoint (%s, %s) is not a valid position — the coordinates are NaN or infinite.",
            "Use real field coordinates in Waypoint.at(x, y, robot.config), never NaN or Infinity.",
            true),

    PATH_BAD_SPEED(Category.PATH, 304,
            "Waypoint speed %s is out of range — motor power must be between 0 and 1.",
            "Use .speed(0.0 ... 1.0) or remove it and let robot.config.defaultMoveSpeed apply.",
            true),

    PATH_DUPLICATE_WAYPOINT(Category.PATH, 305,
            "Consecutive waypoints (%s, %s) and (%s, %s) are the same point — the path has a zero-length segment.",
            "Remove the duplicate waypoint or give the second one a different coordinate.",
            true),

    PATH_NULL_WAYPOINT(Category.PATH, 306,
            "follow() was given a null waypoint at index %d.",
            "Build every waypoint with Waypoint.at(x, y, robot.config).build() and pass the built objects.",
            true),

    // ------------------------------------------------------------------ 4xx
    // Runtime — overlapping follow() calls, timeouts, out-of-range motor power.
    // ------------------------------------------------------------------

    RUNTIME_OVERLAPPING_FOLLOW(Category.RUNTIME, 401,
            "follow() was called while another path was still being followed.",
            "Call follower.follow(...) once per run — do not nest or overlap follow() calls.",
            true),

    RUNTIME_LEG_TIMEOUT(Category.RUNTIME, 402,
            "A waypoint was not reached before the %.1fs timeout — the leg was aborted.",
            "Raise robot.config.timeoutSecs, lower the move speed near the target, or check the waypoint is reachable (not inside a wall).",
            false),

    RUNTIME_NON_FINITE_POWER(Category.RUNTIME, 403,
            "drive() received a non-finite power value (%s) — the motors would get garbage.",
            "Guard your input, e.g. double f = Double.isNaN(forward) ? 0 : forward;",
            true);

    private final Category category;
    private final int number;
    private final String message;
    private final String fix;
    private final boolean thrown;

    CrawlerError(Category category, int number, String message, String fix, boolean thrown) {
        if (category.prefix != number / 100) {
            throw new IllegalArgumentException(
                    "Number " + number + " does not match category " + category.label);
        }
        this.category = category;
        this.number = number;
        this.message = message;
        this.fix = fix;
        this.thrown = thrown;
    }

    /** The category this error belongs to (1xx Setup … 4xx Runtime). */
    public Category category() { return category; }

    /** The numeric part of the code, e.g. 101. */
    public int number() { return number; }

    /** The full searchable code, e.g. {@code CRWL-101}. */
    public String code() { return "CRWL-" + number; }

    /** "What happened" line template (may contain {@code String.format} placeholders). */
    public String message() { return message; }

    /** "Why + fix" line template (may contain {@code String.format} placeholders). */
    public String fix() { return fix; }

    /** {@code true} = reported by throwing (stops the OpMode); {@code false} = telemetry warning only. */
    public boolean isThrown() { return thrown; }

    /** Error categories, mirroring the spec's numbering table. */
    public enum Category {
        SETUP("Setup", 1),
        ODOMETRY("Odometry", 2),
        PATH("Path definition", 3),
        RUNTIME("Runtime", 4);

        private final String label;
        private final int prefix;

        Category(String label, int prefix) {
            this.label = label;
            this.prefix = prefix;
        }

        public String label() { return label; }
    }
}
