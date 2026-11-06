package ing.prometheusreengineer.minecraft.util.os;

import ing.prometheusreengineer.minecraft.util.Settings;
import ing.prometheusreengineer.minecraft.util.io.PathUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@NotNullByDefault
public enum OS {
    Windows(PathUtil.fromEnvOr("APPDATA", PathUtil.fromHomeFolder("AppData", "Roaming"))),
    MacOS(PathUtil.fromHomeFolder("Library", "Application Support")),
    Linux(PathUtil.fromEnvOr("XDG_DATA_HOME", PathUtil.fromHomeFolder(".local", "share")));

    OS(Path configFolder) {
        this.configFolder = configFolder;
    }

    private final Path configFolder;
    @VisibleForTesting
    static OS current = detect(System.getProperty("os.name"));

    @VisibleForTesting
    @Contract(pure = true)
    static OS detect(String osName) {
        String name = osName.toLowerCase(Locale.ROOT);
        // mac first: "darwin" contains "win"
        if (name.contains("mac") || name.contains("darwin")) return MacOS;
        if (name.contains("win")) return Windows;
        return Linux;
    }

    @Contract(value = "-> new", pure = true)
    public static Path getConfigFolder() {
        // -Dprometheus.folder or PROMETHEUS_FOLDER
        String override = Settings.get("folder");
        Path folder = override != null ? Paths.get(override) : current.configFolder.resolve("prometheus");
        return folder.toAbsolutePath();
    }

    @Contract(pure = true)
    public static boolean isOnWindows() {
        return current == Windows;
    }
}
