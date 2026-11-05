package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongConsumer;

@FunctionalInterface
public interface UncheckedLongConsumer extends LongConsumer {
    void checkedAccept(long value) throws Throwable;
    @Override
    default void accept(long value) {
        try {
            checkedAccept(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
