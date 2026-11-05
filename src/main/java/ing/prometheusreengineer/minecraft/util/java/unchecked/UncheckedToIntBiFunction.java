package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToIntBiFunction;

@FunctionalInterface
public interface UncheckedToIntBiFunction<T, U> extends ToIntBiFunction<T, U> {
    int checkedApplyAsInt(T t, U u) throws Throwable;
    @Override
    default int applyAsInt(T t, U u) {
        try {
            return checkedApplyAsInt(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
