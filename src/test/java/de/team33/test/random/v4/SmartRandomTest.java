package de.team33.test.random.v4;

import static java.lang.Boolean.TRUE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Supplier;

import de.team33.libs.random.v4.SmartRandom;
import de.team33.libs.random.v4.Typex;
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
    public final void anyByClass() {
        final SmartRandom random = SmartRandom.builder().build();
        for (final Class<?> type : Arrays.asList(
                boolean.class,
                Boolean.class,
                byte.class,
                Byte.class,
                short.class,
                Short.class,
                int.class,
                Integer.class,
                long.class,
                Long.class,
                float.class,
                Float.class,
                double.class,
                Double.class,
                char.class,
                Character.class
        )) {
            final Object any = random.any(type);
            assertNotNull(any);
            assertTrue("expected "
                    + type.getCanonicalName()
                    + " but was "
                    + any.getClass().getCanonicalName(),
                    Typex.isInstance(type, any));
        }
    }

    @Test
    public final void anyByType() {
    }
}
