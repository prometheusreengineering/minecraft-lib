package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.Function;

@FunctionalInterface
public interface UncheckedFunction<T, R> extends Function<T, R> {
    R checkedApply(T t) throws Throwable;
    @Override
    default R apply(T t) {
        try {
            return checkedApply(t);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
