package ing.prometheusreengineer.minecraft.patch;

import ing.prometheusreengineer.minecraft.log.Log;
import ing.prometheusreengineer.minecraft.util.OS;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * What the loader core hands a {@link PrometheusPatch}. Created by the core, one per patch.
 * <br>
 * This does not claim the lib with {@link Log#setOwner}: every patch shares the core's one copy of the lib, so the
 * lib's own lines belong to the core ({@code loader}), and a patch logs through {@link #log} instead.
 */
public final class PatchContext {
    private final @NotNull String project;
    private final @NotNull String platform;
    private final @NotNull String coreVersion;

    /**
     * @param project     the patch's name, its log file and settings key, ex: {@code lunar}
     * @param platform    what the core is running on, ex: {@code fabric}, {@code launchwrapper}, {@code modlauncher}
     * @param coreVersion the version of the core (and so of this lib) that loaded the patch
     */
    public PatchContext(@NotNull String project, @NotNull String platform, @NotNull String coreVersion) {
        this.project = project;
        this.platform = platform;
        this.coreVersion = coreVersion;
    }

    @Contract(pure = true)
    public @NotNull String project() {
        return project;
    }

    @Contract(pure = true)
    public @NotNull String platform() {
        return platform;
    }

    @Contract(pure = true)
    public @NotNull String coreVersion() {
        return coreVersion;
    }

    /**
     * {@code <prometheus folder>/<project>}, not created.
     */
    @Contract(value = "-> new", pure = true)
    public @NotNull Path folder() {
        return OS.getConfigFolder().resolve(project);
    }

    /**
     * {@code log()} logs as {@code Prometheus/<project>}, {@code log("ECM")} as {@code Prometheus/<project>/ECM}.
     */
    @Contract("_ -> new")
    public @NotNull Log log(@NotNull String @NotNull ... path) {
        return Log.get(project, path);
    }
}
