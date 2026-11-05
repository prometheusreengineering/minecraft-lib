package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.Predicate;

@FunctionalInterface
public interface UncheckedPredicate<T> extends Predicate<T> {
    boolean checkedTest(T t) throws Throwable;
    @Override
    default boolean test(T t) {
        try {
            return checkedTest(t);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
