package net.team33.patterns;

import net.team33.patterns.test.Recursive;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class RandomTest {

    private static final Supplier<Random> RANDOM = Random.builder()
            .put(Recursive.class, rnd -> new Recursive(rnd.next(Recursive.class)))
            .setMaxDepth(3)
            .build();

    private final Random random = RANDOM.get();

    @Test
    public final void next() {
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
                Recursive[].class)) {
            Assert.assertNotNull(random.next(rClass));
        }
    }

    @Test
    public final void recursive() {
        final Recursive recursive1 = random.next(Recursive.class);
        Assert.assertNotNull(recursive1);
        Assert.assertNotNull(recursive1.getChild());
        Assert.assertNotNull(recursive1.getChild().getChild());
        Assert.assertNull(recursive1.getChild().getChild().getChild());
    }

    @Test
    public final void array() {
        final Random.Bounds bounds = Random.bounds(1, 16);
        final Random subject = Random.builder().setArrayBounds(bounds).build().get();

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
        final boolean[] bools = {true, true, true};
        Assert.assertTrue(random.select.next(bools));

        final byte[] bytes = {3, 3, 3, 3};
        Assert.assertEquals(3, random.select.next(bytes));

        final short[] shorts = {278, 278, 278};
        Assert.assertEquals(278, random.select.next(shorts));

        final int[] ints = {70000, 70000, 70000};
        Assert.assertEquals(70000, random.select.next(ints));
    }

    @Test
    public final void nextBoolean() {
        for (final Class<Boolean> boolClass : Arrays.asList(Boolean.TYPE, Boolean.class)) {
            final int[] falseCount = {0};
            final int[] trueCount = {0};
            for (int index = 0; 1000 > index; ++index) {
                final boolean result = random.next(boolClass);
                if (result) {
                    trueCount[0] += 1;
                } else {
                    falseCount[0] += 1;
                }
            }
            Assert.assertEquals(1000, falseCount[0] + trueCount[0]);
            Assert.assertTrue(550 > trueCount[0]);
            Assert.assertTrue(550 > falseCount[0]);
        }
    }

    @Test
    public final void nextInteger() {
        for (final Class<Integer> intClass : Arrays.asList(Integer.TYPE, Integer.class)) {
            final int[] negativeCount = {0};
            final int[] positiveCount = {0};
            final int[] zeroCount = {0};
            for (int index = 0; 1000 > index; ++index) {
                final int result = random.next(intClass);
                if (0 > result) {
                    negativeCount[0] += 1;
                } else if (0 < result) {
                    positiveCount[0] += 1;
                } else {
                    zeroCount[0] += 1;
                }
            }
            Assert.assertEquals(1000, negativeCount[0] + positiveCount[0] + zeroCount[0]);
            Assert.assertTrue(550 > positiveCount[0]);
            Assert.assertTrue(550 > negativeCount[0]);
        }
    }
}