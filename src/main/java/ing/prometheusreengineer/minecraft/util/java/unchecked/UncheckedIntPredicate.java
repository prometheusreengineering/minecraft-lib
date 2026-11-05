package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.IntPredicate;

@FunctionalInterface
public interface UncheckedIntPredicate extends IntPredicate {
    boolean checkedTest(int value) throws Throwable;
    @Override
    default boolean test(int value) {
        try {
            return checkedTest(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
