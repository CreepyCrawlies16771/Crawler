package org.firstinspires.ftc.teamcode.Crawler.core.utils;

/**
 * Converts between the units Crawler uses.
 *
 * <p>Crawler's <b>field geometry</b> is always <b>centimeters</b>: waypoint coordinates,
 * {@code Pose2d} X/Y, follow distance, and the arrival/orbit thresholds are all cm.
 * The <b>odometry hardware</b> constants on the builder are <b>inches</b>: track width,
 * center wheel offset, and wheel diameter. This class is the bridge between the two —
 * measure your robot in inches with a ruler, convert to cm for the field, or convert
 * your pod size into the builder's expected unit.</p>
 */
public final class UnitConverter {

    /** Inches per centimeter (2.54 cm in 1 in). */
    public static final double INCHES_PER_CM = 0.39370078740157477;

    /** Centimeters per inch (1 in = 2.54 cm). */
    public static final double CM_PER_INCH = 2.54;

    private UnitConverter() {}

    /** Converts inches to centimeters. */
    public static double inToCm(double inches) {
        return inches * CM_PER_INCH;
    }

    /** Converts centimeters to inches. */
    public static double cmToIn(double cm) {
        return cm * INCHES_PER_CM;
    }

    /** Converts meters to centimeters. */
    public static double mToCm(double meters) {
        return meters * 100.0;
    }

    /** Converts centimeters to meters. */
    public static double cmToM(double cm) {
        return cm / 100.0;
    }

    /** Converts millimeters to centimeters. */
    public static double mmToCm(double mm) {
        return mm / 10.0;
    }

    /** Converts centimeters to millimeters. */
    public static double cmToMm(double cm) {
        return cm * 10.0;
    }

    /** Converts feet to centimeters (e.g. field dimensions). */
    public static double ftToCm(double feet) {
        return feet * 12.0 * CM_PER_INCH;
    }

    /** Converts centimeters to feet. */
    public static double cmToFt(double cm) {
        return cm / (12.0 * CM_PER_INCH);
    }
}
