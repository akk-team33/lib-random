package net.team33.random;

import net.team33.random.test.DataObject;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SmartRandomComposedTest {

    private static final int MAX_RETRY = 1000;
    private static final Generic<List<String>> LIST_OF_STRING = new Generic<List<String>>() {
    };
    private static final Generic<Map<BigInteger, List<String>>> MAP_INTEGER_TO_LIST = new Generic<Map<BigInteger, List<String>>>() {
    };
    static final Generic<Map<String, List<String>>> MAP_STRING_TO_LIST = new Generic<Map<String, List<String>>>() {
    };
    private static final Generic<Map<BigInteger, BigDecimal>> MAP_INTEGER_TO_DECIMAL = new Generic<Map<BigInteger, BigDecimal>>() {
    };

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

    private static Map<BigInteger, List<String>> newIntegerToListMap(final SmartRandom rnd) {
        final int size = rnd.basic.anyInt(3);
        return Stream.generate(() -> rnd.any(BigInteger.class))
                .distinct().limit(size)
                .collect(Collectors.toMap(key -> key, key -> rnd.any(LIST_OF_STRING)));
    }

    @SuppressWarnings({"AssertEqualsMayBeAssertSame", "Duplicates"})
    @Test
    public final void fieldWise() {
        final SmartRandom random = SmartRandom.builder()
                .put(MAP_INTEGER_TO_LIST, SmartRandomComposedTest::newIntegerToListMap)
                .put(MAP_INTEGER_TO_DECIMAL, rnd -> new HashMap<>(0))
                .put(MAP_STRING_TO_LIST, rnd -> new HashMap<>(0))
                .put(LIST_OF_STRING, rnd -> new ArrayList<>(Arrays.asList(rnd.any(String[].class))))
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
    public final void setterWise() {
        final SmartRandom random = SmartRandom.builder()
                .put(MAP_INTEGER_TO_LIST, SmartRandomComposedTest::newIntegerToListMap)
                .put(MAP_INTEGER_TO_DECIMAL, rnd -> new HashMap<>(0))
                .put(MAP_STRING_TO_LIST, rnd -> new HashMap<>(0))
                .put(LIST_OF_STRING, rnd -> new ArrayList<>(Arrays.asList(rnd.any(String[].class))))
                .put(DataObject.class, rnd -> rnd.setAll(new DataObject()))
                .prepare().get();
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