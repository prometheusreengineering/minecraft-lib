package ing.prometheusreengineer.minecraft.util.java;

import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedFunction;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedRunnable;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.function.Supplier;

@NotNullByDefault
public final class LambdaUtil {
    private LambdaUtil() {}

    public static boolean runCatching(UncheckedRunnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    public static <T> boolean runCatching(UncheckedFunction<T, Void> func, T arg) {
        try {
            func.apply(arg);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    public static <T> boolean runCatching(UncheckedSupplier<T> func) {
        try {
            func.get();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    public static <T, R> R runCatchingOr(UncheckedFunction<T, R> func, T arg, R fallback) {
        try {
            return func.apply(arg);
        } catch (Throwable e) {
            return fallback;
        }
    }
    public static <R> R runCatchingOr(UncheckedSupplier<R> func, R fallback) {
        try {
            return func.get();
        } catch (Throwable e) {
            return fallback;
        }
    }
}
