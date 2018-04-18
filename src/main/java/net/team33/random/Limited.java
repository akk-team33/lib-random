package net.team33.random;

import java.util.function.Function;

public class Limited<T, R> implements Function<T, R> {

    private final Function<T, R> delegate;
    private final int maxDepth;
    private final R fallback;
    private int depth = 0;

    public Limited(final Function<T, R> delegate, final int maxDepth, final R fallback) {
        this.delegate = delegate;
        this.maxDepth = maxDepth;
        this.fallback = fallback;
    }

    @Override
    public final R apply(final T t) {
        depth += 1;
        try {
            return (maxDepth < depth) ? fallback : delegate.apply(t);
        } finally {
            depth -= 1;
        }
    }
}
