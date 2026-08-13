package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for {@link UnitConverter} — pure unit math, no robot hardware required. */
public class UnitConverterTest {

    private static final double DELTA = 1e-9;

    // ------------------------------------------------------------------
    // in ↔ cm
    // ------------------------------------------------------------------

    @Test
    public void inToCm_knownValue() {
        assertEquals(2.54, UnitConverter.inToCm(1.0), DELTA);
        assertEquals(13.0 * 2.54, UnitConverter.inToCm(13.0), DELTA);
        assertEquals(35.0 / 10.0 * 2.54, UnitConverter.inToCm(3.5), DELTA);
    }

    @Test
    public void cmToIn_knownValue() {
        assertEquals(1.0, UnitConverter.cmToIn(2.54), DELTA);
        assertEquals(1.37795, UnitConverter.cmToIn(UnitConverter.inToCm(1.37795)), DELTA);
    }

    @Test
    public void inCm_roundTrip() {
        for (double inches : new double[]{0.0, 1.0, 3.5, 13.0, 20.0}) {
            assertEquals(inches, UnitConverter.cmToIn(UnitConverter.inToCm(inches)), DELTA);
        }
    }

    // ------------------------------------------------------------------
    // m ↔ cm
    // ------------------------------------------------------------------

    @Test
    public void mToCm_knownValue() {
        assertEquals(100.0, UnitConverter.mToCm(1.0), DELTA);
        assertEquals(30.0, UnitConverter.mToCm(0.30), DELTA);
    }

    @Test
    public void cmToM_knownValue() {
        assertEquals(0.3, UnitConverter.cmToM(30.0), DELTA);
        assertEquals(1.0, UnitConverter.cmToM(100.0), DELTA);
    }

    @Test
    public void mCm_roundTrip() {
        assertEquals(1.25, UnitConverter.cmToM(UnitConverter.mToCm(1.25)), DELTA);
    }

    // ------------------------------------------------------------------
    // mm ↔ cm
    // ------------------------------------------------------------------

    @Test
    public void mmToCm_knownValue() {
        assertEquals(1.0, UnitConverter.mmToCm(10.0), DELTA);
        assertEquals(3.5, UnitConverter.mmToCm(35.0), DELTA);   // 35 mm pod
    }

    @Test
    public void cmToMm_knownValue() {
        assertEquals(35.0, UnitConverter.cmToMm(3.5), DELTA);
    }

    // ------------------------------------------------------------------
    // ft ↔ cm
    // ------------------------------------------------------------------

    @Test
    public void ftToCm_knownValue() {
        assertEquals(30.48, UnitConverter.ftToCm(1.0), DELTA);
        assertEquals(365.76, UnitConverter.ftToCm(12.0), DELTA);  // FTC field is 12 ft
    }

    @Test
    public void cmToFt_knownValue() {
        assertEquals(1.0, UnitConverter.cmToFt(30.48), DELTA);
    }

    // ------------------------------------------------------------------
    // Crawler-specific helpers
    // ------------------------------------------------------------------

    @Test
    public void builderInches_toFieldCm() {
        // A 13-inch track width in the units the pathing code thinks in.
        assertEquals(33.02, UnitConverter.inToCm(13.0), DELTA);
    }

    @Test
    public void drivePidMeters_toFieldCm() {
        // RobotOrientedDrive takes meters; the library works in cm.
        assertEquals(20.0, UnitConverter.mToCm(0.20), DELTA);
    }
}
