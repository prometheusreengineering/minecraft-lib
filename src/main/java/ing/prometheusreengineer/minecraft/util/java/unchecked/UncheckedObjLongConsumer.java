package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ObjLongConsumer;

@FunctionalInterface
public interface UncheckedObjLongConsumer<T> extends ObjLongConsumer<T> {
    void checkedAccept(T t, long value) throws Throwable;
    @Override
    default void accept(T t, long value) {
        try {
            checkedAccept(t, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
