package ing.prometheusreengineer.minecraft.util.java;

import ing.prometheusreengineer.minecraft.util.java.unchecked.*;
import org.jetbrains.annotations.NotNull;

public final class LambdaUtil {
    private LambdaUtil() {}

    /**
     * Returns true if the function returned successfully.
     */
    public static boolean runCatching(@NotNull UncheckedRunnable runnable) {
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
    public static <T> boolean runCatching(@NotNull UncheckedSupplier<T> func) {
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
    public static boolean runCatching(@NotNull UncheckedBooleanSupplier func) {
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
    public static boolean runCatching(@NotNull UncheckedIntSupplier func) {
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
    public static boolean runCatching(@NotNull UncheckedLongSupplier func) {
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
    public static boolean runCatching(@NotNull UncheckedDoubleSupplier func) {
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
    public static void runChecked(@NotNull UncheckedRunnable runnable) {
        runnable.run();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static <R> R runChecked(@NotNull UncheckedSupplier<R> func) {
        return func.get();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static boolean runChecked(@NotNull UncheckedBooleanSupplier func) {
        return func.getAsBoolean();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static int runChecked(@NotNull UncheckedIntSupplier func) {
        return func.getAsInt();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static long runChecked(@NotNull UncheckedLongSupplier func) {
        return func.getAsLong();
    }

    /**
     * Runs a function, throwing errors.
     */
    public static double runChecked(@NotNull UncheckedDoubleSupplier func) {
        return func.getAsDouble();
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static <R> R runCheckedOr(@NotNull UncheckedSupplier<R> func, R fallback) {
        try {
            return func.get();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static boolean runCheckedOr(@NotNull UncheckedBooleanSupplier func, boolean fallback) {
        try {
            return func.getAsBoolean();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static int runCheckedOr(@NotNull UncheckedIntSupplier func, int fallback) {
        try {
            return func.getAsInt();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static long runCheckedOr(@NotNull UncheckedLongSupplier func, long fallback) {
        try {
            return func.getAsLong();
        } catch (Throwable e) {
            return fallback;
        }
    }

    /**
     * Runs a function, returning the fallback on error.
     */
    public static double runCheckedOr(@NotNull UncheckedDoubleSupplier func, double fallback) {
        try {
            return func.getAsDouble();
        } catch (Throwable e) {
            return fallback;
        }
    }
}
