package net.team33.patterns;

import net.team33.patterns.test.Recursive;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;

public class SmartRandomTest {

    private static final Function<SmartRandom, Recursive> RANDOM_RECURSIVE =
            random -> new Recursive(random.any(Recursive[].class));

    @Test
    public final void any() throws Exception {
        final SmartRandom random = SmartRandom.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 3, null)
                .build();
        for (final Class<?> rClass : Arrays.asList(
                // Singles ...
                Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
                Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
                Character.TYPE, Character.class, Number.class, Object.class/*,
                String.class, Date.class, BigInteger.class, BigDecimal.class, Recursive.class,
                // Arrays ...
                boolean[].class, Boolean[].class, byte[].class, Byte[].class, short[].class, Short[].class,
                int[].class, Integer[].class, long[].class, Long[].class, float[].class, Float[].class,
                double[].class, Double[].class, char[].class, Character[].class,
                String[].class, Date[].class, BigInteger[].class, BigDecimal[].class, Recursive[].class*/)) {
            final Object result = random.any(rClass);
            Assert.assertNotNull(result);
            if (rClass.isPrimitive()) {
                Assert.assertSame(rClass.getCanonicalName(), rClass, result.getClass().getField("TYPE").get(null));
            } else {
                Assert.assertTrue(rClass.getCanonicalName(), rClass.isAssignableFrom(result.getClass()));
            }
        }
    }
}