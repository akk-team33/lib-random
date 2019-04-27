package de.team33.test.random.v4;

import com.google.common.collect.ImmutableMap;
import de.team33.libs.identification.v1.Unique;
import de.team33.libs.random.v4.Dispenser;
import de.team33.libs.random.v4.SmartRandom;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public class SmartRandomTest {

    @Test
    public final void getMethod() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Test
    public final void getFeature() {
        final Date date0 = new Date();
        final Dispenser random = SmartRandom.builder()
                .setFeature(Key.DATE1, () -> new Date(date0.getTime()))
                .build();
        assertEquals(date0, random.getFeature(Key.DATE1));
        assertNotSame(date0, random.getFeature(Key.DATE1));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void getFeatureFail() {
        final Dispenser random = SmartRandom.builder().build();
        fail("Should fail but was " + random.getFeature(Key.DATE2));
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static class Key<T> extends Unique implements Dispenser.Key<T> {
        private static final Key<Date> DATE1 = new Key<>();
        private static final Key<Date> DATE2 = new Key<>();
    }
}