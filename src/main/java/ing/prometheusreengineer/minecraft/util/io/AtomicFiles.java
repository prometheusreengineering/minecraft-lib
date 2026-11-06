package ing.prometheusreengineer.minecraft.util.io;

import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes that never leave a half written file behind: the content goes to a temp file next to the target, which then
 * replaces the target in one move. A crash or a failed write leaves the old file as it was.
 */
@NotNullByDefault
public final class AtomicFiles {
    private AtomicFiles() {}

    @FunctionalInterface
    public interface Writer {
        void write(OutputStream out) throws IOException;
    }

    /**
     * {@link Files#move}, swappable so tests can reach the fallback for file systems without atomic moves.
     */
    @FunctionalInterface
    interface Mover {
        void move(Path source, Path target, CopyOption... options) throws IOException;
    }

    /**
     * Creates the parent folders. If {@code writer} throws, the target is untouched and the temp file is removed.
     */
    public static void write(Path target, Writer writer) throws IOException {
        write(target, writer, Files::move);
    }

    static void write(Path target, Writer writer, Mover mover) throws IOException {
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
                mover.move(tmp, absolute, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                mover.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    public static void write(Path target, byte[] bytes) throws IOException {
        write(target, out -> out.write(bytes));
    }

    /**
     * UTF-8.
     */
    public static void writeString(Path target, String text) throws IOException {
        write(target, text.getBytes(StandardCharsets.UTF_8));
    }
}
