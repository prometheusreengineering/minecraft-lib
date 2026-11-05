package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntSupplier;

@FunctionalInterface
public interface UncheckedIntSupplier extends IntSupplier {
    int checkedGetAsInt() throws Throwable;
    @Override
    default int getAsInt() {
        try {
            return checkedGetAsInt();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
