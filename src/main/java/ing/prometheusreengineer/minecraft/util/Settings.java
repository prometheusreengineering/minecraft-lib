package ing.prometheusreengineer.minecraft.util;

import ing.prometheusreengineer.minecraft.util.java.StreamUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

import static ing.prometheusreengineer.minecraft.util.java.LambdaUtil.runCheckedOr;

/**
 * User overrides read from {@code -Dprometheus.<key>} first and {@code PROMETHEUS_<KEY>} second.
 * Environment variable is all caps and replaces dots with underscores.
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
        return Optional.ofNullable(get(key)).orElse(fallback);
    }

    @Contract(pure = true)
    public static boolean getBoolean(@NotNull String key, boolean fallback) {
        String value = get(key);
        if (value == null) return fallback;
        if (StreamUtil.from("1", "true",  "yes"   )
                .anyMatch(value::equalsIgnoreCase)) return true;
        if (StreamUtil.from("0", "false", "no", "")
                .anyMatch(value::equalsIgnoreCase)) return false;
        return fallback;
    }

    @Contract(pure = true)
    public static int getInt(@NotNull String key, int fallback) {
        //noinspection DataFlowIssue (null returns the fallback)
        return runCheckedOr(() -> Integer.parseInt(get(key)), fallback);
    }

    @Contract(value = "null -> true", pure = true)
    private static boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
