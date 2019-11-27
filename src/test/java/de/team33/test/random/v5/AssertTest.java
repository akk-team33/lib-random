package de.team33.test.random.v5;

import org.junit.Test;

import java.util.Random;

import static de.team33.test.random.v5.Assert.DOUBLE;
import static de.team33.test.random.v5.Assert.NOT_NULL;
import static de.team33.test.random.v5.Assert.NULL;

public class AssertTest {

    @Test(expected = AssertionError.class)
    public void fail() {
        Assert.fail("message");
    }

    @Test
    public void thatNull() {
        Assert.that(null)
              .is(NULL);
    }

    @Test
    public void that() {
        Assert.that(new Random().nextDouble())
              .is(NOT_NULL)
              .is(DOUBLE);
    }
}
