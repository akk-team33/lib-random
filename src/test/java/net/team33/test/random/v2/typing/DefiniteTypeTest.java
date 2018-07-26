package net.team33.test.random.v2.typing;

import net.team33.random.v2.typing.DefiniteType;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SuppressWarnings({"AnonymousInnerClass", "AnonymousInnerClassMayBeStatic"})
public class DefiniteTypeTest {

    @Test
    public final void simple() {
        assertStringType(new DefiniteType<String>() {
        });
        assertStringType(DefiniteType.of(String.class));
    }

    @Test
    public final void derivedSimple() {
        assertStringType(new StringType());
    }

    @Test
    public final void stringList() {
        assertStringListType(new DefiniteType<List<String>>() {
        });
    }

    @Test
    public final void rawList() {
        //noinspection rawtypes
        assertRawList(new DefiniteType<List>() {
        });
        assertRawList(DefiniteType.of(List.class));
    }

    @Test
    public final void mapStringToListOfString() {
        assertMapStringToListOfString(new DefiniteType<Map<String, List<String>>>() {
        });
    }

    @Test
    public final void memberType() throws NoSuchFieldException {
        final DefiniteType<Container<String, String, List<String>>> containerType =
                new DefiniteType<Container<String, String, List<String>>>() {
                };
        final DefiniteType<?> listType = containerType.getMemberType(
                Container.class.getField("listContent").getGenericType());
        final DefiniteType<?> mapType = containerType.getMemberType(
                Container.class.getField("mapContent").getGenericType());
        assertStringListType(listType);
        assertMapStringToListOfString(mapType);
    }

    private static void assertMapStringToListOfString(final DefiniteType<?> mapType) {
        assertSame(Map.class, mapType.getUnderlyingClass());

        final List<DefiniteType<?>> parameters = mapType.getActualParameters();
        assertEquals(2, parameters.size());
        assertStringType(parameters.get(0));
        assertStringListType(parameters.get(1));
    }

    private static void assertRawList(final DefiniteType<?> rawListType) {
        assertSame(List.class, rawListType.getUnderlyingClass());

        final List<DefiniteType<?>> parameters = rawListType.getActualParameters();
        assertEquals(0, parameters.size());
    }

    public static void assertStringListType(final DefiniteType<?> stringListType) {
        assertSame(List.class, stringListType.getUnderlyingClass());

        final List<DefiniteType<?>> parameters = stringListType.getActualParameters();
        assertEquals(1, parameters.size());
        assertStringType(parameters.get(0));

        assertEquals(stringListType, new DefiniteType<List<String>>() {
        });
    }

    private static void assertStringType(final DefiniteType<?> stringType) {
        assertSame(String.class, stringType.getUnderlyingClass());
        assertEquals(0, stringType.getActualParameters().size());
        assertEquals(stringType, new DefiniteType<String>() {
        });
    }

    @SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor", "AbstractClassWithoutAbstractMethods", "EmptyClass"})
    private static final class StringType extends DefiniteType<String> {
    }

    public static class Container<E, K, V> {

        public List<E> listContent;
        public Map<K, V> mapContent;
    }
}
