package ing.prometheusreengineer.minecraft.util.java.unchecked;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UncheckedTest {
    private static final IOException CAUSE = new IOException("checked");

    @Test
    void runnable() {
        List<String> ran = new ArrayList<>();
        UncheckedRunnable add = () -> ran.add("ran");
        add.run();
        assertEquals(1, ran.size());

        UncheckedRunnable fails = () -> { throw CAUSE; };
        assertSame(CAUSE, assertThrows(RuntimeException.class, fails::run).getCause());
    }

    @Test
    void function() {
        UncheckedFunction<String, Integer> length = String::length;
        assertEquals(3, length.apply("abc"));

        UncheckedFunction<String, Integer> fails = s -> { throw CAUSE; };
        assertSame(CAUSE, assertThrows(RuntimeException.class, () -> fails.apply("abc")).getCause());
    }

    @Test
    void supplier() {
        UncheckedSupplier<String> value = () -> "value";
        assertEquals("value", value.get());

        UncheckedSupplier<String> fails = () -> { throw CAUSE; };
        assertSame(CAUSE, assertThrows(RuntimeException.class, fails::get).getCause());
    }
}
