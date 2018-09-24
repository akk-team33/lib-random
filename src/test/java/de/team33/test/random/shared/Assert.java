package de.team33.test.random.shared;

import java.util.function.Function;
import java.util.function.Predicate;

public final class Assert {

    private Assert() {
    }

    public static <T> void that(final T result,
                                final Predicate<? super T> predicate,
                                final Function<? super T, String> failMessage) {
        if (!predicate.test(result)) {
            throw new AssertionError(failMessage.apply(result));
        }
    }
}
