package de.team33.libs.random.v4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

final class MultiCase<T, R> implements Function<T, R> {

    private final List<Entry<T, R>> cases;
    private final Function<T, R> fallback;

    private MultiCase(final Builder<T, R> builder) {
        this.cases = Collections.unmodifiableList(new ArrayList<>(builder.cases));
        this.fallback = builder.fallback;
    }

    @Override
    public final R apply(final T t) {
        return cases.stream()
                .filter(entry -> entry.filter.test(t))
                .findFirst()
                .map(entry -> entry.function)
                .orElse(fallback)
                .apply(t);
    }

    private static final class Entry<T, R> {

        private final Predicate<T> filter;
        private final Function<T, R> function;

        private Entry(final Predicate<T> filter, final Function<T, R> function) {
            this.filter = filter;
            this.function = function;
        }
    }

    @SuppressWarnings("FieldHasSetterButNoGetter")
    static class Builder<T, R> {

        private final List<Entry<T, R>> cases = new LinkedList<>();
        private Function<T, R> fallback = t -> {
            throw new IllegalArgumentException("No case specified for " + t);
        };

        final Builder<T, R> add(final Predicate<T> filter, final Function<T, R> function) {
            cases.add(new Entry<>(filter, function));
            return this;
        }

        final Builder<T, R> setFallback(final Function<T, R> fallback) {
            this.fallback = fallback;
            return this;
        }

        final MultiCase<T, R> build() {
            return new MultiCase<T, R>(this);
        }
    }
}
