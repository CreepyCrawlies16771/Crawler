package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;

public interface CrawlerLocaliser {
    void update();
    Pose2d getPose();

    void resetPose(Pose2d pose);
}
