package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongUnaryOperator;

@FunctionalInterface
public interface UncheckedLongUnaryOperator extends LongUnaryOperator {
    long checkedApplyAsLong(long operand) throws Throwable;
    @Override
    default long applyAsLong(long operand) {
        try {
            return checkedApplyAsLong(operand);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
