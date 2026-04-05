package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;

/// This is a localizer used to create pathing/follower code and not needing a robot or a proper localizer to do such
public class DevLocaliser  implements CrawlerLocaliser{


    private Pose2d pose;
    private long lastTime;

    public DevLocaliser() {
        pose = new Pose2d(0, 0, new Rotation2d(0));
        lastTime = System.nanoTime();
    }


    @Override
    public void update() {
       long now = System.nanoTime();
       double dt = (now - lastTime) / 1e9; //seconds
        lastTime = now;

        /* simple Fake motion
            Moves forward in X and slowly rotes
         */

        double speed = 0.5; //units per second
        double turnRate = 0.5; //radians per second

        double newX = pose.getX() + speed * dt;
        double newY = pose.getY();
        double newHeading = pose.getHeading() + turnRate * dt;

        pose = new Pose2d(newX, newY, new Rotation2d(newHeading));

    }

    @Override
    public Pose2d getPose() {
        return pose;
    }

    @Override
    public void resetPose() {
        pose = new Pose2d(0, 0, new Rotation2d(0));
        lastTime = System.nanoTime();
    }


}
