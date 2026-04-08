package org.firstinspires.ftc.teamcode.Crawler.core.Localizers;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

/**
 * GoBilda Pinpoint dead-reckoning localizer.
 *
 * <p>The Pinpoint hardware always resets to (0, 0, 0) when resetPosAndIMU() is called.
 * This class manages a manual offset to support arbitrary starting positions while
 * maintaining a clean internal representation.</p>
 */
public class PinpointLocaliser implements CrawlerLocaliser {
    private final GoBildaPinpointDriver pinpoint;
    private final DistanceUnit distanceUnit;
    private Pose2d poseOffset = new Pose2d();

    /**
     * Creates a Pinpoint localizer with hardware configuration.
     *
     * @param hwMap the hardware map
     * @param deviceName the name of the Pinpoint device in the hardware map
     * @param xOffset the x offset from the center of the robot (in distanceUnit)
     * @param yOffset the y offset from the center of the robot (in distanceUnit)
     * @param distanceUnit the unit for all distance measurements (typically INCH)
     * @param pod the odometry pod configuration (pod type and resolution)
     * @param xDirection the encoder direction for the X sensor
     * @param yDirection the encoder direction for the Y sensor
     */
    public PinpointLocaliser(HardwareMap hwMap, String deviceName,
                             double xOffset, double yOffset,
                             DistanceUnit distanceUnit,
                             GoBildaPinpointDriver.GoBildaOdometryPods pod,
                             GoBildaPinpointDriver.EncoderDirection xDirection,
                             GoBildaPinpointDriver.EncoderDirection yDirection) {

        pinpoint = hwMap.get(GoBildaPinpointDriver.class, deviceName);
        this.distanceUnit = distanceUnit;

        pinpoint.setOffsets(xOffset, yOffset, distanceUnit);
        pinpoint.setEncoderResolution(pod);
        pinpoint.setEncoderDirections(xDirection, yDirection);
    }

    /**
     * Updates the Pinpoint state.
     */
    @Override
    public void update() {
        pinpoint.update();
    }

    /**
     * Gets the current pose estimate, adjusted by the configured offset.
     *
     * <p>Since Pinpoint hardware always resets to (0, 0, 0), this method
     * applies the stored offset to support arbitrary start positions.</p>
     *
     * @return the current Pose2d (x, y, heading) adjusted by the offset
     */
    @Override
    public Pose2d getPose() {
        Pose2D raw = pinpoint.getPosition();
        return new Pose2d(
                raw.getX(distanceUnit) + poseOffset.getX(),
                raw.getY(distanceUnit) + poseOffset.getY(),
                new Rotation2d(raw.getHeading(AngleUnit.RADIANS) + poseOffset.getHeading())
        );
    }

    /**
     * Resets the Pinpoint to (0, 0, 0) and stores the given pose as an offset.
     *
     * <p>Since the Pinpoint hardware can only reset to (0, 0, 0), we reset the
     * hardware and store the desired pose as an offset. Subsequent getPose() calls
     * will add this offset to the hardware's reading.</p>
     *
     * @param pose the pose to reset to, or null for (0, 0, 0)
     */
    @Override
    public void resetPose(Pose2d pose) {
        // Pinpoint hardware always resets to (0, 0, 0)
        pinpoint.resetPosAndIMU();
        // Store the desired pose as an offset for future readings
        this.poseOffset = (pose != null) ? pose : new Pose2d();
    }
}