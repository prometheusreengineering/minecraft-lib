package ing.prometheusreengineer.minecraft.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashingTest {
    // FIPS 180-2 test vectors for "abc"
    private static final String ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    private static final String ABC_SHA1 = "a9993e364706816aba3e25717850c26c9cd0d89d";

    @TempDir
    Path folder;

    @Test
    void knownVectors() throws IOException {
        Path file = folder.resolve("abc");
        Files.write(file, "abc".getBytes(StandardCharsets.US_ASCII));

        assertEquals(ABC_SHA256, Hashing.sha256(file));
        assertEquals(ABC_SHA1, Hashing.sha1(file));
        assertEquals(ABC_SHA256, Hashing.sha256("abc".getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    void matchesGithubDigests() {
        assertTrue(Hashing.matches(ABC_SHA256, "sha256:" + ABC_SHA256.toUpperCase()));
        assertTrue(Hashing.matches(ABC_SHA256, " " + ABC_SHA256 + " "));
        assertFalse(Hashing.matches(ABC_SHA256, ABC_SHA1));
        assertFalse(Hashing.matches(ABC_SHA256, null));
    }
}
