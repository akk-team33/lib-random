package de.team33.test.random.v1;

import de.team33.libs.random.v1.SmartRandom;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SmartRandomTest {

    private static final SmartRandom.Template RANDOM = SmartRandom.builder()
            .put(Integer.TYPE, type -> rnd -> 278)
            .prepare();

    @Test
    public final void anyInt() {
        assertThat(RANDOM.get().any(Integer.TYPE), instanceOf(Integer.class));
    }
}