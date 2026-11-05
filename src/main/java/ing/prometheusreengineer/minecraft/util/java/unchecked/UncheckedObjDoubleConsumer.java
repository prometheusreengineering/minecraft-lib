package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ObjDoubleConsumer;

@FunctionalInterface
public interface UncheckedObjDoubleConsumer<T> extends ObjDoubleConsumer<T> {
    void checkedAccept(T t, double value) throws Throwable;
    @Override
    default void accept(T t, double value) {
        try {
            checkedAccept(t, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
