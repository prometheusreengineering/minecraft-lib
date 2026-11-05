package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToDoubleFunction;

@FunctionalInterface
public interface UncheckedToDoubleFunction<T> extends ToDoubleFunction<T> {
    double checkedApplyAsDouble(T value) throws Throwable;
    @Override
    default double applyAsDouble(T value) {
        try {
            return checkedApplyAsDouble(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
