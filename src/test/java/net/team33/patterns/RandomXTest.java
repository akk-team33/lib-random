package net.team33.patterns;

import com.google.common.collect.ImmutableMap;
import net.team33.patterns.test.Recursive;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RandomXTest {

    private static final Map<Class<?>, Class<?>> CLASS_MAP = ImmutableMap.<Class<?>, Class<?>>builder()
            .put(Boolean.TYPE, Boolean.class)
            .put(Byte.TYPE, Byte.class)
            .put(Short.TYPE, Short.class)
            .put(Integer.TYPE, Integer.class)
            .put(Long.TYPE, Long.class)
            .put(Float.TYPE, Float.class)
            .put(Double.TYPE, Double.class)
            .put(Character.TYPE, Character.class)
            .build();

    private final RandomX random = RandomX.builder()
            .put(Recursive.class, rnd -> new Recursive(rnd.next(Recursive[].class)), 3)
            .build();

    @Test
    public final void next() {
        final RandomX.Generator subject = random.generator();
        for (final Class<?> rClass : Arrays.asList(
                // Singles ...
                Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
                Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
                Character.TYPE, Character.class, String.class, Date.class,
                BigInteger.class, BigDecimal.class,
                Recursive.class,
                // Arrays ...
                boolean[].class, Boolean[].class, byte[].class, Byte[].class, short[].class, Short[].class,
                int[].class, Integer[].class, long[].class, Long[].class, float[].class, Float[].class,
                double[].class, Double[].class, char[].class, Character[].class, String[].class, Date[].class,
                BigInteger[].class, BigDecimal[].class,
                Recursive[].class,
                // Arrays of arrays ...
                boolean[][].class, Boolean[][].class, byte[][].class, Byte[][].class, short[][].class, Short[][].class,
                int[][].class, Integer[][].class, long[][].class, Long[][].class, float[][].class, Float[][].class,
                double[][].class, Double[][].class, char[][].class, Character[][].class, String[][].class,
                Date[][].class, BigInteger[][].class, BigDecimal[][].class,
                Recursive[][].class)) {

            final Object result = subject.next(rClass);
            assertNotNull(result);
            final Class<?> expected = Optional.<Class<?>>ofNullable(CLASS_MAP.get(rClass)).orElse(rClass);
            assertTrue(String.format(
                    "expected <%s> but was <%s>",
                    expected, result.getClass()), expected.isInstance(result)
            );
        }
    }

    @Test
    public final void recursive() {
        final Recursive recursive1 = random.generator().next(Recursive.class);
        assertNotNull(recursive1);
        assertNotNull(recursive1.getChildren()[0]);
        assertNotNull(recursive1.getChildren()[0].getChildren()[0]);
        assertNull(recursive1.getChildren()[0].getChildren()[0].getChildren()[0]);
    }
}