package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntFunction;

@FunctionalInterface
public interface UncheckedIntFunction<R> extends IntFunction<R> {
    R checkedApply(int value) throws Throwable;
    @Override
    default R apply(int value) {
        try {
            return checkedApply(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
