package ing.prometheusreengineer.minecraft.util;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTest {
    private static final byte[] JAR = "pretend this is a patch jar".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path folder;

    private HttpServer server;
    private String base;
    private volatile String userAgent;

    @BeforeEach
    void start() throws IOException {
        System.setProperty("prometheus.log.console", "off");
        System.setProperty("prometheus.folder", folder.resolve("prometheus").toString());

        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/jar", exchange -> {
            userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
            exchange.sendResponseHeaders(200, JAR.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(JAR);
            }
        });
        server.createContext("/missing", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.createContext("/truncated", exchange -> {
            // promises 1000 bytes, sends 10 and hangs up
            exchange.sendResponseHeaders(200, 1000);
            exchange.getResponseBody().write(new byte[10]);
            exchange.close();
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stop() {
        server.stop(0);
        System.clearProperty("prometheus.log.console");
        System.clearProperty("prometheus.folder");
    }

    private Path cache() {
        return folder.resolve("cache");
    }

    private long files() throws IOException {
        if (!Files.exists(cache())) return 0;
        try (Stream<Path> walk = Files.walk(cache())) {
            return walk.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void getsWithUserAgent() throws IOException {
        assertEquals(new String(JAR, StandardCharsets.UTF_8), Http.getString(base + "/jar", Collections.<String, String>emptyMap()));
        assertTrue(userAgent.startsWith("Prometheus-Minecraft"), userAgent);
    }

    @Test
    void errorStatusThrows() {
        IOException e = assertThrows(IOException.class, () -> Http.get(base + "/missing"));
        assertTrue(e.getMessage().contains("404"), e.getMessage());
    }

    @Test
    void downloadsAndVerifies() throws IOException {
        Path target = cache().resolve("patch.jar");
        Http.download(base + "/jar", target, "sha256:" + Hashing.sha256(JAR));
        assertEquals(Hashing.sha256(JAR), Hashing.sha256(target));
    }

    @Test
    void mismatchKeepsTheOldFile() throws IOException {
        Path target = cache().resolve("patch.jar");
        AtomicFiles.writeString(target, "previous version");

        IOException e = assertThrows(IOException.class, () -> Http.download(base + "/jar", target, Hashing.sha256(new byte[0])));
        assertTrue(e.getMessage().contains("sha256 mismatch"), e.getMessage());
        assertEquals("previous version", new String(Files.readAllBytes(target), StandardCharsets.UTF_8));
        assertEquals(1, files(), "no temp files left");
    }

    @Test
    void interruptedDownloadLeavesNothing() throws IOException {
        Path target = cache().resolve("patch.jar");
        assertThrows(IOException.class, () -> Http.download(base + "/truncated", target, null));
        assertFalse(Files.exists(target));
        assertEquals(0, files(), "no temp files left");
    }
}
