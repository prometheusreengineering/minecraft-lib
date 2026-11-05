package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoubleConsumer;

@FunctionalInterface
public interface UncheckedDoubleConsumer extends DoubleConsumer {
    void checkedAccept(double value) throws Throwable;
    @Override
    default void accept(double value) {
        try {
            checkedAccept(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
