package de.team33.libs.random.v4;

import static java.lang.Boolean.TRUE;

import static org.junit.Assert.assertEquals;

import java.util.function.Supplier;

import de.team33.libs.typing.v3.Type;
import org.junit.Test;


@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class DispenserBaseTest {

    private static final Byte BYTE27 = (byte) 27;
    private static final Long LONG278 = (long) 278;

    private static final Methods METHODS = new MethodCache.Builder()
            .put(Type.of(Boolean.class), dsp -> TRUE)
            .put(Type.of(Byte.class), dsp -> BYTE27)
            .put(Type.of(long.class), dsp -> LONG278)
            .build();

    private static final Supplier<Features> FEATURES = new Features.Builder()
            .prepare();

    private final Dispenser dsp = new DispenserBase(METHODS, FEATURES.get());

    @Test
    public final void anyBoolean() {
        assertEquals(TRUE, dsp.any(boolean.class));
        assertEquals(TRUE, dsp.any(Boolean.class));
    }

    @Test
    public final void anyByte() {
        assertEquals(BYTE27, dsp.any(byte.class));
        assertEquals(BYTE27, dsp.any(Byte.class));
    }

    @Test
    public final void anyLong() {
        assertEquals(LONG278, dsp.any(long.class));
        assertEquals(LONG278, dsp.any(Long.class));
    }
}
