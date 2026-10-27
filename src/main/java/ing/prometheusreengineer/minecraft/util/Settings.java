package ing.prometheusreengineer.minecraft.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * User overrides, read from {@code -Dprometheus.<key>} first and {@code PROMETHEUS_<KEY>} second.
 * <br>
 * Dots in the key become underscores in the env variable, so {@code dawn.port} is read from
 * {@code -Dprometheus.dawn.port} or {@code PROMETHEUS_DAWN_PORT}. Blank values count as unset.
 */
public final class Settings {
    private Settings() {}

    @Contract(pure = true)
    public static @Nullable String get(@NotNull String key) {
        String value = System.getProperty("prometheus." + key);
        if (isBlank(value)) {
            value = System.getenv("PROMETHEUS_" + key.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        return isBlank(value) ? null : value.trim();
    }

    @Contract(value = "_, !null -> !null", pure = true)
    public static String get(@NotNull String key, @Nullable String fallback) {
        String value = get(key);
        return value == null ? fallback : value;
    }

    /**
     * Only {@code true} and {@code false} (any case) are accepted, anything else is the fallback.
     */
    @Contract(pure = true)
    public static boolean getBoolean(@NotNull String key, boolean fallback) {
        String value = get(key);
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        return fallback;
    }

    @Contract(pure = true)
    public static int getInt(@NotNull String key, int fallback) {
        String value = get(key);
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
