package ing.prometheusreengineer.minecraft.util.java.unchecked;

@FunctionalInterface
public interface UncheckedFunction<T, R> {
    R checkedApply(T t) throws Throwable;
    default R apply(T t) {
        try {
            return checkedApply(t);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
