package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntConsumer;

@FunctionalInterface
public interface UncheckedIntConsumer extends IntConsumer {
    void checkedAccept(int value) throws Throwable;
    @Override
    default void accept(int value) {
        try {
            checkedAccept(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
