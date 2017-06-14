package net.team33.random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;

public class RandomTest {

    private static final List<Boolean> BOOLEANS = Arrays.asList(false, true);

    private Random random;

    private static void repeat(final int times, final IntConsumer consumer) {
        for (int index = 0; index < times; ++index) {
            consumer.accept(index);
        }
    }

    @Before
    public final void setUp() {
        random = Random.builder().build();
    }

    @Test
    public final void nextInteger() {
        repeat(1000, index -> {
            final int result = random.nextInteger(-10, 20);
            Assert.assertTrue("expected: -10 <= " + result, -10 <= result);
            Assert.assertTrue("expected: 20 > " + result, 20 > result);
        });
    }

    @Test
    public final void nextBoolean() {
        final int[] falseCount = {0};
        final int[] trueCount = {0};
        repeat(1000, index -> {
            final boolean result = random.nextBoolean();
            Assert.assertTrue(BOOLEANS.contains(result));
            if (result) {
                trueCount[0] += 1;
            } else {
                falseCount[0] += 1;
            }
        });
        Assert.assertEquals(1000 - trueCount[0], falseCount[0]);
        Assert.assertTrue(550 > trueCount[0]);
        Assert.assertTrue(550 > falseCount[0]);
    }
}