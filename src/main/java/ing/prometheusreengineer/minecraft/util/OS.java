package ing.prometheusreengineer.minecraft.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public enum OS {
    Windows(PathUtil.fromEnvOr("APPDATA", PathUtil.fromHomeFolder("AppData", "Roaming")).resolve(".minecraft")),
    MacOS(PathUtil.fromHomeFolder("Library", "Application Support")),
    Linux(PathUtil.fromEnvOr("XDG_DATA_HOME", PathUtil.fromHomeFolder(".local", "share")));

    OS(@NotNull Path configFolder) {
        this.configFolder = configFolder;
    }

    private final @NotNull Path configFolder;
    /**
     * Not final so tests can try every OS on one machine.
     */
    static @NotNull OS current = detect(System.getProperty("os.name"));

    @Contract(pure = true)
    static @NotNull OS detect(@NotNull String osName) {
        String name = osName.toLowerCase(Locale.ROOT);
        // mac first: "darwin" contains "win"
        if (name.contains("mac") || name.contains("darwin")) return MacOS;
        if (name.contains("win")) return Windows;
        return Linux;
    }

    @Contract(value = "-> new", pure = true)
    public static @NotNull Path getConfigFolder() {
        // -Dprometheus.folder or PROMETHEUS_FOLDER
        String override = Settings.get("folder");
        Path folder = override != null ? Paths.get(override) : current.configFolder.resolve("prometheus");
        // Absolute so it doesn't depend on the working directory the game was launched from
        return folder.toAbsolutePath();
    }

    @Contract(pure = true)
    public static boolean isOnWindows() {
        return current == Windows;
    }
}
