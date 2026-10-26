package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {
    @AfterEach
    void clear() {
        System.clearProperty("prometheus.test.value");
    }

    @Test
    void readsSystemProperty() {
        System.setProperty("prometheus.test.value", "  hello ");
        assertEquals("hello", Settings.get("test.value"));
    }

    @Test
    void blankIsUnset() {
        System.setProperty("prometheus.test.value", "   ");
        assertNull(Settings.get("test.value"));
        assertEquals("fallback", Settings.get("test.value", "fallback"));
    }

    @Test
    void booleansAreStrict() {
        System.setProperty("prometheus.test.value", "TRUE");
        assertTrue(Settings.getBoolean("test.value", false));
        System.setProperty("prometheus.test.value", "yes");
        assertFalse(Settings.getBoolean("test.value", false));
    }

    @Test
    void badIntIsFallback() {
        System.setProperty("prometheus.test.value", "80a");
        assertEquals(8099, Settings.getInt("test.value", 8099));
        System.setProperty("prometheus.test.value", "25565");
        assertEquals(25565, Settings.getInt("test.value", 8099));
    }

    @Test
    void folderOverride() {
        System.setProperty("prometheus.folder", "some/relative");
        try {
            assertTrue(OS.getConfigFolder().isAbsolute());
            assertTrue(OS.getConfigFolder().endsWith("some/relative"));
        } finally {
            System.clearProperty("prometheus.folder");
        }
    }
}
