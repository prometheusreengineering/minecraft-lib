package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleToIntFunction;

@FunctionalInterface
public interface UncheckedDoubleToIntFunction extends DoubleToIntFunction {
    int checkedApplyAsInt(double value) throws Throwable;
    @Override
    default int applyAsInt(double value) {
        try {
            return checkedApplyAsInt(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
