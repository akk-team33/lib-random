package net.team33.random;

import net.team33.random.test.DataObject;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmartRandomComposedTest {

    private static final int MAX_RETRY = 1000;

    @Test
    public final void unknownNull() {
        final SmartRandom random = SmartRandom.builder()
                .setUnknownHandling(UnknownHandling.RETURN_NULL)
                .build();
        //noinspection Convert2MethodRef
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY)
                .forEach(object -> Assert.assertNull(object));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void unknownFail() {
        final SmartRandom random = SmartRandom.builder()
                .setUnknownHandling(UnknownHandling.FAIL)
                .build();
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY)
                .forEach(result -> Assert.fail(String.format("should fail but was <%s>", result)));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void unknownDefault() {
        final SmartRandom random = SmartRandom.builder()
                .build();
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY)
                .forEach(result -> Assert.fail(String.format("should fail but was <%s>", result)));
    }

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