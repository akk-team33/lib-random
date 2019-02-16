package de.team33.libs.random.v3;

import de.team33.libs.random.misc.Features;
import de.team33.libs.random.v3.methods.MethodPool;
import de.team33.libs.typing.v3.Type;

public class Dispenser {

    private final Features features;
    private final MethodPool<Dispenser> methods;

    public Dispenser(final Features.Stage stage, final MethodPool<Dispenser> methods)
    {
        this.features = stage.get();
        this.methods = methods;
    }

    public final Features getFeatures()
    {
        return features;
    }

    public final <T> T get(final Class<T> type) {
        return get(Type.of(type));
    }

    public final <T> T get(final Type<T> type) {
        return methods.get(type).apply(this);
    }
}
