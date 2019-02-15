package de.team33.libs.random.v3;

import java.util.function.Function;

import de.team33.libs.random.misc.Features;
import de.team33.libs.typing.v3.Type;

public abstract class Dispenser {

    private final Features features;

    Dispenser(final Features.Stage stage)
    {
        features = stage.get();
    }

    public Features getFeatures()
    {
        return features;
    }

    public final <T> T get(final Class<T> type) {
        return get(Type.of(type));
    }

    public final <T> T get(final Type<T> type) {
        return getMethod(type).apply(this);
    }

    abstract <T> Function<Dispenser, T> getMethod(final Type<T> type);
}
