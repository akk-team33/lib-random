package de.team33.libs.random.v3.methods;

import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

public class Aggregator<C> implements MethodPool<C> {

    @Override
    public <R> Function<C, R> get(final Type<R> type) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
