package org.firstinspires.ftc.teamcode.Crawler.core;

import com.acmerobotics.dashboard.config.Config;

public class RobotConfig {

    // -----------------------------------------------------------------------
    // Hardware names — package-private
    // Only CrawlerRobot reads these. Teams configure them via MyRobot.create()
    // by passing strings into the builder. They are stored here so CrawlerRobot
    // can validate them in one place.
    // -----------------------------------------------------------------------
    static String FRONT_LEFT     = "frontLeft"; // Defaulted to match your old Robot.java
    static String FRONT_RIGHT    = "frontRight";
    static String BACK_LEFT      = "backLeft";
    static String BACK_RIGHT     = "backRight";
    static String IMU_NAME       = "imu";
    static String LEFT_ENCODER   = "enc_l";
    static String RIGHT_ENCODER  = "enc_r";
    static String CENTER_ENCODER = "enc_c";

    // -----------------------------------------------------------------------
    // Physical constants — public, teams must measure and set these
    // -----------------------------------------------------------------------
    @Config
    public static class Odometry {
        public static double TRACK_WIDTH          = 13.0;    // inches
        public static double CENTER_WHEEL_OFFSET  = 3.5;     // inches
        public static double WHEEL_DIAMETER       = 1.37795; // inches (35mm pod)
        public static double TICKS_PER_REV        = 2000;
    }

    // -----------------------------------------------------------------------
    // Robot-oriented PID — public, tuned via FTC Dashboard
    // -----------------------------------------------------------------------
    @Config
    public static class RobotOriented {
        public static double Kp        = 0.05;
        public static double Ki        = 0.0;
        public static double Kd        = 0.0;

        public static double strafe_Kp = 0.05;
        public static double strafe_Ki = 0.0;
        public static double strafe_Kd = 0.0;

        public static double STEER_P   = 0.03;
        public static double STEER_I   = 0.0;
        public static double STEER_D   = 0.0;

        // Minimum power to overcome drivebase friction
        public static double MIN_POWER = 0.15;
    }

    // -----------------------------------------------------------------------
    // Field-oriented follower — public, tuned via FTC Dashboard
    // -----------------------------------------------------------------------
    @Config
    public static class FieldOriented {
        public static double DEFAULT_MOVE_SPEED             = 0.7;
        public static double DEFAULT_TURN_SPEED             = 0.4;
        public static double DEFAULT_FOLLOW_DISTANCE        = 10.0;  // inches
        public static double DEFAULT_FOLLOW_ANGLE           = 0.0;   // radians
        public static double ARRIVAL_THRESHOLD              = 2.0;   // inches
        public static double ORBIT_THRESHOLD                = 10.0;  // inches — turn fades below this
        public static double SLOW_MOVE_SPEED                = 0.3;
        public static double SLOW_TURN_SPEED                = 0.2;
        public static double SLOW_FOLLOW_DISTANCE           = 5.0;
        public static double DEFAULT_SLOW_DOWN_TURN_RADIANS = 0.5;
        public static double DEFAULT_SLOW_DOWN_TURN_AMOUNT  = 0.5;
    }

    // -----------------------------------------------------------------------
    // Robot base — not tuned, physical hardware limits & general bounds
    // -----------------------------------------------------------------------
    public static class RobotBase {
        public static double MAX_DRIVE_SPEED = 1.0;
        public static double MIN_DRIVE_SPEED = 0.1;

        // ROMovementEngine conversion & safety constants
        public static double TICKS_PER_METER = 2000.0; // Adjust to your physical drivebase
        public static double TICKS_PER_CM = TICKS_PER_METER / 100;
        public static double timeoutSecs     = 5.0;    // Prevent infinite PID loops

        // --- TEMPORARY SEASON HARDWARE (Migrate to MyRobot.java later) ---
        public static double maxShooterSpeed = 0.8;
        public static double maxGobblerSpeed = 0.8;
    }
}