package ing.prometheusreengineer.minecraft.util;

import ing.prometheusreengineer.minecraft.log.Log;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Little utility to write to the system environment
 */
public final class Env {
    private static final Log log = Log.lib("Env");

    private static final Unsafe unsafe;

    static {
        Unsafe u;
        try {
            u = unsafe();
        } catch (Exception e) {
            log.error("failed to get Unsafe", e);
            u = null;
        }
        unsafe = u;
    }

    private Env() {}

    /**
     * Set a variable for {@link System#getenv()}
     *
     * @throws IllegalStateException if the write failed
     */
    public static void setenv(String name, String value) {
        if (name == null || value == null) throw new IllegalArgumentException("name and value are required");
        String original = System.getenv(name);

        Throwable primary = null;
        try {
            inPlace(name, value);
        } catch (Throwable t) {
            primary = t;
            try {
                swapField(name, value);
            } catch (Throwable fallback) {
                throw new IllegalStateException("setenv failed: in-place=" + t + " swap=" + fallback, t);
            }
        }

        try {
            caseInsensitive(name, value);
        } catch (Throwable t) {
            if (primary == null) primary = t;
        }

        String now = System.getenv(name);
        if (!value.equals(now)) {
            throw new IllegalStateException("setenv did not set env " + name + "=" + now +
                    (original == null || original.isEmpty() ? "" :  "(was: "+original+")") +
                    (primary == null ? "" : " (in-place threw " + primary + ")"));
        }
    }

    /** Modify an UnmodifiableMap */
    @SuppressWarnings("unchecked")
    private static void inPlace(String name, String value) throws Exception {
        if (unsafe == null) throw new IllegalStateException("cannot setenv via inPlace: Unsafe unavailable");
        Map<String, String> ro = System.getenv();
        Field m = Class.forName("java.util.Collections$UnmodifiableMap").getDeclaredField("m");
        Map<String, String> backing = (Map<String, String>) unsafe.getObject(ro, unsafe.objectFieldOffset(m));
        // invokeinterface Map.put -- the impl (ProcessEnvironment$StringEnvironment on unix,
        // ProcessEnvironment itself on Windows) stays inaccessible, which is fine.
        backing.put(name, value);
    }

    /**
     * Windows only: System.getenv(String) reads ProcessEnvironment.theCaseInsensitiveEnvironment,
     * a separate TreeMap copy, not the map behind System.getenv(). No-op elsewhere.
     */
    @SuppressWarnings("unchecked")
    private static void caseInsensitive(String name, String value) throws Exception {
        Field f;
        try {
            f = Class.forName("java.lang.ProcessEnvironment").getDeclaredField("theCaseInsensitiveEnvironment");
        } catch (NoSuchFieldException e) {
            return;
        }
        Unsafe u = unsafe != null ? unsafe : unsafe();
        Map<String, String> ci = (Map<String, String>) u.getObject(u.staticFieldBase(f), u.staticFieldOffset(f));
        ci.put(name, value);
    }

    /** Fallback: replace ProcessEnvironment.theUnmodifiableEnvironment entirely. */
    @SuppressWarnings("unchecked")
    private static void swapField(String name, String value) throws Exception {
        Unsafe u = unsafe();
        Field f = Class.forName("java.lang.ProcessEnvironment").getDeclaredField("theUnmodifiableEnvironment");
        Object base = u.staticFieldBase(f);
        long off = u.staticFieldOffset(f);
        Map<String, String> copy = new HashMap<>((Map<String, String>) u.getObject(base, off));
        copy.put(name, value);
        u.putObject(base, off, Collections.unmodifiableMap(copy));
    }

    @SuppressWarnings("unchecked")
    private static Unsafe unsafe() throws Exception {
        try {
            // 1: .getUnsafe()
            return Unsafe.getUnsafe();
        } catch (SecurityException t) {
            try {
                // 2: .theUnsafe
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                return (Unsafe) f.get(null);
            } catch (Exception e) {
                // 3: System class loader .getUnsafe()
                Class<Unsafe> c = (Class<Unsafe>) ClassLoader.getSystemClassLoader().loadClass("sun.misc.Unsafe");
                try {
                    return (Unsafe) c.getDeclaredMethod("getUnsafe").invoke(null);
                } catch (Exception ex) {
                    // 4: System class loader .theUnsafe
                    return (Unsafe) c.getDeclaredField("theUnsafe").get(null);
                }
            }
        }
    }
}
