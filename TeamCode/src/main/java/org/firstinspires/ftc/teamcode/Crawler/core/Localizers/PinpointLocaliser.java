package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrors;

public class PinpointLocaliser implements CrawlerLocaliser {
    private final GoBildaPinpointDriver pinpoint;
    private final DistanceUnit distanceUnit;

    public PinpointLocaliser(HardwareMap hwMap, String deviceName,
                             double xOffset, double yOffset,
                             DistanceUnit distanceUnit,
                             GoBildaPinpointDriver.GoBildaOdometryPods pod,
                             GoBildaPinpointDriver.EncoderDirection xDirection,
                             GoBildaPinpointDriver.EncoderDirection yDirection) {

        try {
            pinpoint = hwMap.get(GoBildaPinpointDriver.class, deviceName);
        } catch (RuntimeException e) {
            CrawlerErrors.throwError(CrawlerError.SETUP_DEVICE_NOT_FOUND, deviceName);
            throw new AssertionError("unreachable");
        }
        this.distanceUnit = distanceUnit;

        pinpoint.setOffsets(xOffset, yOffset, distanceUnit);
        pinpoint.setEncoderResolution(pod);
        pinpoint.setEncoderDirections(xDirection, yDirection);
    }

    @Override
    public void update() {
        pinpoint.update();
    }

    @Override
    public Pose2d getPose() {
        Pose2D raw = pinpoint.getPosition();
        // Library pose is always centimeters.
        return new Pose2d(
                raw.getX(DistanceUnit.CM),
                raw.getY(DistanceUnit.CM),
                new Rotation2d(raw.getHeading(AngleUnit.RADIANS))
        );
    }

    @Override
    public void resetPose(Pose2d pose) {
        pinpoint.resetPosAndIMU();
    }
}