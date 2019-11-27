package de.team33.test.random.v5;

import java.util.Optional;
import java.util.function.Function;

public class Assert<T> {

    public static final Function<Object, String> DOUBLE = subject -> Double.class.isInstance(subject) ? null : "\n" +
            "expected: Double\n" +
            " but was: " + (null == subject ? null : subject.getClass());
    public static final Function<Object, String> NULL = subject -> null == subject ? null : "\n" +
            "expected: null\n" +
            " but was: " + subject;
    public static final Function<Object, String> NOT_NULL = subject -> null != subject ? null : "\n" +
            "expected: non-null value\n" +
            " but was: null";

    private final T subject;

    private Assert(final T subject) {
        this.subject = subject;
    }

    public static <T> Assert<T> that(final T subject) {
        return new Assert<>(subject);
    }

    public static void fail(final String message) {
        throw new AssertionError(message);
    }

    public final Assert<T> is(final Function<? super T, String> matcher) {
        Optional.ofNullable(matcher.apply(subject))
                .ifPresent(Assert::fail);
        return this;
    }
}
