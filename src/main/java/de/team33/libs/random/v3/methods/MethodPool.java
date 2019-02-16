package de.team33.libs.random.v3.methods;

import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

public interface MethodPool<C> {

    <R> Function<C, R> get(Type<R> type);
}
