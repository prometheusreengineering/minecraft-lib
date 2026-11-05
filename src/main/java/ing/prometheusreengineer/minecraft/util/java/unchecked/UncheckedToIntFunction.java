package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToIntFunction;

@FunctionalInterface
public interface UncheckedToIntFunction<T> extends ToIntFunction<T> {
    int checkedApplyAsInt(T value) throws Throwable;
    @Override
    default int applyAsInt(T value) {
        try {
            return checkedApplyAsInt(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
