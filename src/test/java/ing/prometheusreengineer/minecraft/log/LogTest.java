package ing.prometheusreengineer.minecraft.log;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Log.setOwner(null);
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
        assertEquals(null, Console.parse("nope"));
    }
}
