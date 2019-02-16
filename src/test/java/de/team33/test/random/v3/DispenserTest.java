package de.team33.test.random.v3;

import de.team33.libs.random.misc.Features;
import de.team33.libs.random.v3.BasicRandom;
import de.team33.libs.random.v3.Dispenser;
import de.team33.libs.random.v3.methods.MethodCache;
import de.team33.libs.random.v3.methods.MethodFault;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;


public class DispenserTest {

    private static final Key<BasicRandom.Simple> BASIC = new Key<>();
    private static final Features.Stage STAGE = Features.builder()
            .setup(BASIC, BasicRandom.Simple::new)
            .prepare();
    private static final Date THE_DATE = new Date();
    private static final MethodFault<Dispenser> FAULT = MethodFault.instance();

    private final Dispenser subject;

    public DispenserTest() {
        subject = new Dispenser(STAGE, MethodCache.builder(FAULT)
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
