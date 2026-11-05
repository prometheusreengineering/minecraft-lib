package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntBinaryOperator;

@FunctionalInterface
public interface UncheckedIntBinaryOperator extends IntBinaryOperator {
    int checkedApplyAsInt(int left, int right) throws Throwable;
    @Override
    default int applyAsInt(int left, int right) {
        try {
            return checkedApplyAsInt(left, right);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
