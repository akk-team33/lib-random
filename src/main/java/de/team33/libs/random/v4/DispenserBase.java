package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

public class DispenserBase implements Dispenser {

    private final Methods methods;
    private final Features features;

    DispenserBase(final Methods methods, final Features features) {
        this.methods = methods;
        this.features = features;
    }

    @Override
    public final <T> T get(final Type<T> type) {
        return methods.get(type).apply(this);
    }

    @Override
    public final <T> T getFeature(final Key<T> key) {
        return features.get(key);
    }
}
