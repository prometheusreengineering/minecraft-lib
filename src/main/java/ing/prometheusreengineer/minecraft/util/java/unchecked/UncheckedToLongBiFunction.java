package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToLongBiFunction;

@FunctionalInterface
public interface UncheckedToLongBiFunction<T, U> extends ToLongBiFunction<T, U> {
    long checkedApplyAsLong(T t, U u) throws Throwable;
    @Override
    default long applyAsLong(T t, U u) {
        try {
            return checkedApplyAsLong(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
