package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongBinaryOperator;

@FunctionalInterface
public interface UncheckedLongBinaryOperator extends LongBinaryOperator {
    long checkedApplyAsLong(long left, long right) throws Throwable;
    @Override
    default long applyAsLong(long left, long right) {
        try {
            return checkedApplyAsLong(left, right);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
