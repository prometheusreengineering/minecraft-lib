package ing.prometheusreengineer.minecraft.log;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/* Log output destination */
public enum Console {
    // Pre-1.7 doesn't use log4j
    LOG4J,
    STDOUT,
    STDERR,
    OFF;

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
