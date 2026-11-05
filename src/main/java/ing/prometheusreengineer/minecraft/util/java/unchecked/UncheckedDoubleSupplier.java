package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleSupplier;

@FunctionalInterface
public interface UncheckedDoubleSupplier extends DoubleSupplier {
    double checkedGetAsDouble() throws Throwable;
    @Override
    default double getAsDouble() {
        try {
            return checkedGetAsDouble();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
