package net.team33.random;

import com.google.common.base.CaseFormat;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ChargedRandomGenericTest<T> {

    private static final Generic<CaseFormat> CASE_FORMAT = new Generic<CaseFormat>() {
    };
    private static final Supplier<ChargedRandom> RANDOM = ChargedRandom.builder()
            .prepare();

    private final Generic<T> type;
    private final ChargedRandom random;

    public ChargedRandomGenericTest(final Generic<T> type) {
        this.type = type;
        this.random = RANDOM.get();
    }

    @Parameters(name = "{index}: any({0})")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                // Singles ...
                CASE_FORMAT
        ).stream()
                .map(generic -> new Object[]{generic})
                .collect(Collectors.toList());
    }

    @Test
    public final void any() throws Exception {
        final Object result = random.any(type);
        Assert.assertNotNull(result);
        final Class<?> rawClass = type.getCompound().getRawClass();
        if (rawClass.isPrimitive()) {
            Assert.assertSame(rawClass.getCanonicalName(), rawClass, result.getClass().getField("TYPE").get(null));
        } else {
            Assert.assertTrue(type.toString(), rawClass.isAssignableFrom(result.getClass()));
        }
    }
}