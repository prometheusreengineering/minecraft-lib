package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToLongFunction;

@FunctionalInterface
public interface UncheckedToLongFunction<T> extends ToLongFunction<T> {
    long checkedApplyAsLong(T value) throws Throwable;
    @Override
    default long applyAsLong(T value) {
        try {
            return checkedApplyAsLong(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
