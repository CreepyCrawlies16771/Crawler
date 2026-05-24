package org.firstinspires.ftc.teamcode.Crawler.Tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import com.arcrobotics.ftclib.geometry.Pose2d;

/**
 * Driver Station + FTC Dashboard telemetry for tuning opmodes.
 */
public final class TuningTelemetry {
    private final Telemetry out;

    public TuningTelemetry(Telemetry driverStationTelemetry) {
        FtcDashboard dashboard = FtcDashboard.getInstance();
        this.out = new MultipleTelemetry(driverStationTelemetry, dashboard.getTelemetry());
    }

    public Telemetry get() {
        return out;
    }

    public void clear() {
        out.clear();
    }

    public void addLine(String line) {
        out.addLine(line);
    }

    public void addData(String caption, Object value) {
        out.addData(caption, value);
    }

    public void displayMovementDebug(Pose2d pose, double power, double error) {
        out.addData("Pose X (cm)", String.format("%.2f", pose.getX()));
        out.addData("Pose Y (cm)", String.format("%.2f", pose.getY()));
        out.addData("Heading (deg)", String.format("%.2f",
                Math.toDegrees(pose.getHeading())));
        out.addData("Error", String.format("%.2f", error));
        out.addData("Motor Power", String.format("%.3f", power));
    }

    public void update() {
        out.update();
    }
}
