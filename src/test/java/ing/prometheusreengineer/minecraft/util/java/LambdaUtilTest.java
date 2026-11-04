package ing.prometheusreengineer.minecraft.util.java;

import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedFunction;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedRunnable;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaUtilTest {
    @Test
    void runnable() {
        List<String> ran = new ArrayList<>();
        assertTrue(LambdaUtil.runCatching((UncheckedRunnable) () -> ran.add("ran")));
        assertEquals(1, ran.size());
        assertFalse(LambdaUtil.runCatching((UncheckedRunnable) () -> { throw new IOException("checked"); }));
    }

    @Test
    void function() {
        List<String> got = new ArrayList<>();
        UncheckedFunction<String, Void> add = s -> { got.add(s); return null; };
        assertTrue(LambdaUtil.runCatching(add, "a"));
        assertEquals(1, got.size());
        UncheckedFunction<String, Void> fails = s -> { throw new IOException(s); };
        assertFalse(LambdaUtil.runCatching(fails, "b"));
    }

    @Test
    void supplier() {
        assertTrue(LambdaUtil.runCatching(() -> "value"));
        assertFalse(LambdaUtil.runCatching((UncheckedSupplier<String>) () -> { throw new IOException("checked"); }));
    }

    @Test
    void functionOrFallback() {
        UncheckedFunction<String, Integer> parse = Integer::parseInt;
        assertEquals(42, LambdaUtil.runCatchingOr(parse, "42", -1));
        assertEquals(-1, LambdaUtil.runCatchingOr(parse, "nope", -1));
    }

    @Test
    void supplierOrFallback() {
        assertEquals("value", LambdaUtil.runCatchingOr(() -> "value", "fallback"));
        assertEquals("fallback", LambdaUtil.runCatchingOr(() -> { throw new IOException("checked"); }, "fallback"));
    }
}
