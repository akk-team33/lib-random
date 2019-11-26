package de.team33.test.random.v5;

import de.team33.libs.random.v5.SmartRandom;
import de.team33.libs.typing.v3.Type;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertThat;

public class SmartRandomTest {

    private final Matchers matchers = new Matchers()
            .add(Subject.class, new Matcher<>(Subject.class));

    @Test
    public final void anyUnknown() {
        final SmartRandom random = SmartRandom.builder().build();
        try {
            final Unknown unknown = random.any(Unknown.class);
            Assert.fail("should fail but was <" + unknown + ">");
        } catch (final IllegalArgumentException caught) {
            // ok
        }
    }

    @Test
    public final void anySubject() {
        final SmartRandom random = SmartRandom.builder()
                                              .addMethod(Subject.class, rnd -> new Subject())
                                              .build();
        final Subject subject = random.any(Subject.class);
        assertThat(subject, matchers.get(Subject.class));
    }

    static class Unknown {}

    static class Subject {}

    static class Matcher<T> extends BaseMatcher<T> {

        private final Class<T> type;

        Matcher(final Class<T> type) {
            this.type = type;
        }

        @Override
        public boolean matches(final Object item) {
            return type.isInstance(item);
        }

        @Override
        public void describeTo(final Description description) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    static class Matchers {

        private final Map<Type, Matcher> matchers = new HashMap<>(0);

        final <T> Matcher<T> get(final Class<T> type) {
            return get(Type.of(type));
        }

        final <T> Matcher<T> get(final Type<T> type) {
            //noinspection unchecked
            return Optional.ofNullable((Matcher<T>) matchers.get(type))
                           .orElseThrow(() -> new IllegalArgumentException("No matcher found for " + type));
        }

        final Matchers add(final Class<Subject> type, final Matcher<Subject> matcher) {
            return add(Type.of(type), matcher);
        }

        final Matchers add(final Type<Subject> type, final Matcher<Subject> matcher) {
            matchers.put(type, matcher);
            return this;
        }
    }
}
