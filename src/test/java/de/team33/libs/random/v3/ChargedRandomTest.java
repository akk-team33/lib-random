package de.team33.libs.random.v3;

import org.junit.Test;

import java.lang.reflect.Array;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChargedRandomTest {

    private static final boolean[] PRIMITIVE_BOOLEANS = {false, true};
    private static final Boolean[] OBJECT_BOOLEANS = {false, true};
    private static final ChargedRandom.Template TEMPLATE = ChargedRandom.builder()
            .put(Boolean.class, type -> random -> true)
            .put(boolean[].class, type -> random -> PRIMITIVE_BOOLEANS)
            .put(Boolean[].class, type -> random -> OBJECT_BOOLEANS)
            .prepare();

    private final ChargedRandom nailedRandom = TEMPLATE.get();
    private final ChargedRandom plainRandom = ChargedRandom.instance();

    @Test
    public final void getBoolean() {
        assertEquals(Boolean.TRUE, nailedRandom.get(boolean.class));
        assertEquals(Boolean.TRUE, nailedRandom.get(Boolean.class));
        assertEquals(Boolean.class, plainRandom.get(boolean.class).getClass());
        assertEquals(Boolean.class, plainRandom.get(Boolean.class).getClass());
    }

    @Test
    public final void getBooleanArray() {
        assertArrayEquals(PRIMITIVE_BOOLEANS, nailedRandom.get(boolean[].class));
        assertArrayEquals(OBJECT_BOOLEANS, nailedRandom.get(Boolean[].class));
        assertTrue(Boolean.TYPE.isInstance(true));
        assertArray(boolean[].class, plainRandom.get(boolean[].class));
        assertEquals(Boolean.class, plainRandom.get(Boolean[].class).getClass());
    }

    private static <T> void assertArray(final Class<T> arrayClass, final T array) {
        assertEquals(arrayClass, array.getClass());
        for (int index = 0; index < Array.getLength(array); ++index) {
            assertTrue(arrayClass.getComponentType().isInstance(Array.get(array, index)));
        }
    }
}