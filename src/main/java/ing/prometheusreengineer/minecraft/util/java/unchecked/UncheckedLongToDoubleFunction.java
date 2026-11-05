package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongToDoubleFunction;

@FunctionalInterface
public interface UncheckedLongToDoubleFunction extends LongToDoubleFunction {
    double checkedApplyAsDouble(long value) throws Throwable;
    @Override
    default double applyAsDouble(long value) {
        try {
            return checkedApplyAsDouble(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
