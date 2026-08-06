package org.firstinspires.ftc.teamcode.Crawler.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.firstinspires.ftc.teamcode.Crawler.core.Robot.CrawlerRobot;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerError;
import org.firstinspires.ftc.teamcode.Crawler.core.errors.CrawlerErrorException;
import org.junit.Test;

/** Unit tests for {@link Waypoint} and its builder. */
public class WaypointTest {

    private static final double DELTA = 1e-9;

    @Test
    public void defaults_useRobotConfig() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        Waypoint w = Waypoint.at(10, 20, config).build();

        assertEquals(10.0, w.x, DELTA);
        assertEquals(20.0, w.y, DELTA);
        assertEquals(0.0, w.heading, DELTA);
        assertEquals(config.defaultMoveSpeed, w.moveSpeed, DELTA);
        assertEquals(config.defaultTurnSpeed, w.turnSpeed, DELTA);
        assertEquals(config.followDistanceCm, w.followDistance, DELTA);
        assertEquals(config.slowDownTurnRadians, w.slowDownTurnRadians, DELTA);
        assertEquals(config.slowDownTurnAmount, w.slowDownTurnAmount, DELTA);
    }

    @Test
    public void builder_overrides() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        config.defaultMoveSpeed = 0.55;

        Waypoint w = Waypoint.at(0, 0, config)
                .heading(90)
                .speed(0.8)
                .turnSpeed(0.25)
                .followDistance(15.0)
                .build();

        assertEquals(90.0, w.heading, DELTA);
        assertEquals(0.8, w.moveSpeed, DELTA);
        assertEquals(0.25, w.turnSpeed, DELTA);
        assertEquals(15.0, w.followDistance, DELTA);
    }

    @Test
    public void slow_appliesSlowConfig() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        config.slowMoveSpeed = 0.3;
        config.slowTurnSpeed = 0.2;
        config.slowFollowDistanceCm = 12.7;

        Waypoint w = Waypoint.at(1, 1, config).slow(config).build();

        assertEquals(0.3, w.moveSpeed, DELTA);
        assertEquals(0.2, w.turnSpeed, DELTA);
        assertEquals(12.7, w.followDistance, DELTA);
    }

    @Test
    public void slowDown_setsBothValues() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        Waypoint w = Waypoint.at(0, 0, config).slowDown(0.3, 0.7).build();
        assertEquals(0.3, w.slowDownTurnRadians, DELTA);
        assertEquals(0.7, w.slowDownTurnAmount, DELTA);
    }

    @Test
    public void onReach_runsCallback() {
        final int[] hits = {0};
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        Waypoint w = Waypoint.at(5, 5, config).onReach(new Runnable() {
            @Override public void run() { hits[0]++; }
        }).build();

        assertNotNull(w.onReach);
        w.onReach.run();
        assertEquals(1, hits[0]);
    }

    @Test
    public void copyConstructor_copiesFields() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        Waypoint original = Waypoint.at(3, 4, config).heading(45).speed(0.6).build();
        Waypoint copy = new Waypoint(original);

        assertEquals(original.x, copy.x, DELTA);
        assertEquals(original.y, copy.y, DELTA);
        assertEquals(original.heading, copy.heading, DELTA);
        assertEquals(original.moveSpeed, copy.moveSpeed, DELTA);
        assertSame(original.onReach, copy.onReach);
    }

    @Test
    public void at_nullConfig_throws() {
        try {
            Waypoint.at(1, 1, null);
            fail("Expected IllegalArgumentException for null config");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void conversions() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        Waypoint w = Waypoint.at(2, 3, config).build();

        Vector2d v = w.toVector();
        assertEquals(2.0, v.x, DELTA);
        assertEquals(3.0, v.y, DELTA);

        Point p = w.toPoint();
        assertEquals(2.0, p.x, DELTA);
        assertEquals(3.0, p.y, DELTA);
    }

    // -----------------------------------------------------------------------
    // Error-spec validation (CRWL-303 / CRWL-304)
    // -----------------------------------------------------------------------

    @Test
    public void build_nanCoordinate_throwsNonFiniteWaypoint() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        assertBuildThrows(CrawlerError.PATH_NON_FINITE_WAYPOINT,
                () -> Waypoint.at(Double.NaN, 10, config).build());
        assertBuildThrows(CrawlerError.PATH_NON_FINITE_WAYPOINT,
                () -> Waypoint.at(10, Double.POSITIVE_INFINITY, config).build());
        assertBuildThrows(CrawlerError.PATH_NON_FINITE_WAYPOINT,
                () -> Waypoint.at(10, 20, config).heading(Double.NaN).build());
    }

    @Test
    public void build_badSpeeds_throwBadSpeed() {
        CrawlerRobot.Config config = new CrawlerRobot.Config();
        assertBuildThrows(CrawlerError.PATH_BAD_SPEED,
                () -> Waypoint.at(0, 0, config).speed(0).build());
        assertBuildThrows(CrawlerError.PATH_BAD_SPEED,
                () -> Waypoint.at(0, 0, config).speed(1.5).build());
        assertBuildThrows(CrawlerError.PATH_BAD_SPEED,
                () -> Waypoint.at(0, 0, config).speed(Double.NaN).build());
        assertBuildThrows(CrawlerError.PATH_BAD_SPEED,
                () -> Waypoint.at(0, 0, config).turnSpeed(0).build());
        assertBuildThrows(CrawlerError.PATH_BAD_SPEED,
                () -> Waypoint.at(0, 0, config).turnSpeed(-0.2).build());
    }

    private static void assertBuildThrows(CrawlerError expected, Runnable build) {
        try {
            build.run();
            fail("build() was expected to throw " + expected.code());
        } catch (CrawlerErrorException e) {
            assertEquals(expected, e.error);
        }
    }
}
