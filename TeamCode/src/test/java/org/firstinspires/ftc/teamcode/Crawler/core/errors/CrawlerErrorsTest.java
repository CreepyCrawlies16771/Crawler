package org.firstinspires.ftc.teamcode.Crawler.core.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Unit tests for {@link CrawlerErrors} rendering and exception behavior. */
public class CrawlerErrorsTest {

    @Test
    public void format_rendersSpecFormat() {
        StackTraceElement frame = new StackTraceElement(
                "com.example.MyAuto", "runOpMode", "MyAuto.java", 37);
        String rendered = CrawlerErrors.format(
                CrawlerError.SETUP_NO_START_POSE, new Object[0], frame);

        assertEquals(
                "CRWL-101: Can't start following a path — the robot's starting position was never set.\n"
                        + "→ Call robot.startPose(x, y, heading) — or robot.resetPose() — once in init() before running any path.\n"
                        + "→ Error found in MyAuto.java at 37! (IN RED)",
                rendered);
    }

    @Test
    public void format_substitutesArgs() {
        StackTraceElement frame = new StackTraceElement(
                "com.example.MyRobot", "builder", "MyRobot.java", 12);
        String rendered = CrawlerErrors.format(
                CrawlerError.SETUP_DEVICE_NOT_FOUND,
                new Object[]{"odo"},
                frame);

        assertTrue(rendered.startsWith("CRWL-104: Can't find hardware device \"odo\""));
        assertTrue(rendered.contains("fix the name spelling/case of \"odo\""));
    }

    @Test
    public void exception_messageUsesCallerFrame() {
        CrawlerErrorException e = CrawlerErrors.exception(CrawlerError.PATH_EMPTY);

        assertEquals("CRWL-301", e.code());
        assertTrue(e.getMessage().startsWith("CRWL-301: "));
        assertTrue("message must point at a real source frame",
                e.getMessage().contains(" at "));
        assertTrue(e.getMessage().endsWith("(IN RED)"));
    }

    @Test
    public void throwError_throwsCrawlerErrorException() {
        try {
            CrawlerErrors.throwError(CrawlerError.PATH_TOO_SHORT, 1);
            fail("expected CrawlerErrorException");
        } catch (CrawlerErrorException e) {
            assertEquals(CrawlerError.PATH_TOO_SHORT, e.error);
            assertTrue(e.getMessage().contains("got 1"));
        }
    }
}
