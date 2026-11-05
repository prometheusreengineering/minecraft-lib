package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntToDoubleFunction;

@FunctionalInterface
public interface UncheckedIntToDoubleFunction extends IntToDoubleFunction {
    double checkedApplyAsDouble(int value) throws Throwable;
    @Override
    default double applyAsDouble(int value) {
        try {
            return checkedApplyAsDouble(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
