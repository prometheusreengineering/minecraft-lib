package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleToLongFunction;

@FunctionalInterface
public interface UncheckedDoubleToLongFunction extends DoubleToLongFunction {
    long checkedApplyAsLong(double value) throws Throwable;
    @Override
    default long applyAsLong(double value) {
        try {
            return checkedApplyAsLong(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
