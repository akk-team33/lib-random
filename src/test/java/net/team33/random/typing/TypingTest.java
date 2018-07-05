package net.team33.random.typing;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypingTest {

    private static final Typing<List<String>> LIST_OF_STRING =
            new Typing<List<String>>() {
            };
    private static final Typing<String> STRING_TYPE =
            new Typing<String>() {
            };
    private static final Typing<Map<List<String>, Map<Double, Set<Integer>>>> MAP_OF_LIST_TO_MAP =
            new Typing<Map<List<String>, Map<Double, Set<Integer>>>>() {
            };

    @Test(expected = IllegalStateException.class)
    public final void failDirectGeneric() {
        final Direct<String> direct = new Direct<>();
        Assert.fail("expected to Fail but was " + direct.getSetup());
    }

    @Test(expected = IllegalStateException.class)
    public final void failIndirect() {
        final Typing<?> indirect = new Indirect();
        Assert.fail("expected to Fail but was " + indirect.getSetup());
    }

    @Test
    public final void simple() {
        Assert.assertEquals(
                new TypeSetup(String.class),
                STRING_TYPE.getSetup()
        );
    }

    @Test
    public final void list() {
        Assert.assertEquals(
                new TypeSetup(List.class, new TypeSetup(String.class)),
                LIST_OF_STRING.getSetup()
        );
    }

    @SuppressWarnings("rawtypes")
    @Test
    public final void rawList() {
        Assert.assertEquals(
                new TypeSetup(List.class),
                new Typing<List>() {
                }.getSetup()
        );
    }

    @Test
    public final void map() {
        Assert.assertEquals(
                new TypeSetup(
                        Map.class,
                        new TypeSetup(List.class, new TypeSetup(String.class)),
                        new TypeSetup(
                                Map.class,
                                new TypeSetup(Double.class),
                                new TypeSetup(Set.class, new TypeSetup(Integer.class)))),
                MAP_OF_LIST_TO_MAP.getSetup()
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

    private static class Direct<T> extends Typing<T> {
    }

    @SuppressWarnings("EmptyClass")
    private static class Indirect extends Direct<String> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringList1 extends Typing<List<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet1 extends Typing<Set<String>> {
    }

    @SuppressWarnings("EmptyClass")
    private static class StringSet2 extends Typing<Set<String>> {
    }
}