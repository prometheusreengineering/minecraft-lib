package ing.prometheusreengineer.minecraft.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The only class that touches Log4j. {@link Log} checks Log4j is present before calling into it, so a missing Log4j
 * never surfaces as a {@link NoClassDefFoundError}.
 * <br>
 * Compiled against log4j-api 2.0-beta9 (Forge 1.8.9's), and only uses methods that every 2.x has kept.
 */
final class Log4jConsole {
    private Log4jConsole() {}

    private static final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();

    static void write(@NotNull Level level, @NotNull String name, @NotNull String message, @Nullable Throwable t) {
        Logger logger = loggers.get(name);
        if (logger == null) {
            logger = LogManager.getLogger(name);
            Logger raced = loggers.putIfAbsent(name, logger);
            if (raced != null) logger = raced;
        }

        switch (level) {
            case DEBUG:
                logger.debug(message, t);
                break;
            case INFO:
                logger.info(message, t);
                break;
            case WARN:
                logger.warn(message, t);
                break;
            case ERROR:
                logger.error(message, t);
                break;
        }
    }
}
