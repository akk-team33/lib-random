package net.team33.random;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypeTest {

    private static final Type<List<String>> LIST_OF_STRING =
            new Type<List<String>>() {
            };
    private static final Type<String> STRING_TYPE =
            new Type<String>() {
            };
    private static final Type<Map<List<String>, Map<Double, Set<Integer>>>> MAP_OF_LIST_TO_MAP =
            new Type<Map<List<String>, Map<Double, Set<Integer>>>>() {
            };

    @Test(expected = IllegalStateException.class)
    public final void failDirectGeneric() {
        final Direct<String> direct = new Direct<>();
        Assert.fail("expected to Fail but was " + direct.getCompound());
    }

    @Test(expected = IllegalStateException.class)
    public final void failIndirect() {
        final Type<?> indirect = new Indirect();
        Assert.fail("expected to Fail but was " + indirect.getCompound());
    }

    @Test
    public final void simple() {
        Assert.assertEquals(
                new Type.Compound(String.class),
                STRING_TYPE.getCompound()
        );
    }

    @Test
    public final void list() {
        Assert.assertEquals(
                new Type.Compound(List.class, new Type.Compound(String.class)),
                LIST_OF_STRING.getCompound()
        );
    }

    @SuppressWarnings("rawtypes")
    @Test
    public final void rawList() {
        Assert.assertEquals(
                new Type.Compound(List.class),
                new Type<List>() {
                }.getCompound()
        );
    }

    @Test
    public final void map() {
        Assert.assertEquals(
                new Type.Compound(
                        Map.class,
                        new Type.Compound(List.class, new Type.Compound(String.class)),
                        new Type.Compound(
                                Map.class,
                                new Type.Compound(Double.class),
                                new Type.Compound(Set.class, new Type.Compound(Integer.class)))),
                MAP_OF_LIST_TO_MAP.getCompound()
        );
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    @Test
    public final void equals() {
        Assert.assertEquals(
                new StringSet1(),
                new StringSet2()
        );
        Assert.assertNotEquals(
                new StringList1(),
                new StringSet1()
        );
    }

    private static class Direct<T> extends Type<T> {
    }

    @SuppressWarnings("EmptyClass")
    private static class Indirect extends Direct<String> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringList1 extends Type<List<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet1 extends Type<Set<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet2 extends Type<Set<String>> {
    }
}