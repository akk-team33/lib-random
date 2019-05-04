package de.team33.test.random.v4;

import static java.lang.Boolean.TRUE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Supplier;

import de.team33.libs.random.v4.SmartRandom;
import de.team33.libs.typing.v3.Type;
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
        assertEquals(TRUE, random.any(boolean.class));
        assertEquals(TRUE, random.any(Boolean.class));
    }

    @Test
    public final void anyByte() {
        assertEquals(BYTE27, random.any(byte.class));
        assertEquals(BYTE27, random.any(Byte.class));
    }

    @Test
    public final void anyLong() {
        assertEquals(LONG278, random.any(long.class));
        assertEquals(LONG278, random.any(Long.class));
    }

    @Test
    public final void anyByType() {
        final SmartRandom random = SmartRandom.builder().build();
        for (final Type<?> type : Arrays.asList(
                Type.of(boolean.class),
                Type.of(Boolean.class),
                Type.of(byte.class),
                Type.of(Byte.class),
                Type.of(short.class),
                Type.of(Short.class),
                Type.of(int.class),
                Type.of(Integer.class),
                Type.of(long.class),
                Type.of(Long.class),
                Type.of(float.class),
                Type.of(Float.class),
                Type.of(double.class),
                Type.of(Double.class),
                Type.of(char.class),
                Type.of(Character.class)
        )) {
            final Object any = random.any(type);
            assertNotNull(any);
            assertTrue(type.getUnderlyingClass().isInstance(any));
        }
    }
}
