package de.team33.test.random.v4;

import static java.lang.Boolean.TRUE;

import static org.junit.Assert.assertEquals;

import java.util.function.Supplier;

import de.team33.libs.random.v4.SmartRandom;
import org.junit.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SmartRandomTest {

    private static final Byte BYTE27 = (byte) 27;
    private static final Long LONG278 = (long) 278;

    private static final Supplier<SmartRandom> RANDOM = SmartRandom.builder()
                                                                   .set(Boolean.class, rnd -> TRUE)
                                                                   .set(Byte.class, rnd -> BYTE27)
                                                                   .set(long.class, rnd -> LONG278)
                                                                   .prepare();

    private final SmartRandom random = RANDOM.get();

    @Test
    public final void anyBoolean() {
        assertEquals(TRUE, random.get(boolean.class));
        assertEquals(TRUE, random.get(Boolean.class));
    }

    @Test
    public final void anyByte() {
        assertEquals(BYTE27, random.get(byte.class));
        assertEquals(BYTE27, random.get(Byte.class));
    }

    @Test
    public final void anyLong() {
      assertEquals(LONG278, random.get(long.class));
      assertEquals(LONG278, random.get(Long.class));
    }
}
