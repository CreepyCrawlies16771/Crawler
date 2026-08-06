package org.firstinspires.ftc.teamcode.Crawler.core.errors;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Renders and reports {@link CrawlerError} catalog entries in the CRWL spec format.
 *
 * <p>Two ways to surface an error:</p>
 * <ul>
 *   <li>{@link #throwError(CrawlerError, Object...)} — fail fast; the OpMode stops
 *       and the Driver Station shows the red message.</li>
 *   <li>{@link #postToTelemetry(Telemetry, CrawlerError, Object...)} — non-fatal
 *       warning; the message appears in telemetry but the code keeps running.</li>
 * </ul>
 *
 * <p>Both render the caller's {@code file:line} so a rookie knows exactly where the
 * check fired.</p>
 */
public final class CrawlerErrors {

    private static final String ERROR_PACKAGE =
            "org.firstinspires.ftc.teamcode.Crawler.core.errors";

    private CrawlerErrors() {}

    /**
     * Builds a {@link CrawlerErrorException} for a code, pointing at the call site.
     *
     * @param error the catalog entry
     * @param args values for the {@code %s}/{@code %d}/{@code %.1f} placeholders in
     *             the entry's message/fix templates, in order (may be empty)
     */
    public static CrawlerErrorException exception(CrawlerError error, Object... args) {
        StackTraceElement frame = callerFrame();
        return new CrawlerErrorException(format(error, args, frame), error, frame);
    }

    /**
     * Throws a {@link CrawlerErrorException} for a code, pointing at the call site.
     * Use this when the error must stop the robot before a path starts.
     */
    public static void throwError(CrawlerError error, Object... args) {
        throw exception(error, args);
    }

    /**
     * Writes a non-fatal warning to telemetry in the CRWL spec format. Use this for
     * errors you want to surface without aborting (timeouts, tuning hints).
     */
    public static void postToTelemetry(Telemetry telemetry, CrawlerError error, Object... args) {
        if (telemetry == null) return;
        telemetry.addLine(format(error, args, callerFrame()));
    }

    /** Writes an already-built Crawler error exception (full rendered message) to telemetry. */
    public static void postToTelemetry(Telemetry telemetry, CrawlerErrorException e) {
        if (telemetry == null) return;
        telemetry.addLine(e.getMessage());
    }

    /**
     * Renders a catalog entry in the CRWL spec format.
     *
     * <p>Package-private for unit tests and {@link #format} reuse; production callers
     * should use {@link #exception(CrawlerError, Object...)}.</p>
     */
    static String format(CrawlerError error, Object[] args, StackTraceElement frame) {
        StringBuilder sb = new StringBuilder();
        sb.append(error.code()).append(": ").append(render(error.message(), args)).append('\n');
        sb.append("→ ").append(render(error.fix(), args)).append('\n');
        sb.append("→ Error found in ").append(frame.getFileName())
          .append(" at ").append(frame.getLineNumber()).append("! (IN RED)");
        return sb.toString();
    }

    private static String render(String template, Object[] args) {
        if (args == null || args.length == 0) return template;
        return String.format(template, args);
    }

    /** First stack frame outside the errors package — the check site that fired. */
    private static StackTraceElement callerFrame() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stack.length; i++) {
            String className = stack[i].getClassName();
            if (!className.startsWith(ERROR_PACKAGE)
                    && !className.startsWith("java.")
                    && !className.startsWith("jdk.")
                    && !className.startsWith("android.")
                    && !className.startsWith("dalvik.")) {
                return stack[i];
            }
        }
        return stack[Math.min(1, stack.length - 1)];
    }
}
