package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtomicFilesTest {
    @TempDir
    Path folder;

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private long files() throws IOException {
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(Files::isRegularFile).count();
        }
    }

    @Test
    void createsParentsAndReplaces() throws IOException {
        Path target = folder.resolve("a").resolve("b").resolve("config.json");
        AtomicFiles.writeString(target, "first");
        AtomicFiles.writeString(target, "second");

        assertEquals("second", read(target));
        assertEquals(1, files(), "no temp files left");
    }

    @Test
    void failedWriteKeepsTheOldFile() throws IOException {
        Path target = folder.resolve("config.json");
        AtomicFiles.writeString(target, "good");

        assertThrows(IOException.class, () -> AtomicFiles.write(target, out -> {
            out.write("half".getBytes(StandardCharsets.UTF_8));
            throw new IOException("crashed mid write");
        }));

        assertEquals("good", read(target));
        assertEquals(1, files(), "no temp files left");
    }

    @Test
    void plainMoveWhereAtomicIsNotSupported() throws IOException {
        Path target = folder.resolve("config.json");
        AtomicFiles.write(target, out -> out.write("moved".getBytes(StandardCharsets.UTF_8)), (source, to, options) -> {
            if (Arrays.asList(options).contains(StandardCopyOption.ATOMIC_MOVE)) throw new AtomicMoveNotSupportedException(source.toString(), to.toString(), "test");
            Files.move(source, to, options);
        });

        assertEquals("moved", read(target));
        assertEquals(1, files(), "no temp files left");
    }
}
