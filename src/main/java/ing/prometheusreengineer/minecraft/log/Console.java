package ing.prometheusreengineer.minecraft.log;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Where a {@link Log} echoes its lines, on top of the log file it always writes.
 */
public enum Console {
    /**
     * The game's Log4j if it is on the classpath (every Minecraft since 1.7), else {@link #STDOUT}.
     */
    LOG4J,
    STDOUT,
    STDERR,
    OFF;

    /**
     * Lenient: any case, surrounding whitespace ignored. Null if it is not a console.
     */
    @Contract(value = "null -> null", pure = true)
    static @Nullable Console parse(@Nullable String value) {
        if (value == null) return null;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
