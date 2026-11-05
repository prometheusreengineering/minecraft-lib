package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleUnaryOperator;

@FunctionalInterface
public interface UncheckedDoubleUnaryOperator extends DoubleUnaryOperator {
    double checkedApplyAsDouble(double operand) throws Throwable;
    @Override
    default double applyAsDouble(double operand) {
        try {
            return checkedApplyAsDouble(operand);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
