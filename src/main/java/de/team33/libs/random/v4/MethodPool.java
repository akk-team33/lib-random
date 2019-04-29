package de.team33.libs.random.v4;

import java.util.function.Function;

import de.team33.libs.typing.v3.Type;

abstract class MethodPool {

    static final MethodPool FAIL = new MethodPool() {
        @Override
        final <T> Function<Dispenser, T> get(final Type<T> type) {
            throw new IllegalArgumentException("No method specified for " + type);
        }
    };

    abstract <T> Function<Dispenser, T> get(Type<T> type);
}
