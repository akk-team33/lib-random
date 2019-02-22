package de.team33.test.random.v3;

import java.util.Date;

import de.team33.libs.random.v3.PoolDispenser;
import de.team33.libs.random.v3.methods.MethodCache;
import de.team33.libs.random.v3.methods.MethodFault;
import org.junit.Assert;
import org.junit.Test;


public class PoolDispenserTest {

    private static final Date THE_DATE = new Date();
    private static final MethodFault<Void> FAULT = MethodFault.instance();

    private final PoolDispenser<Void> subject;

    public PoolDispenserTest() {
        subject = new PoolDispenser<>(MethodCache.builder(FAULT)
                                                 .put(int.class, dsp -> 278)
                                                 .put(Date.class, dsp -> THE_DATE)
                                                 .build(), null);
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
}
