package net.team33.random;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericTest {

    @Test(expected = RuntimeException.class)
    public final void failDirectGeneric() {
        final Direct<String> direct = new Direct<>();
        Assert.fail("expected to Fail but was " + direct.getCompound());
    }

    @Test(expected = RuntimeException.class)
    public final void failIndirect() {
        final Generic<?> indirect = new Indirect();
        Assert.fail("expected to Fail but was " + indirect.getCompound());
    }

    @Test
    public final void simple() {
        Assert.assertEquals(
                new Generic.Compound(String.class),
                new Generic<String>() {
                }.getCompound()
        );
    }

    @Test
    public final void list() {
        Assert.assertEquals(
                new Generic.Compound(List.class, new Generic.Compound(String.class)),
                new Generic<List<String>>() {
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
                new Generic<Map<List<String>, Map<Double, Set<Integer>>>>() {
                }.getCompound()
        );
    }

    private static class Direct<T> extends Generic<T> {
    }

    @SuppressWarnings("EmptyClass")
    private static class Indirect extends Direct<String> {
    }
}