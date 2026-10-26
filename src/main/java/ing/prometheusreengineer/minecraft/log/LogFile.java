package ing.prometheusreengineer.minecraft.log;

import ing.prometheusreengineer.minecraft.util.OS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code <prometheus folder>/logs/<project>.log}, one per project.
 * <br>
 * Opened on the first line written, so nothing touches the disk until something is logged. The first copy of the lib
 * to open it in this JVM moves the previous run to {@code <project>.1.log}; a second copy of the same project (its jar
 * loaded twice) appends instead. Every line is flushed, so the file survives a crash.
 */
final class LogFile {
    /** Per run (counted in chars, close enough), so a logging loop can't fill the disk. */
    static final long MAX_BYTES = 8L * 1024 * 1024;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String NEWLINE = System.lineSeparator();

    private final @NotNull String project;
    /** Null means {@code OS.getConfigFolder()/logs}, resolved when the file is opened. */
    private final @Nullable Path folder;

    private @Nullable Writer writer;
    private @Nullable Path path;
    private boolean opened;
    private boolean full;
    private long written;

    LogFile(@NotNull String project, @Nullable Path folder) {
        this.project = project;
        this.folder = folder;
    }

    /** Never throws. If the file can't be opened, file logging is turned off and the console keeps working. */
    synchronized void write(@NotNull Level level, @NotNull String name, @NotNull String message, @Nullable Throwable t) {
        if (!opened) open();
        if (writer == null || full) return;

        StringBuilder line = new StringBuilder(64 + message.length())
                .append(LocalDateTime.now().format(TIME))
                .append(" [").append(Thread.currentThread().getName()).append("] ")
                .append(pad(level))
                .append(' ').append(name).append(": ")
                .append(message).append(NEWLINE);
        if (t != null) line.append(stackTrace(t));

        if (written + line.length() > MAX_BYTES) {
            full = true;
            append("---- " + MAX_BYTES / 1024 / 1024 + " MB reached, the rest of this run only goes to the console ----" + NEWLINE);
            return;
        }
        append(line.toString());
    }

    private void open() {
        opened = true;
        Path target = null;
        try {
            Path dir = folder != null ? folder : OS.getConfigFolder().resolve("logs");
            target = dir.resolve(project + ".log");
            Files.createDirectories(dir);

            // system properties are shared by every class loader, so this is once per JVM even with several copies
            boolean first = System.getProperties().putIfAbsent("prometheus.log." + project + ".rotated", "true") == null;
            if (first && Files.exists(target)) {
                try {
                    Files.move(target, dir.resolve(project + ".1.log"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    // locked or read only, appending to the old run beats losing this one
                }
            }

            writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            path = target;
            append(first ? header() : "---- another copy of the lib attached: " + jar() + " ----" + NEWLINE);
        } catch (Throwable e) {
            writer = null;
            // the file is the one place this could go, so say it once on stderr
            System.err.println("[Prometheus/" + project + "] (WARN) could not open log file " + target + ": " + e);
        }
    }

    private void append(@NotNull String text) {
        Writer w = writer;
        if (w == null) return;
        try {
            w.write(text);
            w.flush();
            written += text.length();
        } catch (Throwable e) {
            writer = null;
            System.err.println("[Prometheus/" + project + "] (WARN) stopped writing log file " + path + ": " + e);
        }
    }

    private @NotNull String header() {
        List<String> environment = Environment.detect();
        return "==== Prometheus/" + project + " log, started " + LocalDateTime.now().format(TIME) + " ====" + NEWLINE
                + "jar:         " + jar() + NEWLINE
                + "java:        " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")" + NEWLINE
                + "os:          " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")" + NEWLINE
                + "folder:      " + OS.getConfigFolder() + NEWLINE
                + "environment: " + (environment.isEmpty() ? "unknown" : String.join(", ", environment)) + NEWLINE
                + "debug:       " + (Log.isDebug(project) ? "on" : "off") + NEWLINE;
    }

    /** The jar this copy of the lib was loaded from, which names the patch and its version. */
    private static @NotNull String jar() {
        try {
            CodeSource source = LogFile.class.getProtectionDomain().getCodeSource();
            if (source != null && source.getLocation() != null) return source.getLocation().toString();
        } catch (Throwable ignored) {}
        return "unknown";
    }

    private static @NotNull String pad(@NotNull Level level) {
        String name = level.name();
        return name.length() == 4 ? name + ' ' : name;
    }

    private static @NotNull String stackTrace(@NotNull Throwable t) {
        StringWriter out = new StringWriter();
        try (PrintWriter writer = new PrintWriter(out)) {
            t.printStackTrace(writer);
        }
        return out.toString();
    }

    /** What the game is running on, by which loader classes exist. Nothing is initialized. */
    static final class Environment {
        private Environment() {}

        private static final String[][] KNOWN = {
                {"net.fabricmc.loader.api.FabricLoader", "Fabric"},
                {"net.minecraft.launchwrapper.Launch", "LaunchWrapper (Forge 1.8-1.12)"},
                {"cpw.mods.modlauncher.Launcher", "ModLauncher (Forge 1.13+)"},
                {"net.neoforged.fml.loading.FMLLoader", "NeoForge"},
                {"com.moonsworth.lunar.genesis.Genesis", "Lunar Client"},
        };

        static @NotNull List<String> detect() {
            List<String> found = new ArrayList<>();
            for (String[] known : KNOWN) {
                if (isPresent(known[0])) found.add(known[1]);
            }
            return found;
        }

        static boolean isPresent(@NotNull String className) {
            ClassLoader[] loaders = {Thread.currentThread().getContextClassLoader(), LogFile.class.getClassLoader(), ClassLoader.getSystemClassLoader()};
            for (ClassLoader loader : loaders) {
                if (loader == null) continue;
                try {
                    Class.forName(className, false, loader);
                    return true;
                } catch (Throwable ignored) {}
            }
            return false;
        }
    }
}
