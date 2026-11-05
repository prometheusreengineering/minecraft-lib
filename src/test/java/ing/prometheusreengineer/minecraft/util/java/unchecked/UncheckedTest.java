package ing.prometheusreengineer.minecraft.util.java.unchecked;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UncheckedTest {
    private static final IOException CAUSE = new IOException("checked");

    private static void assertWraps(Executable call) {
        assertSame(CAUSE, assertThrows(RuntimeException.class, call).getCause());
    }

    @Test
    void runnable() {
        List<String> ran = new ArrayList<>();
        UncheckedRunnable add = () -> ran.add("ran");
        add.run();
        assertEquals(1, ran.size());

        UncheckedRunnable fails = () -> { throw CAUSE; };
        assertWraps(fails::run);
    }

    @Test
    void suppliers() {
        UncheckedSupplier<String> value = () -> "value";
        assertEquals("value", value.get());
        UncheckedSupplier<String> fails = () -> { throw CAUSE; };
        assertWraps(fails::get);

        UncheckedBooleanSupplier bool = () -> true;
        assertTrue(bool.getAsBoolean());
        UncheckedBooleanSupplier boolFails = () -> { throw CAUSE; };
        assertWraps(boolFails::getAsBoolean);

        UncheckedIntSupplier i = () -> 1;
        assertEquals(1, i.getAsInt());
        UncheckedIntSupplier iFails = () -> { throw CAUSE; };
        assertWraps(iFails::getAsInt);

        UncheckedLongSupplier l = () -> 1L;
        assertEquals(1L, l.getAsLong());
        UncheckedLongSupplier lFails = () -> { throw CAUSE; };
        assertWraps(lFails::getAsLong);

        UncheckedDoubleSupplier d = () -> 1.5;
        assertEquals(1.5, d.getAsDouble());
        UncheckedDoubleSupplier dFails = () -> { throw CAUSE; };
        assertWraps(dFails::getAsDouble);
    }

    @Test
    void consumers() {
        List<Object> got = new ArrayList<>();

        UncheckedConsumer<String> c = got::add;
        c.accept("a");
        UncheckedConsumer<String> cFails = t -> { throw CAUSE; };
        assertWraps(() -> cFails.accept("a"));

        UncheckedBiConsumer<String, Integer> bi = (t, u) -> got.add(t + u);
        bi.accept("b", 1);
        UncheckedBiConsumer<String, Integer> biFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> biFails.accept("b", 1));

        UncheckedIntConsumer i = got::add;
        i.accept(2);
        UncheckedIntConsumer iFails = v -> { throw CAUSE; };
        assertWraps(() -> iFails.accept(2));

        UncheckedLongConsumer l = got::add;
        l.accept(3L);
        UncheckedLongConsumer lFails = v -> { throw CAUSE; };
        assertWraps(() -> lFails.accept(3L));

        UncheckedDoubleConsumer d = got::add;
        d.accept(4.5);
        UncheckedDoubleConsumer dFails = v -> { throw CAUSE; };
        assertWraps(() -> dFails.accept(4.5));

        UncheckedObjIntConsumer<String> oi = (t, v) -> got.add(t + v);
        oi.accept("i", 5);
        UncheckedObjIntConsumer<String> oiFails = (t, v) -> { throw CAUSE; };
        assertWraps(() -> oiFails.accept("i", 5));

        UncheckedObjLongConsumer<String> ol = (t, v) -> got.add(t + v);
        ol.accept("l", 6L);
        UncheckedObjLongConsumer<String> olFails = (t, v) -> { throw CAUSE; };
        assertWraps(() -> olFails.accept("l", 6L));

        UncheckedObjDoubleConsumer<String> od = (t, v) -> got.add(t + v);
        od.accept("d", 7.5);
        UncheckedObjDoubleConsumer<String> odFails = (t, v) -> { throw CAUSE; };
        assertWraps(() -> odFails.accept("d", 7.5));

        assertEquals(java.util.Arrays.<Object>asList("a", "b1", 2, 3L, 4.5, "i5", "l6", "d7.5"), got);
    }

    @Test
    void predicates() {
        UncheckedPredicate<String> p = String::isEmpty;
        assertTrue(p.test(""));
        UncheckedPredicate<String> pFails = t -> { throw CAUSE; };
        assertWraps(() -> pFails.test(""));

        UncheckedBiPredicate<String, String> bi = String::equals;
        assertTrue(bi.test("a", "a"));
        UncheckedBiPredicate<String, String> biFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> biFails.test("a", "a"));

        UncheckedIntPredicate i = v -> v > 0;
        assertTrue(i.test(1));
        UncheckedIntPredicate iFails = v -> { throw CAUSE; };
        assertWraps(() -> iFails.test(1));

        UncheckedLongPredicate l = v -> v > 0;
        assertFalse(l.test(-1L));
        UncheckedLongPredicate lFails = v -> { throw CAUSE; };
        assertWraps(() -> lFails.test(1L));

        UncheckedDoublePredicate d = v -> v > 0;
        assertTrue(d.test(0.5));
        UncheckedDoublePredicate dFails = v -> { throw CAUSE; };
        assertWraps(() -> dFails.test(0.5));
    }

    @Test
    void functions() {
        UncheckedFunction<String, Integer> length = String::length;
        assertEquals(3, length.apply("abc"));
        UncheckedFunction<String, Integer> fails = s -> { throw CAUSE; };
        assertWraps(() -> fails.apply("abc"));

        UncheckedBiFunction<String, Integer, String> repeat = (s, n) -> n + s;
        assertEquals("2a", repeat.apply("a", 2));
        UncheckedBiFunction<String, Integer, String> biFails = (s, n) -> { throw CAUSE; };
        assertWraps(() -> biFails.apply("a", 2));

        UncheckedIntFunction<String> i = Integer::toString;
        assertEquals("1", i.apply(1));
        UncheckedIntFunction<String> iFails = v -> { throw CAUSE; };
        assertWraps(() -> iFails.apply(1));

        UncheckedLongFunction<String> l = Long::toString;
        assertEquals("2", l.apply(2L));
        UncheckedLongFunction<String> lFails = v -> { throw CAUSE; };
        assertWraps(() -> lFails.apply(2L));

        UncheckedDoubleFunction<String> d = Double::toString;
        assertEquals("2.5", d.apply(2.5));
        UncheckedDoubleFunction<String> dFails = v -> { throw CAUSE; };
        assertWraps(() -> dFails.apply(2.5));
    }

    @Test
    void toPrimitiveFunctions() {
        UncheckedToIntFunction<String> ti = String::length;
        assertEquals(2, ti.applyAsInt("ab"));
        UncheckedToIntFunction<String> tiFails = t -> { throw CAUSE; };
        assertWraps(() -> tiFails.applyAsInt("ab"));

        UncheckedToLongFunction<String> tl = String::length;
        assertEquals(2L, tl.applyAsLong("ab"));
        UncheckedToLongFunction<String> tlFails = t -> { throw CAUSE; };
        assertWraps(() -> tlFails.applyAsLong("ab"));

        UncheckedToDoubleFunction<String> td = String::length;
        assertEquals(2.0, td.applyAsDouble("ab"));
        UncheckedToDoubleFunction<String> tdFails = t -> { throw CAUSE; };
        assertWraps(() -> tdFails.applyAsDouble("ab"));

        UncheckedToIntBiFunction<String, String> tib = (t, u) -> t.length() + u.length();
        assertEquals(3, tib.applyAsInt("a", "bc"));
        UncheckedToIntBiFunction<String, String> tibFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> tibFails.applyAsInt("a", "bc"));

        UncheckedToLongBiFunction<String, String> tlb = (t, u) -> t.length() + u.length();
        assertEquals(3L, tlb.applyAsLong("a", "bc"));
        UncheckedToLongBiFunction<String, String> tlbFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> tlbFails.applyAsLong("a", "bc"));

        UncheckedToDoubleBiFunction<String, String> tdb = (t, u) -> t.length() + u.length();
        assertEquals(3.0, tdb.applyAsDouble("a", "bc"));
        UncheckedToDoubleBiFunction<String, String> tdbFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> tdbFails.applyAsDouble("a", "bc"));
    }

    @Test
    void primitiveConversions() {
        UncheckedIntToLongFunction il = v -> v * 2L;
        assertEquals(4L, il.applyAsLong(2));
        UncheckedIntToLongFunction ilFails = v -> { throw CAUSE; };
        assertWraps(() -> ilFails.applyAsLong(2));

        UncheckedIntToDoubleFunction id = v -> v / 2.0;
        assertEquals(1.5, id.applyAsDouble(3));
        UncheckedIntToDoubleFunction idFails = v -> { throw CAUSE; };
        assertWraps(() -> idFails.applyAsDouble(3));

        UncheckedLongToIntFunction li = v -> (int) v;
        assertEquals(5, li.applyAsInt(5L));
        UncheckedLongToIntFunction liFails = v -> { throw CAUSE; };
        assertWraps(() -> liFails.applyAsInt(5L));

        UncheckedLongToDoubleFunction ld = v -> v / 2.0;
        assertEquals(2.5, ld.applyAsDouble(5L));
        UncheckedLongToDoubleFunction ldFails = v -> { throw CAUSE; };
        assertWraps(() -> ldFails.applyAsDouble(5L));

        UncheckedDoubleToIntFunction di = v -> (int) v;
        assertEquals(2, di.applyAsInt(2.7));
        UncheckedDoubleToIntFunction diFails = v -> { throw CAUSE; };
        assertWraps(() -> diFails.applyAsInt(2.7));

        UncheckedDoubleToLongFunction dl = Math::round;
        assertEquals(3L, dl.applyAsLong(2.7));
        UncheckedDoubleToLongFunction dlFails = v -> { throw CAUSE; };
        assertWraps(() -> dlFails.applyAsLong(2.7));
    }

    @Test
    void operators() {
        UncheckedUnaryOperator<String> upper = String::toUpperCase;
        assertEquals("A", upper.apply("a"));
        UncheckedUnaryOperator<String> uFails = t -> { throw CAUSE; };
        assertWraps(() -> uFails.apply("a"));

        UncheckedBinaryOperator<String> concat = String::concat;
        assertEquals("ab", concat.apply("a", "b"));
        UncheckedBinaryOperator<String> bFails = (t, u) -> { throw CAUSE; };
        assertWraps(() -> bFails.apply("a", "b"));

        UncheckedIntUnaryOperator iu = v -> -v;
        assertEquals(-1, iu.applyAsInt(1));
        UncheckedIntUnaryOperator iuFails = v -> { throw CAUSE; };
        assertWraps(() -> iuFails.applyAsInt(1));

        UncheckedIntBinaryOperator ib = Integer::sum;
        assertEquals(3, ib.applyAsInt(1, 2));
        UncheckedIntBinaryOperator ibFails = (a, b) -> { throw CAUSE; };
        assertWraps(() -> ibFails.applyAsInt(1, 2));

        UncheckedLongUnaryOperator lu = v -> -v;
        assertEquals(-1L, lu.applyAsLong(1L));
        UncheckedLongUnaryOperator luFails = v -> { throw CAUSE; };
        assertWraps(() -> luFails.applyAsLong(1L));

        UncheckedLongBinaryOperator lb = Long::sum;
        assertEquals(3L, lb.applyAsLong(1L, 2L));
        UncheckedLongBinaryOperator lbFails = (a, b) -> { throw CAUSE; };
        assertWraps(() -> lbFails.applyAsLong(1L, 2L));

        UncheckedDoubleUnaryOperator du = v -> -v;
        assertEquals(-1.5, du.applyAsDouble(1.5));
        UncheckedDoubleUnaryOperator duFails = v -> { throw CAUSE; };
        assertWraps(() -> duFails.applyAsDouble(1.5));

        UncheckedDoubleBinaryOperator db = Double::sum;
        assertEquals(3.5, db.applyAsDouble(1.5, 2.0));
        UncheckedDoubleBinaryOperator dbFails = (a, b) -> { throw CAUSE; };
        assertWraps(() -> dbFails.applyAsDouble(1.5, 2.0));
    }
}
