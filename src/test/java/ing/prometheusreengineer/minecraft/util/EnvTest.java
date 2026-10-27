package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvTest {
    private static final Env.Write FAIL = (name, value) -> {
        throw new IllegalStateException("boom");
    };
    private static final Env.Write NOTHING = (name, value) -> {};
    private static final Env.Write REAL = Env::setenv;

    /**
     * A static map like ProcessEnvironment's, so caseInsensitive can be tested on every OS (only Windows has the
     * real one).
     */
    static final Map<String, String> fakeCaseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @TempDir
    Path folder;

    @BeforeEach
    void setUp() {
        // Env's failures log through Log.lib, keep them out of the real prometheus folder
        System.setProperty("prometheus.folder", folder.toString());
        System.setProperty("prometheus.log.console", "off");
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("prometheus.folder");
        System.clearProperty("prometheus.log.console");
    }

    /**
     * Unique per test, the environment is shared by the whole JVM.
     */
    private static String name() {
        return "PROMETHEUS_TEST_" + UUID.randomUUID().toString().replace('-', '_').toUpperCase();
    }

    @Test
    void setsAVariable() {
        String name = name();
        Env.setenv(name, "value");
        assertEquals("value", System.getenv(name));
        assertEquals("value", System.getenv().get(name));
    }

    @Test
    void requiresNameAndValue() {
        assertThrows(IllegalArgumentException.class, () -> Env.setenv(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> Env.setenv(name(), null));
    }

    @Test
    void swapIsTheFallback() {
        String name = name();
        Env.setenv(name, "value", FAIL, REAL, NOTHING);
        assertEquals("value", System.getenv(name));
    }

    @Test
    void eachRealWrite() throws Exception {
        String inPlace = name();
        Env.IN_PLACE.write(inPlace, "value");
        assertEquals("value", System.getenv().get(inPlace));

        String swapped = name();
        Env.SWAP.write(swapped, "value");
        assertEquals("value", System.getenv().get(swapped));

        // Windows only has the field; elsewhere this is a no-op, and must not throw either way
        assertDoesNotThrow(() -> Env.CASE_INSENSITIVE.write(name(), "value"));
    }

    @Test
    void theFirstFailureIsKept() {
        String name = name();
        // in-place fails first, so the case-insensitive failure isn't the one reported
        assertDoesNotThrow(() -> Env.setenv(name, "value", FAIL, REAL, FAIL));
        assertEquals("value", System.getenv(name));
    }

    @Test
    void bothWritesFailing() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Env.setenv(name(), "value", FAIL, FAIL, NOTHING));
        assertTrue(e.getMessage().startsWith("setenv failed"), e.getMessage());
    }

    @Test
    void caseInsensitiveFailingIsFineIfTheValueIsThere() {
        String name = name();
        assertDoesNotThrow(() -> Env.setenv(name, "value", REAL, FAIL, FAIL));
        assertEquals("value", System.getenv(name));
    }

    @Test
    void notSetNamesTheFirstFailure() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Env.setenv(name(), "value", FAIL, NOTHING, NOTHING));
        assertTrue(e.getMessage().contains("in-place threw java.lang.IllegalStateException: boom"), e.getMessage());
        assertFalse(e.getMessage().contains("was:"), e.getMessage());
    }

    @Test
    void notSetNamesTheOriginalValue() {
        String name = name();
        Env.setenv(name, "old");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Env.setenv(name, "new", NOTHING, NOTHING, NOTHING));
        assertTrue(e.getMessage().contains("(was: old)"), e.getMessage());
        assertFalse(e.getMessage().contains("in-place threw"), e.getMessage());
    }

    @Test
    void notSetIgnoresAnEmptyOriginal() {
        String name = name();
        Env.setenv(name, "");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Env.setenv(name, "new", NOTHING, NOTHING, NOTHING));
        assertFalse(e.getMessage().contains("was:"), e.getMessage());
    }

    @Test
    void everyWriteNeedsUnsafe() {
        assertThrows(IllegalStateException.class, () -> Env.inPlace(null, name(), "value"));
        assertThrows(IllegalStateException.class, () -> Env.swapField(null, name(), "value"));
        assertThrows(IllegalStateException.class, () -> Env.caseInsensitive(null, EnvTest.class, "fakeCaseInsensitive", name(), "value"));
    }

    @Test
    void swapFieldReplacesTheMap() throws Exception {
        String name = name();
        Env.swapField(Env.theUnsafe(), name, "value");
        assertEquals("value", System.getenv().get(name));
    }

    @Test
    void caseInsensitiveWritesTheStaticMap() throws Exception {
        String name = name();
        Env.caseInsensitive(Env.theUnsafe(), EnvTest.class, "fakeCaseInsensitive", name, "value");
        assertEquals("value", fakeCaseInsensitive.get(name.toLowerCase()));
    }

    @Test
    void caseInsensitiveIsANoOpWithoutTheField() {
        assertDoesNotThrow(() -> Env.caseInsensitive(null, EnvTest.class, "notAField", name(), "value"));
    }

    @Test
    void loadingUnsafe() {
        assertNotNull(Env.load(Env::theUnsafe));
        assertNull(Env.load(() -> {
            throw new Exception("no Unsafe here");
        }));
    }
}
