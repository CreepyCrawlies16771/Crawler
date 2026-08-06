package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Unit tests for {@link Vector2d} — pure 2D vector math. */
public class Vector2dTest {

    private static final double DELTA = 1e-9;

    @Test
    public void magnitude() {
        assertEquals(5.0, new Vector2d(3, 4).magnitude(), DELTA);
        assertEquals(0.0, new Vector2d(0, 0).magnitude(), DELTA);
    }

    @Test
    public void distanceTo() {
        assertEquals(5.0, new Vector2d(0, 0).distanceTo(new Vector2d(3, 4)), DELTA);
        assertEquals(1.0, new Vector2d(1, 1).distanceTo(new Vector2d(2, 1)), DELTA);
    }

    @Test
    public void normalized_unitVector() {
        Vector2d n = new Vector2d(3, 4).normalized();
        assertEquals(0.6, n.x, DELTA);
        assertEquals(0.8, n.y, DELTA);
        assertEquals(1.0, n.magnitude(), DELTA);
    }

    @Test
    public void normalized_zeroVector_returnsZero() {
        Vector2d n = new Vector2d(0, 0).normalized();
        assertEquals(0.0, n.x, DELTA);
        assertEquals(0.0, n.y, DELTA);
    }

    @Test
    public void plus() {
        Vector2d v = new Vector2d(1, 2).plus(new Vector2d(3, 4));
        assertEquals(4.0, v.x, DELTA);
        assertEquals(6.0, v.y, DELTA);
    }

    @Test
    public void minus() {
        Vector2d v = new Vector2d(3, 4).minus(new Vector2d(1, 2));
        assertEquals(2.0, v.x, DELTA);
        assertEquals(2.0, v.y, DELTA);
    }

    @Test
    public void times() {
        Vector2d v = new Vector2d(2, 3).times(2);
        assertEquals(4.0, v.x, DELTA);
        assertEquals(6.0, v.y, DELTA);
        assertEquals(0.0, new Vector2d(5, -1).times(0).magnitude(), DELTA);
    }

    @Test
    public void dot() {
        assertEquals(11.0, new Vector2d(1, 2).dot(new Vector2d(3, 4)), DELTA);
        assertEquals(0.0, new Vector2d(1, 0).dot(new Vector2d(0, 1)), DELTA);
    }

    @Test
    public void angleTo() {
        assertEquals(0.0, new Vector2d(0, 0).angleTo(new Vector2d(1, 0)), DELTA);
        assertEquals(Math.PI / 2, new Vector2d(0, 0).angleTo(new Vector2d(0, 1)), DELTA);
        assertEquals(Math.PI, new Vector2d(0, 0).angleTo(new Vector2d(-1, 0)), DELTA);
    }

    @Test
    public void toPoint() {
        Point p = new Vector2d(1.5, -2.5).toPoint();
        assertEquals(1.5, p.x, DELTA);
        assertEquals(-2.5, p.y, DELTA);
    }

    @Test
    public void toString_format() {
        assertTrue(new Vector2d(1, 2).toString().contains("1.0"));
    }
}
