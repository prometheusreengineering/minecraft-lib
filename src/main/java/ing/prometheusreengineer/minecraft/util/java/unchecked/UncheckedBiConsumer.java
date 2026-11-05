package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface UncheckedBiConsumer<T, U> extends BiConsumer<T, U> {
    void checkedAccept(T t, U u) throws Throwable;
    @Override
    default void accept(T t, U u) {
        try {
            checkedAccept(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
