package de.team33.libs.random.v3;

import de.team33.libs.random.misc.Features;
import de.team33.libs.typing.v3.Type;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.function.Function;


public class DispenserTest {

    private static final Key<BasicRandom.Simple> BASIC = new Key<>();
    private static final Features.Stage STAGE = Features.builder()
            .setup(BASIC, BasicRandom.Simple::new)
            .prepare();
    private static final MethodPool<Dispenser> DUMMY = new MethodPool<Dispenser>() {
        @Override
        public <R> Function<Dispenser, R> get(final Type<R> type) {
            throw new IllegalArgumentException("no method found for " + type);
        }
    };
    private static final Date THE_DATE = new Date();

    private final Dispenser subject;

    public DispenserTest() {
        subject = new Dispenser(STAGE, MethodCache.builder(DUMMY)
                .put(int.class, dsp -> 278)
                .put(Date.class, dsp -> THE_DATE)
                .build());
    }

    @Test
    public void get() {
        Assert.assertEquals(Integer.valueOf(278), subject.get(int.class));
        Assert.assertEquals(THE_DATE, subject.get(Date.class));
        try {
            final String result = subject.get(String.class);
            Assert.fail("expected to fail but was \"" + result + "\"");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    private static class Key<T> extends Features.Key<T> {
    }
}
