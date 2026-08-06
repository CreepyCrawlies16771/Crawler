package org.firstinspires.ftc.teamcode.Crawler.core.errors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Catalog invariants for {@link CrawlerError}: unique codes, valid rendering, category prefixes. */
public class CrawlerErrorTest {

    private static final Pattern PLACEHOLDER = Pattern.compile("%[-+0-9#.,]*[sdf]");

    @Test
    public void allCodes_areUnique() {
        Set<Integer> numbers = new HashSet<>();
        for (CrawlerError e : CrawlerError.values()) {
            assertTrue("duplicate number " + e.number(), numbers.add(e.number()));
        }
    }

    @Test
    public void codes_matchTheirCategoryPrefix() {
        for (CrawlerError e : CrawlerError.values()) {
            String expectedPrefix = Integer.toString(e.category().ordinal() + 1);
            assertTrue(e + " code must be " + expectedPrefix + "xx",
                    e.code().startsWith("CRWL-" + expectedPrefix));
        }
    }

    @Test
    public void allCodes_renderWithEnoughArgs() {
        for (CrawlerError e : CrawlerError.values()) {
            Object[] args = sampleArgs(e);
            String rendered = CrawlerErrors.format(
                    e, args, new StackTraceElement("com.example.Foo", "bar", "Foo.java", 42));
            assertTrue(e.code() + " must start with its code",
                    rendered.startsWith(e.code() + ":"));
            assertTrue(e.code() + " must include the fix arrow",
                    rendered.contains("\n→ "));
            assertTrue(e.code() + " must include the (IN RED) source line",
                    rendered.endsWith("(IN RED)"));
        }
    }

    @Test
    public void onlyWarningCodes_areNonThrowing() {
        List<CrawlerError> nonThrown = new ArrayList<>();
        for (CrawlerError e : CrawlerError.values()) {
            if (!e.isThrown()) nonThrown.add(e);
        }
        assertEquals(java.util.Arrays.asList(
                        CrawlerError.ODO_REVERSED_DIRECTION,
                        CrawlerError.ODO_DRIFT_DETECTED,
                        CrawlerError.RUNTIME_LEG_TIMEOUT),
                nonThrown);
    }

    /** Builds one value per placeholder in message+fix, matched to the conversion type. */
    private static Object[] sampleArgs(CrawlerError e) {
        List<Object> args = new ArrayList<>();
        collectArgs(e.message(), args);
        collectArgs(e.fix(), args);
        return args.toArray();
    }

    private static void collectArgs(String template, List<Object> args) {
        Matcher m = PLACEHOLDER.matcher(template);
        while (m.find()) {
            String spec = m.group();
            char conv = spec.charAt(spec.length() - 1);
            switch (conv) {
                case 'd': args.add(3); break;
                case 'f': args.add(60.5); break;
                default:  args.add("fl"); break;
            }
        }
    }
}
