package org.firstinspires.ftc.teamcode.Crawler;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;

public class PleaseWorkd {

    CrawlerRobot robot = new CrawlerRobot.Builder(null)
            .frontLeft("fl")
            .frontRight("fr")
            .backLeft("bl")
            .backRight("br")
            .motors()
            .withThreeDeadWheels("enc_l", "enc_r", "enc_c")
            .setTrackWidth(13.0)
            .setCenterWheelOffset(3.5)
            .build();
}
