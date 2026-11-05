package ing.prometheusreengineer.minecraft.util.java;

import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedBooleanSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedDoubleSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedIntSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedLongSupplier;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedRunnable;
import ing.prometheusreengineer.minecraft.util.java.unchecked.UncheckedSupplier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LambdaUtilTest {
    private static final IOException CAUSE = new IOException("checked");

    @Test
    void runnable() {
        List<String> ran = new ArrayList<>();
        assertTrue(LambdaUtil.runCatching((UncheckedRunnable) () -> ran.add("ran")));
        assertEquals(1, ran.size());
        assertFalse(LambdaUtil.runCatching((UncheckedRunnable) () -> { throw CAUSE; }));

        LambdaUtil.runChecked((UncheckedRunnable) () -> ran.add("ran"));
        assertEquals(2, ran.size());
        assertSame(CAUSE, assertThrows(RuntimeException.class,
                () -> LambdaUtil.runChecked((UncheckedRunnable) () -> { throw CAUSE; })).getCause());
    }

    @Test
    void supplier() {
        assertTrue(LambdaUtil.runCatching(() -> "value"));
        assertFalse(LambdaUtil.runCatching((UncheckedSupplier<String>) () -> { throw CAUSE; }));

        assertEquals("value", LambdaUtil.runChecked(() -> "value"));
        assertSame(CAUSE, assertThrows(RuntimeException.class,
                () -> LambdaUtil.runChecked((UncheckedSupplier<String>) () -> { throw CAUSE; })).getCause());

        assertEquals("value", LambdaUtil.runCheckedOr(() -> "value", "fallback"));
        assertEquals("fallback", LambdaUtil.runCheckedOr(() -> { throw CAUSE; }, "fallback"));
    }

    @Test
    void booleanSupplier() {
        UncheckedBooleanSupplier value = () -> true;
        UncheckedBooleanSupplier fails = () -> { throw CAUSE; };
        assertTrue(LambdaUtil.runCatching(value));
        assertFalse(LambdaUtil.runCatching(fails));
        assertTrue(LambdaUtil.runChecked(value));
        assertSame(CAUSE, assertThrows(RuntimeException.class, () -> LambdaUtil.runChecked(fails)).getCause());
        assertTrue(LambdaUtil.runCheckedOr(value, false));
        assertFalse(LambdaUtil.runCheckedOr(fails, false));
    }

    @Test
    void intSupplier() {
        UncheckedIntSupplier value = () -> 42;
        UncheckedIntSupplier fails = () -> { throw CAUSE; };
        assertTrue(LambdaUtil.runCatching(value));
        assertFalse(LambdaUtil.runCatching(fails));
        assertEquals(42, LambdaUtil.runChecked(value));
        assertSame(CAUSE, assertThrows(RuntimeException.class, () -> LambdaUtil.runChecked(fails)).getCause());
        assertEquals(42, LambdaUtil.runCheckedOr(value, -1));
        assertEquals(-1, LambdaUtil.runCheckedOr(fails, -1));
    }

    @Test
    void longSupplier() {
        UncheckedLongSupplier value = () -> 42L;
        UncheckedLongSupplier fails = () -> { throw CAUSE; };
        assertTrue(LambdaUtil.runCatching(value));
        assertFalse(LambdaUtil.runCatching(fails));
        assertEquals(42L, LambdaUtil.runChecked(value));
        assertSame(CAUSE, assertThrows(RuntimeException.class, () -> LambdaUtil.runChecked(fails)).getCause());
        assertEquals(42L, LambdaUtil.runCheckedOr(value, -1L));
        assertEquals(-1L, LambdaUtil.runCheckedOr(fails, -1L));
    }

    @Test
    void doubleSupplier() {
        UncheckedDoubleSupplier value = () -> 4.2;
        UncheckedDoubleSupplier fails = () -> { throw CAUSE; };
        assertTrue(LambdaUtil.runCatching(value));
        assertFalse(LambdaUtil.runCatching(fails));
        assertEquals(4.2, LambdaUtil.runChecked(value));
        assertSame(CAUSE, assertThrows(RuntimeException.class, () -> LambdaUtil.runChecked(fails)).getCause());
        assertEquals(4.2, LambdaUtil.runCheckedOr(value, -1.0));
        assertEquals(-1.0, LambdaUtil.runCheckedOr(fails, -1.0));
    }
}
