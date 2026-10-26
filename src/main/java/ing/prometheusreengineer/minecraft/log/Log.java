package ing.prometheusreengineer.minecraft.log;

import ing.prometheusreengineer.minecraft.util.Settings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * The logger for every Prometheus project, which works the same on every client and loader.
 * <pre>{@code
 * private static final Log log = Log.get("essential", "ECM");
 * log.info("Merged %d cosmetics (%d new)", total, added);
 * log.error("Failed to save cosmetics list", e);
 * }</pre>
 * Every line goes to {@code <prometheus folder>/logs/<project>.log} (see {@link LogFile}) and is echoed to a
 * {@link Console}. Hosts disagree on logging (Lunar swallows JUL, Forge 1.8.9 has a Log4j beta, the loader runs before
 * most of it is set up), so the level and the format are decided here, never by the host.
 * <br>
 * Messages are {@link String#format} patterns, formatted before any backend sees them. Logging never throws.
 * <br>
 * Settings ({@code -Dprometheus.<key>} or {@code PROMETHEUS_<KEY>}):
 * <ul>
 *     <li>{@code debug} / {@code <project>.debug}: {@code true} to log {@link Level#DEBUG}</li>
 *     <li>{@code log.console} / {@code <project>.log.console}: {@code log4j}, {@code stdout}, {@code stderr} or {@code off}</li>
 * </ul>
 */
public final class Log {
    /** Where the lib's own lines go while no project has claimed this copy with {@link #setOwner}. */
    static final String UNOWNED = "lib";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern UNSAFE_NAME = Pattern.compile("[^A-Za-z0-9._-]");

    private static final ConcurrentMap<String, LogFile> files = new ConcurrentHashMap<>();
    private static volatile @Nullable Console defaultConsole;
    private static volatile @Nullable String owner;
    private static volatile @Nullable Boolean log4jPresent;

    /** Null for the lib's own loggers, which follow {@link #owner}. */
    private final @Nullable String fixedProject;
    /** What follows {@code Prometheus/<project>}, ex: {@code /ECM}. */
    private final @NotNull String suffix;

    private Log(@Nullable String fixedProject, @NotNull String suffix) {
        this.fixedProject = fixedProject;
        this.suffix = suffix;
    }

    /**
     * @param project the file name and settings key, ex: {@code lunar}, {@code essential}, {@code dawn}, {@code loader}
     * @param path    optional sub names, ex: {@code Log.get("essential", "ECM")} logs as {@code Prometheus/essential/ECM}
     */
    @Contract("_, _ -> new")
    public static @NotNull Log get(@NotNull String project, @NotNull String @NotNull ... path) {
        return new Log(safe(project), join(path));
    }

    /**
     * For code inside this lib. Every project bundles its own copy of the lib, so its lines belong to that project:
     * {@code Log.lib("Env")} logs as {@code Prometheus/dawn/lib/Env} into {@code dawn.log} once Dawn called
     * {@code setOwner("dawn")}, and as {@code Prometheus/lib/Env} into {@code lib.log} before that.
     */
    @Contract("_ -> new")
    public static @NotNull Log lib(@NotNull String @NotNull ... path) {
        return new Log(null, join(path));
    }

    /**
     * Claims this copy of the lib for a project, called first thing by its entry point. The lib's own loggers
     * ({@link #lib}) then log as part of it. Read on every line, so loggers created before this pick it up too.
     */
    public static void setOwner(@Nullable String project) {
        owner = project == null ? null : safe(project);
    }

    /**
     * The console used when the user hasn't picked one, set by a project's entry point.
     * Without it, {@link Console#LOG4J} (which falls back to {@link Console#STDOUT}).
     * <br>
     * Read on every line, so loggers created before the entry point ran pick it up too.
     */
    public static void setDefaultConsole(@Nullable Console console) {
        defaultConsole = console;
    }

    @Contract(pure = true)
    public boolean isDebugEnabled() {
        return isDebug(project());
    }

    public void debug(@NotNull String format, @Nullable Object @NotNull ... args) {
        log(Level.DEBUG, null, format, args);
    }

    public void info(@NotNull String format, @Nullable Object @NotNull ... args) {
        log(Level.INFO, null, format, args);
    }

    public void warn(@NotNull String format, @Nullable Object @NotNull ... args) {
        log(Level.WARN, null, format, args);
    }

    /**
     * Logs {@code t}'s stack trace. {@code t} is also the format argument, since Java picks this overload for
     * {@code warn("failed: %s", e)}. Pass {@code e.toString()} instead for one line without the stack trace.
     */
    public void warn(@NotNull String message, @Nullable Throwable t) {
        log(Level.WARN, t, message, t);
    }

    public void error(@NotNull String format, @Nullable Object @NotNull ... args) {
        log(Level.ERROR, null, format, args);
    }

    /** See {@link #warn(String, Throwable)}. */
    public void error(@NotNull String message, @Nullable Throwable t) {
        log(Level.ERROR, t, message, t);
    }

    /**
     * Without args the message is used as is, so a literal {@code %} needs no escaping. A trailing {@link Throwable}
     * arg is logged as the exception when {@code t} is null, so {@code info("failed %s", id, e)} keeps its stack trace.
     */
    public void log(@NotNull Level level, @Nullable Throwable t, @NotNull String format, @Nullable Object @NotNull ... args) {
        try {
            String project = project();
            if (level == Level.DEBUG && !isDebug(project)) return;
            //noinspection ConstantValue - an explicit (Object[]) null
            if (args == null) args = new Object[0];
            if (t == null && args.length > 0 && args[args.length - 1] instanceof Throwable) {
                t = (Throwable) args[args.length - 1];
            }
            String message = format(format, args);
            // the lib's own lines are marked inside their owner's log, unowned they're already Prometheus/lib
            boolean marked = fixedProject == null && !UNOWNED.equals(project);
            String name = "Prometheus/" + project + (marked ? "/lib" : "") + suffix;
            file(project).write(level, name, message, t);
            console(project, level, name, message, t);
        } catch (Throwable ignored) {
            // a logger must never be what crashes the game
        }
    }

    static boolean isDebug(@NotNull String project) {
        return Settings.getBoolean(project + ".debug", Settings.getBoolean("debug", false));
    }

    private @NotNull String project() {
        if (fixedProject != null) return fixedProject;
        String current = owner;
        return current != null ? current : UNOWNED;
    }

    private static @NotNull LogFile file(@NotNull String project) {
        LogFile file = files.get(project);
        if (file == null) {
            file = new LogFile(project, null);
            LogFile raced = files.putIfAbsent(project, file);
            if (raced != null) file = raced;
        }
        return file;
    }

    private static @NotNull String safe(@NotNull String project) {
        return UNSAFE_NAME.matcher(project).replaceAll("_");
    }

    private static @NotNull String join(@NotNull String @NotNull [] path) {
        StringBuilder suffix = new StringBuilder();
        for (String part : path) suffix.append('/').append(part);
        return suffix.toString();
    }

    @SuppressWarnings("fallthrough")
    private static void console(@NotNull String project, @NotNull Level level, @NotNull String name,
                                @NotNull String message, @Nullable Throwable t) {
        switch (console(project)) {
            case LOG4J:
                if (isLog4jPresent()) {
                    try {
                        Log4jConsole.write(level, name, message, t);
                        return;
                    } catch (Throwable e) {
                        // present but broken (ex: a host that strips it down), stop trying
                        log4jPresent = false;
                    }
                }
                // fall through
            case STDOUT:
                print(System.out, level, name, message, t);
                return;
            case STDERR:
                print(System.err, level, name, message, t);
                return;
            case OFF:
        }
    }

    private static @NotNull Console console(@NotNull String project) {
        Console console = Console.parse(Settings.get(project + ".log.console"));
        if (console == null) console = Console.parse(Settings.get("log.console"));
        if (console == null) console = defaultConsole;
        return console == null ? Console.LOG4J : console;
    }

    private static void print(@NotNull PrintStream stream, @NotNull Level level, @NotNull String name,
                              @NotNull String message, @Nullable Throwable t) {
        // read at call time, so it goes through Forge's redirect even if Forge installs it after us
        synchronized (stream) {
            stream.println("[" + LocalTime.now().format(TIME) + "] [" + name + "] (" + level + ") " + message);
            if (t != null) t.printStackTrace(stream);
        }
    }

    private static boolean isLog4jPresent() {
        Boolean present = log4jPresent;
        if (present == null) {
            present = LogFile.Environment.isPresent("org.apache.logging.log4j.LogManager");
            log4jPresent = present;
        }
        return present;
    }

    @Contract(pure = true)
    private static @NotNull String format(@NotNull String format, @Nullable Object @NotNull [] args) {
        if (args.length == 0) return format;
        try {
            return String.format(Locale.ROOT, format, args);
        } catch (IllegalFormatException e) {
            // a broken pattern still shows what was meant
            return format + " " + Arrays.toString(args);
        }
    }
}
