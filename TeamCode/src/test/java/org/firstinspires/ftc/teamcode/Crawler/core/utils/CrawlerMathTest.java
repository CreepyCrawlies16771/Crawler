package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Unit tests for {@link CrawlerMath} — pure math used by the pathing library.
 * These run on the JVM (no robot hardware required).
 */
public class CrawlerMathTest {

    private static final double DELTA = 1e-6;

    // ------------------------------------------------------------------
    // wrapAngle
    // ------------------------------------------------------------------

    @Test
    public void wrapAngle_identity() {
        assertEquals(0, CrawlerMath.wrapAngle(0), DELTA);
        assertEquals(90, CrawlerMath.wrapAngle(90), DELTA);
        assertEquals(180, CrawlerMath.wrapAngle(180), DELTA);
        assertEquals(-180, CrawlerMath.wrapAngle(-180), DELTA);
    }

    @Test
    public void wrapAngle_wrapsBackwards() {
        assertEquals(-170, CrawlerMath.wrapAngle(190), DELTA);
        assertEquals(-10, CrawlerMath.wrapAngle(350), DELTA);
    }

    @Test
    public void wrapAngle_wrapsForwards() {
        assertEquals(170, CrawlerMath.wrapAngle(-190), DELTA);
        assertEquals(10, CrawlerMath.wrapAngle(-350), DELTA);
    }

    @Test
    public void wrapAngle_multipleRevolutions() {
        assertEquals(180, CrawlerMath.wrapAngle(540), DELTA);
        assertEquals(-180, CrawlerMath.wrapAngle(-540), DELTA);
        assertEquals(0, CrawlerMath.wrapAngle(720), DELTA);
        assertEquals(-80, CrawlerMath.wrapAngle(1000), DELTA);
    }

    // ------------------------------------------------------------------
    // wrapRadians
    // ------------------------------------------------------------------

    @Test
    public void wrapRadians_identity() {
        assertEquals(0, CrawlerMath.wrapRadians(0), DELTA);
        assertEquals(Math.PI, CrawlerMath.wrapRadians(Math.PI), DELTA);
        assertEquals(-Math.PI, CrawlerMath.wrapRadians(-Math.PI), DELTA);
    }

    @Test
    public void wrapRadians_wraps() {
        assertEquals(-Math.PI / 2, CrawlerMath.wrapRadians(3 * Math.PI / 2), DELTA);
        assertEquals(Math.PI / 2, CrawlerMath.wrapRadians(-3 * Math.PI / 2), DELTA);
        assertEquals(0, CrawlerMath.wrapRadians(4 * Math.PI), DELTA);
        assertEquals(-Math.PI / 2, CrawlerMath.wrapRadians(7 * Math.PI / 2), DELTA);
    }

    // ------------------------------------------------------------------
    // clamp
    // ------------------------------------------------------------------

    @Test
    public void clamp_withinRange() {
        assertEquals(5, CrawlerMath.clamp(5, 0, 10), DELTA);
        assertEquals(-5, CrawlerMath.clamp(-5, -10, -1), DELTA);
    }

    @Test
    public void clamp_belowMin() {
        assertEquals(0, CrawlerMath.clamp(-5, 0, 10), DELTA);
        assertEquals(-10, CrawlerMath.clamp(-20, -10, -1), DELTA);
    }

    @Test
    public void clamp_aboveMax() {
        assertEquals(10, CrawlerMath.clamp(15, 0, 10), DELTA);
        assertEquals(-1, CrawlerMath.clamp(0, -10, -1), DELTA);
    }

    // ------------------------------------------------------------------
    // lineCircleIntersection
    // ------------------------------------------------------------------

    @Test
    public void lineCircle_intersectsHorizontalSegment() {
        ArrayList<Point> pts = CrawlerMath.lineCircleIntersection(
                new Point(0, 0), 5.0,
                new Point(-10, 0), new Point(10, 0));
        assertEquals(2, pts.size());
        for (Point p : pts) {
            assertEquals("point should lie on the circle", 25.0, p.x * p.x + p.y * p.y, 0.01);
            assertTrue("x should be near +-5", Math.abs(Math.abs(p.x) - 5.0) < 0.01);
        }
    }

    @Test
    public void lineCircle_intersectsVerticalSegment() {
        ArrayList<Point> pts = CrawlerMath.lineCircleIntersection(
                new Point(0, 0), 5.0,
                new Point(0, -10), new Point(0, 10));
        assertEquals(2, pts.size());
        for (Point p : pts) {
            assertEquals("point should lie on the circle", 25.0, p.x * p.x + p.y * p.y, 0.01);
            assertTrue("y should be near +-5", Math.abs(Math.abs(p.y) - 5.0) < 0.01);
        }
    }

    @Test
    public void lineCircle_noIntersection_returnsEmpty() {
        ArrayList<Point> pts = CrawlerMath.lineCircleIntersection(
                new Point(0, 0), 5.0,
                new Point(100, 0), new Point(200, 0));
        assertTrue(pts.isEmpty());
    }

    @Test
    public void lineCircle_intersectsGeneralSegment_regression() {
        // Regression test: the quadratic was missing the (y - cy)^2 term, which
        // only matters when the segment endpoints aren't horizontally aligned
        // with the circle center. The line y = 0.4x + 2 cuts the unit circle at
        // two points: near (-5, 0) and near (3.62, 3.45).
        ArrayList<Point> pts = CrawlerMath.lineCircleIntersection(
                new Point(0, 0), 5.0,
                new Point(-10, -2), new Point(10, 6));
        assertEquals(2, pts.size());
        for (Point p : pts) {
            assertEquals(25.0, p.x * p.x + p.y * p.y, 1e-6);
            assertTrue("point should lie on the segment",
                    p.x >= -10 && p.x <= 10 && p.y >= -2 && p.y <= 6);
        }
        assertEquals(3.62, pts.get(0).x, 0.01);
        assertEquals(3.45, pts.get(0).y, 0.01);
        assertEquals(-5.0, pts.get(1).x, 0.01);
        assertEquals(0.0, pts.get(1).y, 0.01);
    }

    @Test
    public void lineCircle_segmentEndpointsAreExcluded() {
        // Segment ends exactly on the circle; only the interior root is returned.
        ArrayList<Point> pts = CrawlerMath.lineCircleIntersection(
                new Point(0, 0), 5.0,
                new Point(-10, 0), new Point(0, 0));
        assertEquals(1, pts.size());
        assertTrue(Math.abs(pts.get(0).x + 5.0) < 0.01);
    }
}
