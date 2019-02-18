package de.team33.libs.random.v3.range;

import de.team33.libs.random.v3.BasicRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BoundsTest {

    private final BasicRandom simple = BasicRandom.simple();

    @Test
    public void projected() {
        final Bounds bounds = new Bounds(-20, 80);
        for (int i = 0; i < 10000; ++i) {
            final int anyInt = simple.anyInt();
            final int projected = bounds.projected(anyInt);
            final String message = String.format("i = %d; anyInt = %d; projected = %d", i, anyInt, projected);
            assertEquals(message, true, (-20 <= projected) && (projected < 80));
        }
    }
}