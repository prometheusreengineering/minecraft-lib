package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongFunction;

@FunctionalInterface
public interface UncheckedLongFunction<R> extends LongFunction<R> {
    R checkedApply(long value) throws Throwable;
    @Override
    default R apply(long value) {
        try {
            return checkedApply(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
