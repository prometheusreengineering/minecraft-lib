package ing.prometheusreengineer.minecraft.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public final class PathUtil {
    private PathUtil() {}

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Path fromHomeFolder(@NotNull String... paths) {
        return Paths.get(System.getProperty("user.home"), paths);
    }

    @NotNull
    @Contract(value = "null, _ -> param2", pure = true)
    // Passing null to env is the same as passing a non-existent key (and will return the fallback).
    public static Path fromEnvOr(@Nullable String env, @NotNull Path d) {
        if (env == null) return d;
        String path = System.getenv(env);
        if (path == null || path.isEmpty()) return d;
        return Paths.get(path);
    }

    // mkdir -p $(dirname dst) && cp -r src dst
    // Makes all the folders required for dst, then copies src to dst
    public static void copyRecursively(@NotNull Path src, @NotNull Path dst) throws IOException {
        File source = src.toFile();
        if (source.isDirectory()) {
            Files.createDirectories(dst);
            for (File child : children(source)) {
                copyRecursively(child.toPath(), dst.resolve(child.getName()));
            }
        } else {
            createParent(dst);
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // cp -rn src dst
    // Like copyRecursively, but never overwrites files that already exist in dst
    public static void copyMissingRecursively(@NotNull Path src, @NotNull Path dst) throws IOException {
        File source = src.toFile();
        if (source.isDirectory()) {
            Files.createDirectories(dst);
            for (File child : children(source)) {
                copyMissingRecursively(child.toPath(), dst.resolve(child.getName()));
            }
        } else if (!Files.exists(dst)) {
            createParent(dst);
            Files.copy(src, dst);
        }
    }

    // rm -rf path
    // Deletes a folder and all subfolders
    public static void deleteRecursively(@NotNull Path path) throws IOException {
        File file = path.toFile();
        if (file.isDirectory()) {
            for (File child : children(file)) {
                deleteRecursively(child.toPath());
            }
        }
        Files.deleteIfExists(path);
    }

    @Contract(value = "null -> true", pure = true)
    public static boolean isEmptyOrNull(@Nullable File dir) {
        return dir == null || children(dir).length == 0;
    }

    /**
     * {@link File#listFiles()}, but empty instead of null when {@code dir} isn't a folder or can't be read.
     */
    static File @NotNull [] children(@NotNull File dir) {
        File[] children = dir.listFiles();
        return children == null ? new File[0] : children;
    }

    /**
     * A relative path like {@code file.txt} has no parent, and needs none created.
     */
    static void createParent(@NotNull Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
    }
}
