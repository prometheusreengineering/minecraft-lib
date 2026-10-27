package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OSTest {
    private final OS real = OS.current;

    @AfterEach
    void restore() {
        OS.current = real;
        System.clearProperty("prometheus.folder");
    }

    @Test
    void detectsFromOsName() {
        assertEquals(OS.Windows, OS.detect("Windows 11"));
        assertEquals(OS.MacOS, OS.detect("Mac OS X"));
        assertEquals(OS.MacOS, OS.detect("Darwin"));
        assertEquals(OS.Linux, OS.detect("Linux"));
        assertEquals(OS.Linux, OS.detect("FreeBSD"));
    }

    @Test
    void isOnWindows() {
        OS.current = OS.Windows;
        assertTrue(OS.isOnWindows());
        OS.current = OS.Linux;
        assertFalse(OS.isOnWindows());
    }

    @Test
    void defaultFolderIsAbsoluteAndNamedPrometheus() {
        System.clearProperty("prometheus.folder");
        Path folder = OS.getConfigFolder();
        assertTrue(folder.isAbsolute());
        assertEquals("prometheus", folder.getFileName().toString());
    }

    @Test
    void folderOverride() {
        System.setProperty("prometheus.folder", "some/relative");
        assertTrue(OS.getConfigFolder().isAbsolute());
        assertTrue(OS.getConfigFolder().endsWith("some/relative"));
    }
}
