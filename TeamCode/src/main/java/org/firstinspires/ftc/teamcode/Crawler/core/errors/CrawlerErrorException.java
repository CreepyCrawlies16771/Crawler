package org.firstinspires.ftc.teamcode.Crawler.core.errors;

/**
 * An error detected by Crawler, carrying its {@link CrawlerError} code and the
 * source location where the problem was found.
 *
 * <p>Constructed by {@link CrawlerErrors#exception(CrawlerError, Object...)} so the
 * location points at the check that fired — not at this class. The {@code message}
 * is pre-rendered in the CRWL spec format:</p>
 * <pre>{@code
 * CRWL-101: Can't start following a path — the robot's starting position was never set.
 * → Call robot.startPose(x, y, heading) — or robot.resetPose() — once in init() before running any path.
 * → Error found in <filename> at <Linenumber>! (IN RED)
 * }</pre>
 */
public class CrawlerErrorException extends RuntimeException {

    /** The catalog entry for this error. */
    public final CrawlerError error;

    /** The {@code file:line} where Crawler detected the problem. */
    public final StackTraceElement location;

    CrawlerErrorException(String message, CrawlerError error, StackTraceElement location) {
        super(message);
        this.error = error;
        this.location = location;
    }

    /** The searchable code, e.g. {@code CRWL-101}. */
    public String code() {
        return error.code();
    }
}
