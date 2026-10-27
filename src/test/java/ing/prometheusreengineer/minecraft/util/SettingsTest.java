package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
        assertEquals("hello", Settings.get("test.value", "fallback"));
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
    void falseAndMissingBooleans() {
        System.setProperty("prometheus.test.value", "false");
        assertFalse(Settings.getBoolean("test.value", true));
        System.clearProperty("prometheus.test.value");
        assertTrue(Settings.getBoolean("test.value", true));
    }

    @Test
    void missingIntIsFallback() {
        assertEquals(8099, Settings.getInt("test.value", 8099));
    }

    @Test
    void envWhenNoProperty() {
        // test.env.<random> is read from PROMETHEUS_TEST_ENV_<RANDOM>
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Env.setenv("PROMETHEUS_TEST_ENV_" + suffix.toUpperCase(), " from env ");
        assertEquals("from env", Settings.get("test.env." + suffix));
    }

    @Test
    void propertyBeatsEnv() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        Env.setenv("PROMETHEUS_TEST_ENV_" + suffix.toUpperCase(), "from env");
        System.setProperty("prometheus.test.env." + suffix, "from property");
        try {
            assertEquals("from property", Settings.get("test.env." + suffix));
        } finally {
            System.clearProperty("prometheus.test.env." + suffix);
        }
    }
}
