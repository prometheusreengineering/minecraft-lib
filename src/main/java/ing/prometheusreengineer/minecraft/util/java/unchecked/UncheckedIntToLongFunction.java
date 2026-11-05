package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntToLongFunction;

@FunctionalInterface
public interface UncheckedIntToLongFunction extends IntToLongFunction {
    long checkedApplyAsLong(int value) throws Throwable;
    @Override
    default long applyAsLong(int value) {
        try {
            return checkedApplyAsLong(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
