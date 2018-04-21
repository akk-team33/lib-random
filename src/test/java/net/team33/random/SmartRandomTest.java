package net.team33.random;

import com.google.common.base.CaseFormat;
import net.team33.random.test.DataObject;
import net.team33.random.test.Recursive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SmartRandomTest {

    private static final Generic<List<String>> LIST_OF_STRING = new Generic<List<String>>() {
    };
    private static final Generic<Map<BigInteger, BigDecimal>> MAP_BIGINT_BIGDEC = new Generic<Map<BigInteger, BigDecimal>>() {
    };
    private static final Generic<Map<BigInteger, List<String>>> MAP_BIGINT_LIST = new Generic<Map<BigInteger, List<String>>>() {
    };
    private static final Generic<Map<String, List<String>>> MAP_STRING_LIST = new Generic<Map<String, List<String>>>() {
    };
    private static final Supplier<SmartRandom> RANDOM = SmartRandom.builder()
            .put(MAP_BIGINT_BIGDEC, SmartRandomTest::newMapBigIntBigDec)
            .put(MAP_BIGINT_LIST, SmartRandomTest::newMapBigIntList)
            .put(MAP_STRING_LIST, SmartRandomTest::newMapStringList)
            .put(Recursive.class, () -> new Limited<>(rnd -> new Recursive(rnd.any(Recursive[].class)), 3, null))
            .put(DataObject.class, random -> random.setAll(new DataObject()))
            .put(LIST_OF_STRING, random -> new ArrayList<>(Arrays.asList(random.any(String[].class))))
            .put(Number.class, random -> random.any(Double.class))
            .put(Object.class, random -> random.any(String.class))
            .prepare();

    private static Map<BigInteger, List<String>> newMapBigIntList(final SmartRandom random) {
        return newMap(random.basic.anyInt(3), () -> random.any(BigInteger.class), () -> random.any(LIST_OF_STRING));
    }

    private static Map<String, List<String>> newMapStringList(final SmartRandom random) {
        return newMap(random, Generic.of(String.class), LIST_OF_STRING);
    }

    private static Map<BigInteger, BigDecimal> newMapBigIntBigDec(final SmartRandom random) {
        return newMap(random.basic.anyInt(3), () -> random.any(BigInteger.class), () -> random.any(BigDecimal.class));
    }

    private static <K, V> Map<K, V> newMap(final int size, final Supplier<K> keySupp, final Supplier<V> valSupp) {
        return Stream.generate(keySupp).distinct()
                .limit(size)
                .collect(Collectors.toMap(key -> key, key -> valSupp.get()));
    }

    private static <K, V> Map<K, V> newMap(final SmartRandom random, final Generic<K> keyGen, final Generic<V> valGen) {
        return Stream.generate(() -> random.any(keyGen)).distinct()
                .limit(random.basic.anyInt(3))
                .collect(Collectors.toMap(key -> key, key -> random.any(valGen)));
    }

    private static final Class<?>[] CLASSES = {
            // Singles ...
            Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
            Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
            Character.TYPE, Character.class, Number.class, Object.class, CaseFormat.class,
            String.class, Date.class, BigInteger.class, BigDecimal.class,
            Recursive.class, DataObject.class,
            // Arrays ...
            boolean[].class, Boolean[].class, byte[].class, Byte[].class, short[].class, Short[].class,
            int[].class, Integer[].class, long[].class, Long[].class, float[].class, Float[].class,
            double[].class, Double[].class, char[].class, Character[].class, CaseFormat[].class,
            String[].class, Date[].class, BigInteger[].class, BigDecimal[].class,
            Recursive[].class, DataObject[].class};

    private final Class<?> rClass;
    private final SmartRandom random;

    public SmartRandomTest(final Class<?> rClass) {
        this.rClass = rClass;
        this.random = RANDOM.get();
    }

    @Parameters(name = "{index}: any({0})")
    public static Collection<Object[]> parameters() {
        return Stream.of(CLASSES)
                .map(aClass -> new Object[]{aClass})
                .collect(Collectors.toList());
    }

    @Test
    public final void any() throws Exception {
        final Object result = random.any(rClass);
        Assert.assertNotNull(result);
        if (rClass.isPrimitive()) {
            Assert.assertSame(rClass.getCanonicalName(), rClass, result.getClass().getField("TYPE").get(null));
        } else {
            Assert.assertTrue(rClass.getCanonicalName(), rClass.isAssignableFrom(result.getClass()));
        }
    }
}