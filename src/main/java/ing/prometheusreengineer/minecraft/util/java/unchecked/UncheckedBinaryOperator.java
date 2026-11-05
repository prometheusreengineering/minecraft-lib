package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.BinaryOperator;

@FunctionalInterface
public interface UncheckedBinaryOperator<T> extends BinaryOperator<T>, UncheckedBiFunction<T, T, T> {
}
