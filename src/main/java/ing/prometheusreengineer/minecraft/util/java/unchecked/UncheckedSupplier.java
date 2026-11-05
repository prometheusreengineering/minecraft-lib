package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.Supplier;

@FunctionalInterface
public interface UncheckedSupplier<T> extends Supplier<T> {
    T checkedGet() throws Throwable;
    @Override
    default T get() {
        try {
            return checkedGet();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
