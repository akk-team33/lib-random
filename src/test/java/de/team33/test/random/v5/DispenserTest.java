package de.team33.test.random.v5;

import de.team33.libs.random.v5.Dispenser;
import org.junit.Test;

import java.util.function.Function;

public class DispenserTest {

    private static final Function<Object, String> CONSISTENT_SUBJECT = subject -> {
        if (subject instanceof Subject) {
            return null;
        } else {
            return "\n" +
                    "expected: instance of " + Subject.class + "\n" +
                    " but was: " + ((null == subject) ? null : ("instance of " + subject.getClass()));
        }
    };

    @Test
    public final void anyUnknown() {
        final Dispenser random = Dispenser.builder().build();
        try {
            final Unknown unknown = random.any(Unknown.class);
            Assert.fail("should fail but was <" + unknown + ">");
        } catch (final IllegalArgumentException caught) {
            // ok
        }
    }

    @Test
    public final void anySubject() {
        final Dispenser random = Dispenser.builder()
                                          .addMethod(Subject.class, rnd -> new Subject())
                                          .build();
        final Subject subject = random.any(Subject.class);
        Assert.that(subject).is(CONSISTENT_SUBJECT);
    }

    static class Unknown {}

    static class Subject {}
}
