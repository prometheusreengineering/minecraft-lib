package ing.prometheusreengineer.minecraft.patch;

import org.jetbrains.annotations.NotNull;

/**
 * A patch's only way in. The loader core adds the patch jar to the classpath, registers its mixin configs, then calls
 * {@link #init} on every implementation it finds in
 * {@code META-INF/services/ing.prometheusreengineer.minecraft.patch.PrometheusPatch}.
 * <br>
 * Patches have no entry points of their own (no Fabric entrypoints, tweakers or mod metadata), so anything a patch
 * needs to run at startup goes here.
 */
@SuppressWarnings("unused")
public interface PrometheusPatch {
    /**
     * Called once, on the platform's loading thread. A thrown exception is logged by the core and only disables this
     * patch, it never crashes the game.
     */
    void init(@NotNull PatchContext context) throws Exception;
}
