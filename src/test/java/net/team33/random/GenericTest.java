package net.team33.random;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericTest {

    private static final Generic<List<String>> LIST_OF_STRING =
            new Generic<List<String>>() {
            };
    private static final Generic<String> STRING_GENERIC =
            new Generic<String>() {
            };
    private static final Generic<Map<List<String>, Map<Double, Set<Integer>>>> MAP_OF_LIST_TO_MAP =
            new Generic<Map<List<String>, Map<Double, Set<Integer>>>>() {
            };

    @Test(expected = IllegalStateException.class)
    public final void failDirectGeneric() {
        final Direct<String> direct = new Direct<>();
        Assert.fail("expected to Fail but was " + direct.getCompound());
    }

    @Test(expected = IllegalStateException.class)
    public final void failIndirect() {
        final Generic<?> indirect = new Indirect();
        Assert.fail("expected to Fail but was " + indirect.getCompound());
    }

    @Test
    public final void simple() {
        Assert.assertEquals(
                new Generic.Compound(String.class),
                STRING_GENERIC.getCompound()
        );
    }

    @Test
    public final void list() {
        Assert.assertEquals(
                new Generic.Compound(List.class, new Generic.Compound(String.class)),
                LIST_OF_STRING.getCompound()
        );
    }

    @SuppressWarnings("rawtypes")
    @Test
    public final void rawList() {
        Assert.assertEquals(
                new Generic.Compound(List.class),
                new Generic<List>() {
                }.getCompound()
        );
    }

    @Test
    public final void map() {
        Assert.assertEquals(
                new Generic.Compound(
                        Map.class,
                        new Generic.Compound(List.class, new Generic.Compound(String.class)),
                        new Generic.Compound(
                                Map.class,
                                new Generic.Compound(Double.class),
                                new Generic.Compound(Set.class, new Generic.Compound(Integer.class)))),
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

    private static class Direct<T> extends Generic<T> {
    }

    @SuppressWarnings("EmptyClass")
    private static class Indirect extends Direct<String> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringList1 extends Generic<List<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet1 extends Generic<Set<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet2 extends Generic<Set<String>> {
    }
}