package net.team33.patterns;

import net.team33.patterns.test.Recursive;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class RandomTest {

    private static final Function<Random, Recursive> RANDOM_RECURSIVE =
            random -> new Recursive(random.next(Recursive[].class));

    @Test
    public final void next() throws Exception {
        final Random random = Random.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 3, null).build();
        for (final Class<?> rClass : Arrays.asList(
                // Singles ...
                Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
                Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
                Character.TYPE, Character.class, String.class, Date.class, BigInteger.class, BigDecimal.class,
                Recursive.class,
                // Arrays ...
                boolean[].class, Boolean[].class, byte[].class, Byte[].class, short[].class, Short[].class,
                int[].class, Integer[].class, long[].class, Long[].class, float[].class, Float[].class,
                double[].class, Double[].class, char[].class, Character[].class, String[].class, Date[].class,
                BigInteger[].class, BigDecimal[].class,
                Recursive[].class)) {
            final Object result = random.next(rClass);
            Assert.assertNotNull(result);
            if (rClass.isPrimitive()) {
                Assert.assertSame(rClass.getCanonicalName(), rClass, result.getClass().getField("TYPE").get(null));
            } else {
                Assert.assertTrue(rClass.getCanonicalName(), rClass.isAssignableFrom(result.getClass()));
            }
        }
    }

    @Test
    public final void recursive0Null() {
        Assert.assertNull(Random.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 0, null).build()
                .next(Recursive.class));
    }

    @Test
    public final void recursive0Empty() {
        Assert.assertSame(Recursive.EMPTY, Random.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 0, Recursive.EMPTY).build()
                .next(Recursive.class));
    }

    @Test(expected = StackOverflowError.class)
    public final void recursiveFail() {
        Assert.fail("Should fail but was " + Random.builder()
                .put(Recursive.class, RANDOM_RECURSIVE).build()
                .next(Recursive.class));
    }

    @Test
    public final void recursive() {
        final Recursive recursive = Random.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 3, null).build()
                .next(Recursive.class);
        Assert.assertNotNull(recursive);
        Assert.assertNotNull(recursive.getChildren()[0]);
        Assert.assertNotNull(recursive.getChildren()[0].getChildren()[0]);
        Assert.assertNull(recursive.getChildren()[0].getChildren()[0].getChildren()[0]);
    }

    @Test
    public final void array() {
        final Random.Bounds bounds = Random.bounds(1, 16);
        final Random subject = Random.builder().setArrayBounds(bounds).build();

        final boolean[] booleans = subject.array.ofBoolean();
        Assert.assertTrue((bounds.minLength <= booleans.length) && (booleans.length < bounds.maxLength));

        final byte[] bytes = subject.array.ofByte();
        Assert.assertTrue((bounds.minLength <= bytes.length) && (bytes.length < bounds.maxLength));

        final short[] shorts = subject.array.ofShort();
        Assert.assertTrue((bounds.minLength <= shorts.length) && (shorts.length < bounds.maxLength));

        final int[] ints = subject.array.ofInt();
        Assert.assertTrue((bounds.minLength <= ints.length) && (ints.length < bounds.maxLength));

        final long[] longs = subject.array.ofLong();
        Assert.assertTrue((bounds.minLength <= longs.length) && (longs.length < bounds.maxLength));

        final float[] floats = subject.array.ofFloat();
        Assert.assertTrue((bounds.minLength <= floats.length) && (floats.length < bounds.maxLength));

        final double[] doubles = subject.array.ofDouble();
        Assert.assertTrue((bounds.minLength <= doubles.length) && (doubles.length < bounds.maxLength));

        final char[] chars = subject.array.ofChar();
        Assert.assertTrue((bounds.minLength <= chars.length) && (chars.length < bounds.maxLength));

        final String[] strings = subject.array.of(String.class);
        Assert.assertTrue((bounds.minLength <= strings.length) && (strings.length < bounds.maxLength));
    }

    @Test
    public final void select() {
        final Random random = Random.builder().build();
        final boolean[] bools = {true, true, true};
        Assert.assertTrue(random.select.next(bools));

        final byte[] bytes = {3, 3, 3, 3};
        Assert.assertEquals(3, random.select.next(bytes));

        final short[] shorts = {278, 278, 278};
        Assert.assertEquals(278, random.select.next(shorts));

        final int[] ints = {70000, 70000, 70000};
        Assert.assertEquals(70000, random.select.next(ints));
    }
}