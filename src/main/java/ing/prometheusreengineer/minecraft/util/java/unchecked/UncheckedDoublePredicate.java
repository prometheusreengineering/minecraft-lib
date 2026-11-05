package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.DoublePredicate;

@FunctionalInterface
public interface UncheckedDoublePredicate extends DoublePredicate {
    boolean checkedTest(double value) throws Throwable;
    @Override
    default boolean test(double value) {
        try {
            return checkedTest(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
