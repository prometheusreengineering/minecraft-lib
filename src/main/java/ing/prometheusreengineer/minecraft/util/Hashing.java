package ing.prometheusreengineer.minecraft.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Lowercase hex digests. Uses the JDK only, Guava's {@code Hashing} differs between the versions games ship.
 */
public final class Hashing {
    private Hashing() {}

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    public static @NotNull String sha256(@NotNull Path file) throws IOException {
        return hash(file, "SHA-256");
    }

    public static @NotNull String sha1(@NotNull Path file) throws IOException {
        return hash(file, "SHA-1");
    }

    @Contract(pure = true)
    public static @NotNull String sha256(byte @NotNull [] bytes) {
        MessageDigest digest = digest("SHA-256");
        return hex(digest.digest(bytes));
    }

    /**
     * Compares a digest to an expected one, ignoring case and a {@code sha256:} style prefix (GitHub's release asset
     * {@code digest} field looks like {@code sha256:ab12...}).
     */
    @Contract(value = "_, null -> false", pure = true)
    public static boolean matches(@NotNull String actual, @Nullable String expected) {
        if (expected == null) return false;
        String bare = expected.trim();
        int colon = bare.indexOf(':');
        if (colon >= 0) bare = bare.substring(colon + 1);
        return actual.equalsIgnoreCase(bare);
    }

    static @NotNull MessageDigest digest(@NotNull String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " is required of every JRE", e);
        }
    }

    @Contract(pure = true)
    static @NotNull String hex(byte @NotNull [] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = HEX[(bytes[i] >> 4) & 0xF];
            out[i * 2 + 1] = HEX[bytes[i] & 0xF];
        }
        return new String(out);
    }

    private static @NotNull String hash(@NotNull Path file, @NotNull String algorithm) throws IOException {
        MessageDigest digest = digest(algorithm);
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[1 << 16];
            for (int n; (n = in.read(buffer)) != -1; ) digest.update(buffer, 0, n);
        }
        return hex(digest.digest());
    }
}
