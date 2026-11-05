package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.Consumer;

@FunctionalInterface
public interface UncheckedConsumer<T> extends Consumer<T> {
    void checkedAccept(T t) throws Throwable;
    @Override
    default void accept(T t) {
        try {
            checkedAccept(t);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
