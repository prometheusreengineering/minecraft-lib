package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongToIntFunction;

@FunctionalInterface
public interface UncheckedLongToIntFunction extends LongToIntFunction {
    int checkedApplyAsInt(long value) throws Throwable;
    @Override
    default int applyAsInt(long value) {
        try {
            return checkedApplyAsInt(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
