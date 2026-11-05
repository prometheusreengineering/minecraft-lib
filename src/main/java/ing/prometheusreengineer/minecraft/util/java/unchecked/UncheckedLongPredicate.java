package ing.prometheusreengineer.minecraft.util.java.unchecked;

import java.util.function.LongPredicate;

@FunctionalInterface
public interface UncheckedLongPredicate extends LongPredicate {
    boolean checkedTest(long value) throws Throwable;
    @Override
    default boolean test(long value) {
        try {
            return checkedTest(value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
