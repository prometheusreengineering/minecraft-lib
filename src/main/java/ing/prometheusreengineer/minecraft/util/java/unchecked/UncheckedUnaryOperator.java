package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface UncheckedUnaryOperator<T> extends UnaryOperator<T>, UncheckedFunction<T, T> {
}
