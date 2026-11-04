package ing.prometheusreengineer.minecraft.patch;

import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedFunction;
import org.jetbrains.annotations.NotNullByDefault;

/**
 * A patch's only way in. The loader core adds the patch jar to the classpath, registers its mixin configs, then calls
 * {@link #init} on every implementation it finds in
 * {@code META-INF/services/ing.prometheusreengineer.minecraft.patch.PrometheusPatch}.
 * <br>
 * Patches have no entry points of their own (no Fabric entrypoints, tweakers or mod metadata), so anything a patch
 * needs to run at startup goes here.
 * <br>
 * A thrown exception simply disables the patch.
 */
@FunctionalInterface
@NotNullByDefault
public interface PrometheusPatch extends UncheckedFunction<PatchContext, Void> {
    void init(PatchContext context) throws Exception;

    @Override
    default Void checkedApply(PatchContext context) throws Throwable {
        init(context);
        return null;
    }
}
