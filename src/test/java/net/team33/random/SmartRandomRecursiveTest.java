package net.team33.random;

import net.team33.random.test.Recursive;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class SmartRandomRecursiveTest {

    private static final Function<SmartRandom, Recursive> RANDOM_RECURSIVE =
            random -> new Recursive(random.any(Recursive[].class));

    @Test
    public final void anyRecursive0Null() {
        Assert.assertNull(SmartRandom.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 0, null).build()
                .any(Recursive.class));
    }

    @Test
    public final void anyRecursive0Empty() {
        Assert.assertSame(Recursive.EMPTY, SmartRandom.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 0, Recursive.EMPTY).build()
                .any(Recursive.class));
    }

    @Test(expected = StackOverflowError.class)
    public final void anyRecursiveFail() {
        //noinspection ObjectToString
        Assert.fail("Should fail but was " + SmartRandom.builder()
                .put(Recursive.class, RANDOM_RECURSIVE).build()
                .any(Recursive.class));
    }

    @Test
    public final void anyRecursive() {
        final Recursive recursive = SmartRandom.builder()
                .put(Recursive.class, RANDOM_RECURSIVE, 3, null).build()
                .any(Recursive.class);
        Assert.assertNotNull(recursive);
        Assert.assertNotNull(recursive.getChildren()[0]);
        Assert.assertNotNull(recursive.getChildren()[0].getChildren()[0]);
        Assert.assertNull(recursive.getChildren()[0].getChildren()[0].getChildren()[0]);
    }
}