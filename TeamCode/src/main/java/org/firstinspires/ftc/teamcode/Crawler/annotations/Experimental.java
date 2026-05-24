package org.firstinspires.ftc.teamcode.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks APIs or features as experimental.
 * <p>
 * Experimental features may be unstable, changed, or removed in future releases.
 * This annotation is informational only.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.PACKAGE
})
public @interface Experimental {
    /** Optional short note about the experimental status. */
    String value() default "";

    /** Optional version or date when the feature became experimental. */
    String since() default "";

    /** Whether the feature may be removed in a future release. Default true. */
    boolean mayBeRemoved() default true;
}
