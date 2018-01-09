package net.team33.patterns;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class RandomTest {

    private final Random random = Random.builder().build();

    @Test
    public final void next() {
        for (final Class<?> rClass : Arrays.asList(
                Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
                Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
                Character.TYPE, Character.class, String.class, Date.class)) {
            Assert.assertNotNull(random.next(rClass));
        }
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