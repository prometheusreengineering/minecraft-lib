package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleBinaryOperator;

@FunctionalInterface
public interface UncheckedDoubleBinaryOperator extends DoubleBinaryOperator {
    double checkedApplyAsDouble(double left, double right) throws Throwable;
    @Override
    default double applyAsDouble(double left, double right) {
        try {
            return checkedApplyAsDouble(left, right);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
