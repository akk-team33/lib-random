package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;


public class TypesTest {

    private static final Type<Collection<Integer>> COLLECTION_TYPE = new Type<Collection<Integer>>() {};
    private static final Type<Map<String, List<String>>> MAP_TYPE = new Type<Map<String, List<String>>>() {};

    @Test
    public final void list() {
        assertList(Type.of(boolean.class), Type.of(Boolean.class));
        assertList(Type.of(byte.class), Type.of(Byte.class));
        assertList(Type.of(short.class), Type.of(Short.class));
        assertList(Type.of(int.class), Type.of(Integer.class));
        assertList(Type.of(long.class), Type.of(Long.class));
        assertList(Type.of(float.class), Type.of(Float.class));
        assertList(Type.of(double.class), Type.of(Double.class));
        assertList(Type.of(char.class), Type.of(Character.class));
        assertList(Type.of(Object.class));
        assertList(Type.of(String.class));
        assertList(Type.of(Date.class));
        assertList(Type.of(int[].class));
        assertList(Type.of(Integer[].class));
        assertList(COLLECTION_TYPE);
        assertList(MAP_TYPE);
    }

    @SafeVarargs
    private static <T> void assertList(final Type<T>... types) {
        for (final Type<T> type : types) {
            Assert.assertEquals(asList(types), Types.list(type));
        }
    }
}
