package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ToDoubleBiFunction;

@FunctionalInterface
public interface UncheckedToDoubleBiFunction<T, U> extends ToDoubleBiFunction<T, U> {
    double checkedApplyAsDouble(T t, U u) throws Throwable;
    @Override
    default double applyAsDouble(T t, U u) {
        try {
            return checkedApplyAsDouble(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
