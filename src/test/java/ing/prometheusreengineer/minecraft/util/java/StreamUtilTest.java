package ing.prometheusreengineer.minecraft.util.java;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamUtilTest {
    private static StreamUtil<String> abc() {
        return StreamUtil.from("b", "a", "c");
    }

    @Test
    void creates() {
        assertEquals(Arrays.asList("b", "a", "c"), StreamUtil.of(new String[]{"b", "a", "c"}).toUnmodifiableList());
        assertEquals(Arrays.asList("b", "a", "c"), StreamUtil.of(Arrays.asList("b", "a", "c")).toUnmodifiableList());
        assertEquals(Arrays.asList("b", "a", "c"), StreamUtil.wrap(Stream.of("b", "a", "c")).toUnmodifiableList());
    }

    @Test
    void extras() {
        assertEquals(Arrays.asList("a", "c"), StreamUtil.from("a", null, "c").filterNotNull().toUnmodifiableList());
        assertTrue(StreamUtil.from("a", null).containsNull());
        assertFalse(abc().containsNull());
        assertThrows(UnsupportedOperationException.class, () -> abc().toUnmodifiableList().add("d"));
    }

    @Test
    void intermediate() {
        assertEquals(Arrays.asList("a"), abc().filter("a"::equals).toUnmodifiableList());
        assertEquals(Arrays.asList("B", "A", "C"), abc().map(String::toUpperCase).toUnmodifiableList());
        assertEquals(Arrays.asList("b", "b", "a", "a", "c", "c"), abc().flatMap(s -> Stream.of(s, s)).collect(Collectors.toList()));
        assertEquals(Arrays.asList("a", "b"), StreamUtil.from("a", "b", "a").distinct().toUnmodifiableList());
        assertEquals(Arrays.asList("a", "b", "c"), abc().sorted().toUnmodifiableList());
        assertEquals(Arrays.asList("c", "b", "a"), abc().sorted(Comparator.reverseOrder()).toUnmodifiableList());
        assertEquals(Arrays.asList("b"), abc().limit(1).toUnmodifiableList());
        assertEquals(Arrays.asList("c"), abc().skip(2).toUnmodifiableList());

        List<String> peeked = new ArrayList<>();
        abc().peek(peeked::add).toUnmodifiableList();
        assertEquals(Arrays.asList("b", "a", "c"), peeked);
    }

    @Test
    void primitives() {
        assertEquals(3, abc().mapToInt(String::length).sum());
        assertEquals(3L, abc().mapToLong(String::length).sum());
        assertEquals(3.0, abc().mapToDouble(String::length).sum());
        assertEquals(6, abc().flatMapToInt(s -> IntStream.of(1, 1)).sum());
        assertEquals(6L, abc().flatMapToLong(s -> LongStream.of(1, 1)).sum());
        assertEquals(6.0, abc().flatMapToDouble(s -> DoubleStream.of(1, 1)).sum());
    }

    @Test
    void forEach() {
        List<String> seen = new ArrayList<>();
        abc().forEach(seen::add);
        abc().forEachOrdered(seen::add);
        assertEquals(Arrays.asList("b", "a", "c", "b", "a", "c"), seen);
    }

    @Test
    void arrays() {
        assertArrayEquals(new Object[]{"b", "a", "c"}, abc().toArray());
        assertArrayEquals(new String[]{"b", "a", "c"}, abc().toArray(String[]::new));
    }

    @Test
    void reduces() {
        assertEquals("_bac", abc().reduce("_", String::concat));
        assertEquals(Optional.of("bac"), abc().reduce(String::concat));
        assertEquals(3, (int) abc().reduce(0, (n, s) -> n + s.length(), Integer::sum));
        assertEquals("bac", abc().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString());
        assertEquals("b,a,c", abc().collect(Collectors.joining(",")));
    }

    @Test
    void terminal() {
        assertEquals(Optional.of("a"), abc().min(Comparator.naturalOrder()));
        assertEquals(Optional.of("c"), abc().max(Comparator.naturalOrder()));
        assertEquals(3, abc().count());
        assertTrue(abc().anyMatch("a"::equals));
        assertTrue(abc().allMatch(s -> s.length() == 1));
        assertTrue(abc().noneMatch("d"::equals));
        assertEquals(Optional.of("b"), abc().findFirst());
        assertTrue(abc().findAny().isPresent());
    }

    @Test
    void iterates() {
        Iterator<String> it = abc().iterator();
        assertEquals("b", it.next());
        List<String> rest = new ArrayList<>();
        abc().spliterator().forEachRemaining(rest::add);
        assertEquals(Arrays.asList("b", "a", "c"), rest);
    }

    @Test
    void modes() {
        assertTrue(abc().parallel().isParallel());
        assertFalse(abc().parallel().sequential().isParallel());
        assertEquals(3, abc().unordered().count());
    }

    @Test
    void closes() {
        List<String> closed = new ArrayList<>();
        StreamUtil<String> stream = abc().onClose(() -> closed.add("closed"));
        stream.close();
        assertEquals(Arrays.asList("closed"), closed);
    }
}
