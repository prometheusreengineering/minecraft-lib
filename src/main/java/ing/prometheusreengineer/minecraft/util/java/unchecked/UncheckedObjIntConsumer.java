package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.ObjIntConsumer;

@FunctionalInterface
public interface UncheckedObjIntConsumer<T> extends ObjIntConsumer<T> {
    void checkedAccept(T t, int value) throws Throwable;
    @Override
    default void accept(T t, int value) {
        try {
            checkedAccept(t, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
