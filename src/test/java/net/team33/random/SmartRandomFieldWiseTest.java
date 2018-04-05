package net.team33.random;

import net.team33.random.test.DataObject;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SmartRandomFieldWiseTest {

    private static final int MAX_RETRY =
            1000;
    private static final String RESULT_SHOULD_BE_IN_CHARSET =
            "Any result should be from \"%s\" but result was '%s'";

    @SuppressWarnings("AssertEqualsMayBeAssertSame")
    @Test
    public final void fieldWise() {
        final SmartRandom random = SmartRandom.builder()
                .put(DataObject.class, rnd -> rnd.fillFields(new DataObject()))
                .build();
        Stream.generate(() -> random.any(DataObject.class)).limit(MAX_RETRY).forEach(result -> {
            assertNotNull(result.getBoolVal());
            assertEquals(Boolean.class, result.getBoolVal().getClass());

            assertNotNull(result.getByteVal());
            assertEquals(Byte.class, result.getByteVal().getClass());

            assertNotNull(result.getShortVal());
            assertEquals(Short.class, result.getShortVal().getClass());

            assertNotNull(result.getIntVal());
            assertEquals(Integer.class, result.getIntVal().getClass());

            assertNotNull(result.getLongVal());
            assertEquals(Long.class, result.getLongVal().getClass());

            assertNotNull(result.getFloatVal());
            assertEquals(Float.class, result.getFloatVal().getClass());

            assertNotNull(result.getDoubleVal());
            assertEquals(Double.class, result.getDoubleVal().getClass());

            assertNotNull(result.getCharVal());
            assertEquals(Character.class, result.getCharVal().getClass());

            assertNotNull(result.getStringVal());
            assertEquals(String.class, result.getStringVal().getClass());
        });
    }
}