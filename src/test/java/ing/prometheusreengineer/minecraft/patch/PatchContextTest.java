package ing.prometheusreengineer.minecraft.patch;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatchContextTest {
    @TempDir
    Path folder;

    @BeforeEach
    void setUp() {
        System.setProperty("prometheus.folder", folder.toString());
        System.setProperty("prometheus.log.console", "off");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("prometheus.folder");
        System.clearProperty("prometheus.log.console");
    }

    /**
     * Registered in src/test/resources/META-INF/services, like a real patch.
     */
    public static final class TestPatch implements PrometheusPatch {
        static PatchContext received;

        @Override
        public void init(@NotNull PatchContext context) {
            received = context;
            context.log("Init").info("on %s, core %s", context.platform(), context.coreVersion());
        }
    }

    @Test
    void contextGivesWhatTheCorePassed() {
        PatchContext context = new PatchContext("patchtest", "fabric", "2.0.0");
        assertEquals("patchtest", context.project());
        assertEquals("fabric", context.platform());
        assertEquals("2.0.0", context.coreVersion());
        assertEquals(folder.resolve("patchtest").toAbsolutePath(), context.folder());
    }

    @Test
    void coreFindsAndInitsThePatch() throws Exception {
        List<PrometheusPatch> patches = new ArrayList<>();
        for (PrometheusPatch patch : ServiceLoader.load(PrometheusPatch.class)) patches.add(patch);
        assertEquals(1, patches.size());
        assertTrue(patches.get(0) instanceof TestPatch);

        PatchContext context = new PatchContext("patchinit", "launchwrapper", "2.0.0");
        patches.get(0).init(context);

        assertSame(context, TestPatch.received);
        assertTrue(read("patchinit").contains("INFO  Prometheus/patchinit/Init: on launchwrapper, core 2.0.0"), read("patchinit"));
    }

    private String read(String project) throws IOException {
        return new String(Files.readAllBytes(folder.resolve("logs").resolve(project + ".log")), StandardCharsets.UTF_8);
    }
}
