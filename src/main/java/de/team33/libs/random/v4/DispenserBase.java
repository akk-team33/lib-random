package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

public abstract class DispenserBase implements Dispenser {

    @Override
    public <T> T get(final Type<T> type) {
        return getMethod(type).apply(this);
    }

    protected abstract <T> Function<Dispenser, T> getMethod(final Type<T> type);
}
