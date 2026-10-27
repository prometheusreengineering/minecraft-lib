package ing.prometheusreengineer.minecraft.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogTest {
    @TempDir
    Path folder;

    /** Unique per test: files and the once-per-JVM rotation are keyed by project. */
    private String project;

    @BeforeEach
    void setUp() {
        project = "test-" + UUID.randomUUID().toString().substring(0, 8);
        System.setProperty("prometheus.folder", folder.toString());
        System.setProperty("prometheus.log.console", "off");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("prometheus.folder");
        System.clearProperty("prometheus.log.console");
        System.clearProperty("prometheus." + project + ".debug");
        System.clearProperty("prometheus." + project + ".log.console");
        Log.setOwner(null);
        Log.setDefaultConsole(null);
    }

    /**
     * What {@code action} prints on stdout or stderr.
     */
    private static String capture(boolean stderr, Runnable action) throws IOException {
        PrintStream original = stderr ? System.err : System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(bytes, true, "UTF-8");
        if (stderr) System.setErr(stream);
        else System.setOut(stream);
        try {
            action.run();
        } finally {
            if (stderr) System.setErr(original);
            else System.setOut(original);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    private String read() throws IOException {
        return read(project);
    }

    private String read(String file) throws IOException {
        return new String(Files.readAllBytes(folder.resolve("logs").resolve(file + ".log")), StandardCharsets.UTF_8);
    }

    @Test
    void libLoggerFollowsOwner() throws IOException {
        // created before the owner is known, like a static final field
        Log log = Log.lib("Env");

        log.info("before owner");
        assertTrue(read(Log.UNOWNED).contains("INFO  Prometheus/lib/Env: before owner"), read(Log.UNOWNED));

        Log.setOwner(project);
        log.info("after owner");
        assertTrue(read().contains("INFO  Prometheus/" + project + "/lib/Env: after owner"), read());
        assertFalse(read(Log.UNOWNED).contains("after owner"));
    }

    @Test
    void libLoggerUsesOwnersDebug() throws IOException {
        Log.setOwner(project);
        System.setProperty("prometheus." + project + ".debug", "true");
        Log.lib("Env").debug("owner debug");
        assertTrue(read().contains("DEBUG Prometheus/" + project + "/lib/Env: owner debug"));
    }

    @Test
    void writesHeaderAndFormattedLines() throws IOException {
        Log.get(project, "ECM").info("Merged %d cosmetics (%d new)", 737, 3);

        String log = read();
        assertTrue(log.startsWith("==== Prometheus/" + project + " log"), log);
        assertTrue(log.contains("debug:       off"), log);
        assertTrue(log.contains("INFO  Prometheus/" + project + "/ECM: Merged 737 cosmetics (3 new)"), log);
        assertTrue(log.contains("[" + Thread.currentThread().getName() + "]"), log);
    }

    @Test
    void errorWithArgs() throws IOException {
        Log.get(project).error("failed %d of %d", 2, 5);
        assertTrue(read().contains("ERROR Prometheus/" + project + ": failed 2 of 5"));
    }

    @Test
    void literalPercentWithoutArgs() throws IOException {
        Log.get(project).info("100% done");
        assertTrue(read().contains(": 100% done"));
    }

    @Test
    void brokenPatternDoesNotThrow() throws IOException {
        Log.get(project).warn("bad %d pattern", "not a number");
        assertTrue(read().contains("bad %d pattern [not a number]"));
    }

    @Test
    void stackTraces() throws IOException {
        Log log = Log.get(project);
        log.error("explicit", new IllegalStateException("first"));
        log.info("trailing %s", "arg", new IllegalArgumentException("second"));

        String text = read();
        assertTrue(text.contains("java.lang.IllegalStateException: first"), text);
        assertTrue(text.contains("trailing arg"), text);
        assertTrue(text.contains("java.lang.IllegalArgumentException: second"), text);
    }

    @Test
    void throwableOverloadStillFormats() throws IOException {
        // Java picks warn(String, Throwable) here, not the varargs one
        Log.get(project).warn("could not fetch: %s", new IllegalStateException("offline"));
        Log.get(project).error("plain message", new IllegalStateException("boom"));

        String text = read();
        assertTrue(text.contains(": could not fetch: java.lang.IllegalStateException: offline"), text);
        assertFalse(text.contains("%s"), text);
        assertTrue(text.contains(": plain message" + System.lineSeparator() + "java.lang.IllegalStateException: boom"), text);
    }

    @Test
    void debugIsOffByDefault() throws IOException {
        Log log = Log.get(project);
        log.debug("hidden");
        System.setProperty("prometheus." + project + ".debug", "true");
        log.debug("shown");

        String text = read();
        assertFalse(text.contains("hidden"));
        assertTrue(text.contains("DEBUG Prometheus/" + project + ": shown"));
    }

    @Test
    void sameProjectSharesOneFile() throws IOException {
        Log.get(project, "A").info("from a");
        Log.get(project, "B").info("from b");

        String text = read();
        assertTrue(text.contains("/A: from a") && text.contains("/B: from b"), text);
        assertEquals(text.indexOf("===="), text.lastIndexOf("==== Prometheus"), "one header");
    }

    @Test
    void rotatesOncePerJvm() throws IOException {
        Path logs = folder.resolve("logs");
        Files.createDirectories(logs);
        Files.write(logs.resolve(project + ".log"), "previous run".getBytes(StandardCharsets.UTF_8));

        new LogFile(project, logs).write(Level.INFO, "first", "first copy", null);
        // a second copy of the lib in the same JVM appends instead of rotating again
        new LogFile(project, logs).write(Level.INFO, "second", "second copy", null);

        assertEquals("previous run", new String(Files.readAllBytes(logs.resolve(project + ".1.log")), StandardCharsets.UTF_8));
        String text = read();
        assertTrue(text.contains("first copy") && text.contains("second copy"), text);
        assertTrue(text.contains("---- another copy of the lib attached"), text);
    }

    @Test
    void unusableFolderNeverThrows() throws IOException {
        Path notAFolder = folder.resolve("file");
        Files.write(notAFolder, new byte[0]);

        LogFile file = new LogFile(project, notAFolder.resolve("logs"));
        assertDoesNotThrow(() -> file.write(Level.ERROR, "x", "lost", null));
        assertDoesNotThrow(() -> file.write(Level.ERROR, "x", "still lost", null));
    }

    @Test
    void capsTheFile() throws IOException {
        Path logs = folder.resolve("logs");
        LogFile file = new LogFile(project, logs);
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 64 * 1024; i++) big.append('x');
        for (long i = 0; i <= LogFile.MAX_BYTES / big.length() + 1; i++) file.write(Level.INFO, "x", big.toString(), null);

        String text = read();
        assertTrue(text.length() <= LogFile.MAX_BYTES + 1024, "length " + text.length());
        assertTrue(text.endsWith("only goes to the console ----" + System.lineSeparator()));
    }

    @Test
    void consoleNames() {
        assertEquals(Console.STDERR, Console.parse(" stderr "));
        assertNull(Console.parse("nope"));
        assertNull(Console.parse(null));
    }

    @Test
    void projectConsoleStdout() throws IOException {
        System.setProperty("prometheus." + project + ".log.console", "stdout");
        String out = capture(false, () -> Log.get(project, "ECM").warn("to %s", "stdout", new IllegalStateException("traced")));
        assertTrue(out.contains("[Prometheus/" + project + "/ECM] (WARN) to stdout"), out);
        assertTrue(out.contains("java.lang.IllegalStateException: traced"), out);
    }

    @Test
    void globalConsoleStderr() throws IOException {
        System.setProperty("prometheus.log.console", "stderr");
        String err = capture(true, () -> Log.get(project).info("to stderr"));
        assertTrue(err.contains("(INFO) to stderr"), err);
    }

    @Test
    void defaultConsoleWhenNothingIsSet() throws IOException {
        System.clearProperty("prometheus.log.console");
        Log.setDefaultConsole(Console.STDERR);
        String err = capture(true, () -> Log.get(project).info("default console"));
        assertTrue(err.contains("(INFO) default console"), err);
    }

    @Test
    void log4jByDefault() throws IOException {
        System.clearProperty("prometheus.log.console");
        // the test classpath has log4j-api, so lines go to Log4j and not stdout
        String out = capture(false, () -> Log.get(project).info("through log4j"));
        assertFalse(out.contains("through log4j"), out);
        assertTrue(read().contains("through log4j"), "the file still gets it");
    }

    @Test
    void log4jEveryLevel() {
        for (Level level : Level.values()) {
            assertDoesNotThrow(() -> Log4jConsole.write(level, "Prometheus/test", "at " + level, null));
        }
    }

    @Test
    void brokenLog4jFallsBackToStdout() throws Exception {
        System.clearProperty("prometheus.log.console");
        // a second copy of the lib that can't see Log4j: it looks present through the context class loader, but
        // Log4jConsole can't link against it, like a host that ships a broken Log4j
        URL classes = Log.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader isolated = new URLClassLoader(new URL[]{classes}, ClassLoader.getSystemClassLoader().getParent())) {
            Class<?> copy = Class.forName(Log.class.getName(), true, isolated);
            Object log = copy.getMethod("get", String.class, String[].class).invoke(null, project, new String[0]);
            Method info = copy.getMethod("info", String.class, Object[].class);

            String out = capture(false, () -> {
                try {
                    info.invoke(log, "first", new Object[0]);
                    info.invoke(log, "second", new Object[0]);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            });
            assertTrue(out.contains("(INFO) first") && out.contains("(INFO) second"), out);
        }
    }

    @Test
    void consoleOff() throws IOException {
        String out = capture(false, () -> Log.get(project).info("silent"));
        assertFalse(out.contains("silent"), out);
    }

    @Test
    void nullArgsArray() throws IOException {
        Log.get(project).info("kept %s", (Object[]) null);
        assertTrue(read().contains(": kept %s"));
    }

    @Test
    void neverThrows() {
        Throwable evil = new IllegalStateException("evil") {
            @Override
            public void printStackTrace(PrintWriter writer) {
                throw new UnsupportedOperationException("can't print me");
            }
        };
        assertDoesNotThrow(() -> Log.get(project).error("boom", evil));
    }

    @Test
    void isDebugEnabled() {
        Log log = Log.get(project);
        assertFalse(log.isDebugEnabled());
        System.setProperty("prometheus." + project + ".debug", "true");
        assertTrue(log.isDebugEnabled());
    }

    @Test
    void headerShowsDebugAndEnvironment() throws IOException {
        System.setProperty("prometheus." + project + ".debug", "true");
        Log.get(project).info("first line");
        assertTrue(read().contains("debug:       on"), read());

        String header = new LogFile(project, folder).header(Arrays.asList("Fabric", "Lunar Client"));
        assertTrue(header.contains("environment: Fabric, Lunar Client"), header);
    }

    @Test
    void rotationBlockedAppends() throws IOException {
        Path logs = folder.resolve("logs");
        Files.createDirectories(logs.resolve(project + ".1.log").resolve("in the way"));
        Files.write(logs.resolve(project + ".log"), "previous run".getBytes(StandardCharsets.UTF_8));

        new LogFile(project, logs).write(Level.INFO, "x", "this run", null);

        String text = read();
        assertTrue(text.startsWith("previous run") && text.contains("this run"), text);
    }

    @Test
    void failedWriteStopsTheFile() throws IOException {
        LogFile file = new LogFile(project, folder.resolve("logs"));
        file.write(Level.INFO, "x", "before", null);
        file.writer = new Writer() {
            @Override
            public void write(char @NotNull [] buffer, int offset, int length) throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };

        String err = capture(true, () -> file.write(Level.INFO, "x", "lost", null));
        assertTrue(err.contains("stopped writing log file") && err.contains("disk full"), err);
        assertNull(file.writer);
        assertDoesNotThrow(() -> file.write(Level.INFO, "x", "after", null));
    }

    /**
     * Loaded a second time, with a code source that has no location.
     */
    static final class Probe {}

    @Test
    void jarLocation() throws Exception {
        assertNotEquals("unknown", LogFile.jar(LogFile.class));
        assertEquals("unknown", LogFile.jar(String.class), "the JDK's classes have no code source");
        assertEquals("unknown", LogFile.jar(null), "a failure is unknown, not an exception");

        byte[] bytes;
        try (InputStream in = Probe.class.getResourceAsStream("LogTest$Probe.class")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = in.read(buffer)) != -1; ) out.write(buffer, 0, n);
            bytes = out.toByteArray();
        }
        ProtectionDomain noLocation = new ProtectionDomain(new CodeSource(null, (Certificate[]) null), null);
        Class<?> probe = new ClassLoader(null) {
            Class<?> define() {
                return defineClass(Probe.class.getName(), bytes, 0, bytes.length, noLocation);
            }
        }.define();
        assertEquals("unknown", LogFile.jar(probe));
    }

    @Test
    void detectsEnvironment() {
        assertEquals(Collections.singletonList("Java"), LogFile.Environment.detect(new String[][]{
                {"java.lang.String", "Java"},
                {"com.example.NotInstalled", "Missing"},
        }));
    }

    @Test
    void presentWithoutContextClassLoader() {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        thread.setContextClassLoader(null);
        try {
            assertTrue(LogFile.Environment.isPresent("java.lang.String"));
            assertFalse(LogFile.Environment.isPresent("com.example.NotInstalled"));
        } finally {
            thread.setContextClassLoader(original);
        }
    }
}
