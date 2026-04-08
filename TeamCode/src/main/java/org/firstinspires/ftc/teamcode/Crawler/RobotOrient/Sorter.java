package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import com.qualcomm.robotcore.hardware.HardwareMap;

/**
 * @deprecated This class uses the old Robot.java which has been removed.
 * Use CrawlerRobot and CrawlerTeleOp instead for new robot control code.
 *
 * This file is retained for reference only and should not be used in new code.
 */
@Deprecated()
public class Sorter {

    int tries = 3;

    /**
     * @deprecated Constructor no longer functional after Robot.java removal.
     */
    public Sorter(HardwareMap hwMap) {
        // Robot.java has been removed. Use CrawlerRobot for new code.
    }

    /**
     * Enumeration for ball color detection.
     *
     * @deprecated Use appropriate sensor integration with CrawlerRobot instead.
     */
    public enum BALLCOLOR {
        RED,
        PURPLE,
        GREEN,
        UNKNOWN
    }
}
