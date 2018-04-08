package net.team33.random;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TypingTest {

    @Test(expected = RuntimeException.class)
    public final void failDirectGeneric() {
        final Direct<String> direct = new Direct<>();
        Assert.fail("expected to Fail but was " + direct.getSetup());
    }

    @Test(expected = RuntimeException.class)
    public final void failIndirect() {
        final Typing<?> indirect = new Indirect();
        Assert.fail("expected to Fail but was " + indirect.getSetup());
    }

    @Test
    public final void simple() {
        Assert.assertEquals(
                new Typing.Setup(String.class),
                new Typing<String>() {
                }.getSetup()
        );
    }

    @Test
    public final void list() {
        Assert.assertEquals(
                new Typing.Setup(List.class, new Typing.Setup(String.class)),
                new Typing<List<String>>() {
                }.getSetup()
        );
    }

    @Test
    public final void map() {
        Assert.assertEquals(
                new Typing.Setup(
                        Map.class,
                        new Typing.Setup(List.class, new Typing.Setup(String.class)),
                        new Typing.Setup(
                                Map.class,
                                new Typing.Setup(Double.class),
                                new Typing.Setup(Set.class, new Typing.Setup(Integer.class)))),
                new Typing<Map<List<String>, Map<Double, Set<Integer>>>>() {
                }.getSetup()
        );
    }

    private static class Direct<T> extends Typing<T> {
    }

    @SuppressWarnings("EmptyClass")
    private static class Indirect extends Direct<String> {
    }
}