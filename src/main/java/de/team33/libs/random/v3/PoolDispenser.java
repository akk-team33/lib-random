package de.team33.libs.random.v3;

import static de.team33.libs.random.v3.mapping.Primitives.normal;

import de.team33.libs.random.v3.methods.MethodPool;
import de.team33.libs.typing.v3.Type;


/**
 * Basic implementation of a dispenser of arbitrary instances of virtually any but defined types.
 */
public abstract class PoolDispenser<C> {

    protected abstract MethodPool<C> getMethods();

    protected abstract C getContext();

    public final <T> T get(final Class<T> type) {
        return get(Type.of(type));
    }

    public final <T> T get(final Type<T> type) {
        return getMethods().get(normal(type))
                           .apply(getContext());
    }
}
