package de.team33.test.random.v4;

import static org.junit.Assert.assertEquals;

import java.util.function.Supplier;

import de.team33.libs.random.v4.SmartRandom;
import org.junit.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SmartRandomTest {

    private static final Supplier<SmartRandom> RANDOM = SmartRandom.builder()
                                                                   .set(Boolean.class, rnd -> Boolean.TRUE)
                                                                   .set(Byte.class, rnd -> (byte) 27)
                                                                   .prepare();

    @Test
    public final void anyBoolean() {
        final SmartRandom random = RANDOM.get();
        assertEquals(Boolean.TRUE, random.get(boolean.class));
        assertEquals(Boolean.TRUE, random.get(Boolean.class));
    }
}
