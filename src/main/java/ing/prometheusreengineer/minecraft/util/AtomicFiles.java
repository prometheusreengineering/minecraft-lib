package ing.prometheusreengineer.minecraft.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes that never leave a half written file behind: the content goes to a temp file next to the target, which then
 * replaces the target in one move. A crash or a failed write leaves the old file as it was.
 */
public final class AtomicFiles {
    private AtomicFiles() {}

    @FunctionalInterface
    public interface Writer {
        void write(@NotNull OutputStream out) throws IOException;
    }

    /** Creates the parent folders. If {@code writer} throws, the target is untouched and the temp file is removed. */
    public static void write(@NotNull Path target, @NotNull Writer writer) throws IOException {
        Path absolute = target.toAbsolutePath();
        Path parent = absolute.getParent();
        Files.createDirectories(parent);

        // unique per call, so two writers of the same file never share a temp file
        Path tmp = Files.createTempFile(parent, absolute.getFileName().toString() + ".", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(tmp)) {
                writer.write(out);
            }
            try {
                Files.move(tmp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public static void write(@NotNull Path target, byte @NotNull [] bytes) throws IOException {
        write(target, out -> out.write(bytes));
    }

    /** UTF-8. */
    public static void writeString(@NotNull Path target, @NotNull String text) throws IOException {
        write(target, text.getBytes(StandardCharsets.UTF_8));
    }
}
