package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface UncheckedIntUnaryOperator extends IntUnaryOperator {
    int checkedApplyAsInt(int operand) throws Throwable;
    @Override
    default int applyAsInt(int operand) {
        try {
            return checkedApplyAsInt(operand);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
