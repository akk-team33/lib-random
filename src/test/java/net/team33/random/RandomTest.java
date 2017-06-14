package net.team33.random;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class RandomTest {

    private final Random random = Random.builder().build();

    @Test
    public final void nextBoolean() {
        final int[] falseCount = {0};
        final int[] trueCount = {0};
        for (int index = 0; 1000 > index; ++index) {
            final boolean result = random.nextBoolean();
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