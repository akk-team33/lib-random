package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

public abstract class DispenserBase implements Dispenser {

    private final MethodPool methods;

    DispenserBase(final MethodPool methods) {
        this.methods = methods;
    }

    @Override
    public <T> T get(final Type<T> type) {
        return methods.get(type).apply(this);
    }
}
