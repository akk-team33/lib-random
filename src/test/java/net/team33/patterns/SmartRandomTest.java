package net.team33.patterns;

import com.google.common.base.CaseFormat;
import net.team33.patterns.test.Recursive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SmartRandomTest {

    private static final Function<SmartRandom, Recursive> RANDOM_RECURSIVE =
            random -> new Recursive(random.any(Recursive[].class));
    private static final Supplier<SmartRandom> RANDOM = SmartRandom.builder()
            .put(Recursive.class, RANDOM_RECURSIVE, 3, null)
            .prepare();
    private static final Class<?>[] CLASSES = {
            // Singles ...
            Boolean.TYPE, Boolean.class, Byte.TYPE, Byte.class, Short.TYPE, Short.class,
            Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class,
            Character.TYPE, Character.class, Number.class, Object.class, CaseFormat.class,
            String.class/*, Date.class, BigInteger.class, BigDecimal.class*/, Recursive.class,
            // Arrays ...
            boolean[].class, Boolean[].class, byte[].class, Byte[].class, short[].class, Short[].class,
            int[].class, Integer[].class, long[].class, Long[].class, float[].class, Float[].class,
            double[].class, Double[].class, char[].class, Character[].class, CaseFormat[].class,
            String[].class/*, Date[].class, BigInteger[].class, BigDecimal[].class*/, Recursive[].class};

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