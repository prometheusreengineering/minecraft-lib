package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongSupplier;

@FunctionalInterface
public interface UncheckedLongSupplier extends LongSupplier {
    long checkedGetAsLong() throws Throwable;
    @Override
    default long getAsLong() {
        try {
            return checkedGetAsLong();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
