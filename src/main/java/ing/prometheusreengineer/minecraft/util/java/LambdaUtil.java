package ing.prometheusreengineer.minecraft.util.java;

import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedBooleanSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedDoubleSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedIntSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedLongSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedRunnable;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedSupplier;
import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public final class LambdaUtil {
    private LambdaUtil() {}

    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(UncheckedRunnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    /**
     * Returns true if the function returned successfully.
     */
    public static <T> boolean runCatching(UncheckedSupplier<T> func) {
        try {
            func.get();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(UncheckedBooleanSupplier func) {
        try {
            func.getAsBoolean();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(UncheckedIntSupplier func) {
        try {
            func.getAsInt();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(UncheckedLongSupplier func) {
        try {
            func.getAsLong();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(UncheckedDoubleSupplier func) {
        try {
            func.getAsDouble();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Runs a function, throwing errors.
     */
    public static void runChecked(UncheckedRunnable runnable) {
        runnable.run();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static <R> R runChecked(UncheckedSupplier<R> func) {
        return func.get();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static boolean runChecked(UncheckedBooleanSupplier func) {
        return func.getAsBoolean();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static int runChecked(UncheckedIntSupplier func) {
        return func.getAsInt();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static long runChecked(UncheckedLongSupplier func) {
        return func.getAsLong();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static double runChecked(UncheckedDoubleSupplier func) {
        return func.getAsDouble();
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static <R> R runCheckedOr(UncheckedSupplier<R> func, R fallback) {
        try {
            return func.get();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static boolean runCheckedOr(UncheckedBooleanSupplier func, boolean fallback) {
        try {
            return func.getAsBoolean();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static int runCheckedOr(UncheckedIntSupplier func, int fallback) {
        try {
            return func.getAsInt();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static long runCheckedOr(UncheckedLongSupplier func, long fallback) {
        try {
            return func.getAsLong();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static double runCheckedOr(UncheckedDoubleSupplier func, double fallback) {
        try {
            return func.getAsDouble();
        } catch (Throwable e) {
            return fallback;
        }
    }
}
