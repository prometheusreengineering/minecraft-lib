package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface UncheckedBooleanSupplier extends BooleanSupplier {
    boolean checkedGetAsBoolean() throws Throwable;
    @Override
    default boolean getAsBoolean() {
        try {
            return checkedGetAsBoolean();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
