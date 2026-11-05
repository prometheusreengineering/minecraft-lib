package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleFunction;

@FunctionalInterface
public interface UncheckedDoubleFunction<R> extends DoubleFunction<R> {
    R checkedApply(double value) throws Throwable;
    @Override
    default R apply(double value) {
        try {
            return checkedApply(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
