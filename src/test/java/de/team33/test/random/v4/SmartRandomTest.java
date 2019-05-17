package de.team33.test.random.v4;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.google.common.base.CaseFormat;
import de.team33.libs.random.v4.SmartRandom;
import de.team33.libs.random.v4.Typex;
import org.junit.Test;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SmartRandomTest {

    @Test
    public final void anyByClass() {
        final SmartRandom random = SmartRandom.builder().build();
        for (final Class<?> type : Arrays.asList(
          boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class,
          char.class, Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
          Double.class, Character.class, TimeUnit.class, CaseFormat.class,

          boolean[].class, byte[].class, short[].class, int[].class, long[].class, float[].class,
          double[].class, char[].class, Boolean[].class, Byte[].class, Short[].class, Integer[].class,
          Long[].class, Float[].class, Double[].class, Character[].class, TimeUnit[].class,
          CaseFormat[].class
        )) {
            final Object any = random.any(type);
            assertType(type, any);
        }
    }

    private void assertType(final Class<?> type, final Object value) {
      assertType(type, type, value);
    }

    private void assertType(final Class<?> context, final Class<?> type, final Object value) {
      final String message =
        "[" + context.getCanonicalName() + "] expected " + type.getCanonicalName() + " but was ";
      assertNotNull(message + "null", value);
      assertTrue(message + value.getClass().getCanonicalName(), Typex.isInstance(type, value));
      if (value.getClass().isArray()) {
          assertArrayElementsType(context, type, value);
      }
    }

  private void assertArrayElementsType(final Class<?> context, final Class<?> type, final Object value)
  {
    for ( int index = 0; index < Array.getLength(value); ++index ) {
        assertType(context, type.getComponentType(), Array.get(value, index));
    }
  }

  @Test
    public final void anyByType() {
    }
}
