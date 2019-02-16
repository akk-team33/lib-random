package de.team33.libs.random.v3.methods;

import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

public class MethodFault<C> implements MethodPool<C> {

    @SuppressWarnings("rawtypes")
    private static final MethodFault INSTANCE = new MethodFault();

    public static <C> MethodFault<C> instance() {
        //noinspection unchecked
        return INSTANCE;
    }

    @Override
    public final <R> Function<C, R> get(final Type<R> type) {
        throw new IllegalArgumentException("no method found for type " + type);
    }
}
