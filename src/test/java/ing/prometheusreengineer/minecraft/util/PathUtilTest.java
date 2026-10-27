package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathUtilTest {
    @TempDir
    Path folder;

    @BeforeEach
    void setUp() {
        System.setProperty("prometheus.folder", folder.resolve("prometheus").toString());
        System.setProperty("prometheus.log.console", "off");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("prometheus.folder");
        System.clearProperty("prometheus.log.console");
    }

    private static void write(Path file, String text) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    /**
     * src/a.txt and src/sub/b.txt
     */
    private Path tree() throws IOException {
        Path src = folder.resolve("src");
        write(src.resolve("a.txt"), "a");
        write(src.resolve("sub").resolve("b.txt"), "b");
        return src;
    }

    @Test
    void homeFolder() {
        assertEquals(Paths.get(System.getProperty("user.home"), "a", "b"), PathUtil.fromHomeFolder("a", "b"));
    }

    @Test
    void envOrFallback() {
        Path fallback = Paths.get("fallback");
        assertEquals(fallback, PathUtil.fromEnvOr(null, fallback));

        String name = "PROMETHEUS_TEST_" + UUID.randomUUID().toString().replace('-', '_').toUpperCase();
        assertEquals(fallback, PathUtil.fromEnvOr(name, fallback));
        Env.setenv(name, "");
        assertEquals(fallback, PathUtil.fromEnvOr(name, fallback));
        Env.setenv(name, "from env");
        assertEquals(Paths.get("from env"), PathUtil.fromEnvOr(name, fallback));
    }

    @Test
    void copiesATree() throws IOException {
        Path dst = folder.resolve("dst");
        write(dst.resolve("a.txt"), "old");

        PathUtil.copyRecursively(tree(), dst);

        assertEquals("a", read(dst.resolve("a.txt")), "overwritten");
        assertEquals("b", read(dst.resolve("sub").resolve("b.txt")));
    }

    @Test
    void copiesAFileIntoNewFolders() throws IOException {
        Path dst = folder.resolve("x").resolve("y").resolve("a.txt");
        PathUtil.copyRecursively(tree().resolve("a.txt"), dst);
        assertEquals("a", read(dst));
    }

    @Test
    void copiesOnlyWhatIsMissing() throws IOException {
        Path dst = folder.resolve("dst");
        write(dst.resolve("a.txt"), "kept");

        PathUtil.copyMissingRecursively(tree(), dst);

        assertEquals("kept", read(dst.resolve("a.txt")));
        assertEquals("b", read(dst.resolve("sub").resolve("b.txt")));
    }

    @Test
    void copiesAMissingFileIntoNewFolders() throws IOException {
        Path dst = folder.resolve("x").resolve("a.txt");
        PathUtil.copyMissingRecursively(tree().resolve("a.txt"), dst);
        assertEquals("a", read(dst));
    }

    @Test
    void deletesATree() throws IOException {
        Path src = tree();
        PathUtil.deleteRecursively(src);
        assertFalse(Files.exists(src));
        assertDoesNotThrow(() -> PathUtil.deleteRecursively(src), "already gone");
    }

    @Test
    void emptyOrNull() throws IOException {
        assertTrue(PathUtil.isEmptyOrNull(null));
        Path empty = Files.createDirectories(folder.resolve("empty"));
        assertTrue(PathUtil.isEmptyOrNull(empty.toFile()));
        Path src = tree();
        assertFalse(PathUtil.isEmptyOrNull(src.toFile()));
        assertTrue(PathUtil.isEmptyOrNull(src.resolve("a.txt").toFile()), "a file has no children");
    }

    @Test
    void childrenOfAFileAreEmpty() throws IOException {
        assertEquals(0, PathUtil.children(tree().resolve("a.txt").toFile()).length);
    }

    @Test
    void aPathWithoutParentNeedsNoFolder() {
        assertDoesNotThrow(() -> PathUtil.createParent(Paths.get("file.txt")));
    }
}
