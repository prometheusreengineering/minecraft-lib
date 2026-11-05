package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.BiFunction;

@FunctionalInterface
public interface UncheckedBiFunction<T, U, R> extends BiFunction<T, U, R> {
    R checkedApply(T t, U u) throws Throwable;
    @Override
    default R apply(T t, U u) {
        try {
            return checkedApply(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
