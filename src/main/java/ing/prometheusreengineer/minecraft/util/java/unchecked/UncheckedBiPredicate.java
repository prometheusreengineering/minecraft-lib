package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.BiPredicate;

@FunctionalInterface
public interface UncheckedBiPredicate<T, U> extends BiPredicate<T, U> {
    boolean checkedTest(T t, U u) throws Throwable;
    @Override
    default boolean test(T t, U u) {
        try {
            return checkedTest(t, u);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
