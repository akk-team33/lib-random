package de.team33.test.random.v4;

import com.google.common.base.CaseFormat;
import de.team33.libs.random.v4.SmartRandom;
import de.team33.libs.random.v4.Typex;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SmartRandomTest {

    @Test
    public final void anyByClass() {
        final SmartRandom random = SmartRandom.builder().build();
        for (final Class<?> type : Arrays.asList(
                boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class,
                char.class, Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
                Double.class, Character.class, TimeUnit.class, CaseFormat.class, String.class,

                boolean[].class, byte[].class, short[].class, int[].class, long[].class, float[].class,
                double[].class, char[].class, Boolean[].class, Byte[].class, Short[].class, Integer[].class,
                Long[].class, Float[].class, Double[].class, Character[].class, TimeUnit[].class,
                CaseFormat[].class, String[].class
        )) {
            final Object any = random.any(type);
            assertType(type, any);
        }
    }

    @Test
    public final void anyByType() {
        // TODO
    }

    private void assertType(final Class<?> type, final Object value) {
        assertType(type, type, value);
    }

    private void assertType(final Class<?> context, final Class<?> type, final Object value) {
        final String message =
                format("[%s] expected %s but was ", context.getCanonicalName(), type.getCanonicalName());
        assertNotNull(message + "null", value);
        assertTrue(message + value.getClass().getCanonicalName(), Typex.isInstance(type, value));
        if (value.getClass().isArray()) {
            assertArrayElementsType(context, type, value);
        }
    }

    private void assertArrayElementsType(final Class<?> context, final Class<?> type, final Object value) {
        for (int index = 0; index < Array.getLength(value); ++index) {
            assertType(context, type.getComponentType(), Array.get(value, index));
        }
    }
}
