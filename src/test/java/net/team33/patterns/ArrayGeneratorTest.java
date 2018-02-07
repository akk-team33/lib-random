package net.team33.patterns;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ArrayGeneratorTest {

    private static final RandomX RANDOM = RandomX.builder()
            .setBounds(new Bounds(5, 5))
            .build();

    private ArrayGenerator subject;

    @Before
    public final void setUp() {
        subject = RANDOM.generator().array;
    }

    @Test
    public final void ofBoolean() {
        final boolean[] booleans = subject.ofBoolean();
        Assert.assertEquals(5, booleans.length);
    }

    @Test
    public void ofByte() {
    }

    @Test
    public void ofShort() {
    }

    @Test
    public void ofInt() {
    }

    @Test
    public void ofLong() {
    }

    @Test
    public void ofFloat() {
    }

    @Test
    public void ofDouble() {
    }

    @Test
    public void ofChar() {
    }

    @Test
    public void of() {
    }
}