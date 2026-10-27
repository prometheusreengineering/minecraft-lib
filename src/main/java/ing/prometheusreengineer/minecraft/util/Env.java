package ing.prometheusreengineer.minecraft.util;

import ing.prometheusreengineer.minecraft.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Little utility to write to the system environment
 */
public final class Env {
    private static final Log log = Log.lib("Env");

    /**
     * Null if it couldn't be read, then every write fails with an {@link IllegalStateException}.
     */
    private static final @Nullable Unsafe unsafe = load(Env::theUnsafe);

    private Env() {}

    /**
     * Set a variable for {@link System#getenv()}
     *
     * @throws IllegalStateException if the write failed
     */
    public static void setenv(String name, String value) {
        setenv(name, value, IN_PLACE, SWAP, CASE_INSENSITIVE);
    }

    /**
     * One way to put a variable in one of the JDK's environment maps.
     */
    interface Write {
        void write(@NotNull String name, @NotNull String value) throws Exception;
    }

    static final Write IN_PLACE = (name, value) -> inPlace(unsafe, name, value);
    static final Write SWAP = (name, value) -> swapField(unsafe, name, value);
    static final Write CASE_INSENSITIVE = (name, value) ->
            caseInsensitive(unsafe, Class.forName("java.lang.ProcessEnvironment"), "theCaseInsensitiveEnvironment", name, value);

    /**
     * {@link #setenv(String, String)} with its three writes passed in, so tests can make each one fail.
     */
    static void setenv(String name, String value, @NotNull Write inPlace, @NotNull Write swap, @NotNull Write caseInsensitive) {
        if (name == null || value == null) throw new IllegalArgumentException("name and value are required");
        String original = System.getenv(name);

        Throwable primary = null;
        try {
            inPlace.write(name, value);
        } catch (Throwable t) {
            primary = t;
            try {
                swap.write(name, value);
            } catch (Throwable fallback) {
                throw new IllegalStateException("setenv failed: in-place=" + t + " swap=" + fallback, t);
            }
        }

        try {
            caseInsensitive.write(name, value);
        } catch (Throwable t) {
            if (primary == null) primary = t;
        }

        String now = System.getenv(name);
        if (!value.equals(now)) {
            throw new IllegalStateException("setenv did not set env " + name + "=" + now +
                    (original == null || original.isEmpty() ? "" : "(was: " + original + ")") +
                    (primary == null ? "" : " (in-place threw " + primary + ")"));
        }
    }

    /**
     * Modify an UnmodifiableMap
     */
    @SuppressWarnings("unchecked")
    static void inPlace(@Nullable Unsafe unsafe, @NotNull String name, @NotNull String value) throws Exception {
        Unsafe u = require(unsafe);
        Map<String, String> ro = System.getenv();
        Field m = Class.forName("java.util.Collections$UnmodifiableMap").getDeclaredField("m");
        Map<String, String> backing = (Map<String, String>) u.getObject(ro, u.objectFieldOffset(m));
        // invokeinterface Map.put -- the impl (ProcessEnvironment$StringEnvironment on unix,
        // ProcessEnvironment itself on Windows) stays inaccessible, which is fine.
        backing.put(name, value);
    }

    /**
     * Windows only: System.getenv(String) reads ProcessEnvironment.theCaseInsensitiveEnvironment,
     * a separate TreeMap copy, not the map behind System.getenv(). No-op where {@code field} doesn't exist.
     */
    @SuppressWarnings("unchecked")
    static void caseInsensitive(@Nullable Unsafe unsafe, @NotNull Class<?> owner, @NotNull String field, @NotNull String name, @NotNull String value) {
        Field f;
        try {
            f = owner.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            return;
        }
        Unsafe u = require(unsafe);
        Map<String, String> ci = (Map<String, String>) u.getObject(u.staticFieldBase(f), u.staticFieldOffset(f));
        ci.put(name, value);
    }

    /**
     * Fallback: replace ProcessEnvironment.theUnmodifiableEnvironment entirely.
     */
    @SuppressWarnings("unchecked")
    static void swapField(@Nullable Unsafe unsafe, @NotNull String name, @NotNull String value) throws Exception {
        Unsafe u = require(unsafe);
        Field f = Class.forName("java.lang.ProcessEnvironment").getDeclaredField("theUnmodifiableEnvironment");
        Object base = u.staticFieldBase(f);
        long off = u.staticFieldOffset(f);
        Map<String, String> copy = new HashMap<>((Map<String, String>) u.getObject(base, off));
        copy.put(name, value);
        u.putObject(base, off, Collections.unmodifiableMap(copy));
    }

    /**
     * Null and one error line instead of an exception, so a JVM without Unsafe doesn't break loading this class.
     */
    static @Nullable Unsafe load(@NotNull Callable<Unsafe> source) {
        try {
            return source.call();
        } catch (Exception e) {
            log.error("failed to get Unsafe", e);
            return null;
        }
    }

    /**
     * The one way that works for a class outside the JDK, from Java 8 to 25: {@code Unsafe.getUnsafe()} throws
     * SecurityException, and reflection hides {@code getUnsafe} and needs {@code setAccessible} for the field.
     */
    static @NotNull Unsafe theUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    private static @NotNull Unsafe require(@Nullable Unsafe unsafe) {
        if (unsafe == null) throw new IllegalStateException("Unsafe unavailable");
        return unsafe;
    }
}
