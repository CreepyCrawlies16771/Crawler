package org.firstinspires.ftc.teamcode.Crawler.RobotOrient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Unit tests for {@link HeadingTimeline} — keyframe interpolation for headings. */
public class HeadingTimelineTest {

    private static final double DELTA = 1e-9;

    @Test
    public void singleKeyframe_returnsItsHeading() {
        HeadingTimeline t = new HeadingTimeline().at(1.0, 90);
        assertEquals(90.0, t.getTarget(1.0, 0.0), DELTA);
    }

    @Test
    public void linearInterpolation_betweenKeyframes() {
        HeadingTimeline t = new HeadingTimeline().at(0.0, 10).at(1.0, 20);
        assertEquals(10.0, t.getTarget(0.0, 0.0), DELTA);
        assertEquals(15.0, t.getTarget(0.5, 0.0), DELTA);
        assertEquals(20.0, t.getTarget(1.0, 0.0), DELTA);
    }

    @Test
    public void missingZeroKeyframe_usesStartHeading() {
        HeadingTimeline t = new HeadingTimeline().at(1.0, 180);
        assertEquals(0.0, t.getTarget(0.0, 0.0), DELTA);
        assertEquals(90.0, t.getTarget(0.5, 0.0), DELTA);
    }

    @Test
    public void angleWrapping_interpolatesShortWay() {
        // 10 -> 350 should interpolate through 0, not the long way around.
        HeadingTimeline t = new HeadingTimeline().at(0.0, 10).at(1.0, 350);
        assertEquals(0.0, t.getTarget(0.5, 0.0), DELTA);
        assertEquals(5.0, t.getTarget(0.25, 0.0), DELTA);
    }

    @Test
    public void pastLastKeyframe_returnsLastValue() {
        HeadingTimeline t = new HeadingTimeline().at(0.0, 10).at(0.5, 20);
        assertEquals(20.0, t.getTarget(1.0, 0.0), DELTA);
    }

    @Test
    public void percentages_areClampedToUnitRange() {
        HeadingTimeline t = new HeadingTimeline().at(2.0, 90).at(-1.0, 45);
        // 2.0 clamps to 1.0, -1.0 clamps to 0.0
        assertEquals(45.0, t.getTarget(0.0, 0.0), DELTA);
        assertEquals(90.0, t.getTarget(1.0, 0.0), DELTA);
    }
}
