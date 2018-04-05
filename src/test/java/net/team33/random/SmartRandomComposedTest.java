package net.team33.random;

import net.team33.random.test.DataObject;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmartRandomComposedTest {

    private static final int MAX_RETRY = 1000;

    @SuppressWarnings({"AssertEqualsMayBeAssertSame", "Duplicates"})
    @Test
    public final void fieldWise() {
        final SmartRandom random = SmartRandom.builder()
                .put(DataObject.class, rnd -> rnd.setAllFields(new DataObject()))
                .build();
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY).forEach(result -> {
            Reflect.publicGetters(DataObject.class).forEach(getter -> {
                final Object value = invoke(getter, result);
                final String message = getter.getName() + "()";
                assertNotNull(message, value);
                assertTrue(message, getter.getReturnType().isAssignableFrom(value.getClass()));
            });
        });
    }

    @SuppressWarnings({"AssertEqualsMayBeAssertSame", "Duplicates"})
    @Test
    public final void setterdWise() {
        final SmartRandom random = SmartRandom.builder()
                .put(DataObject.class, rnd -> rnd.setAll(new DataObject()))
                .build();
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY).forEach(result -> {
            Reflect.publicGetters(DataObject.class).forEach(getter -> {
                final Object value = invoke(getter, result);
                final String message = getter.getName() + "()";
                assertNotNull(message, value);
                assertTrue(message, getter.getReturnType().isAssignableFrom(value.getClass()));
            });
        });
    }

    private static Object invoke(final Method getter, final Object subject) {
        try {
            return getter.invoke(subject);
        } catch (final Exception caught) {
            throw new IllegalStateException(caught.getMessage(), caught);
        }
    }
}