package de.team33.libs.random.v3;

import de.team33.libs.provision.v1.Features;
import de.team33.libs.random.v3.methods.MethodPool;
import de.team33.libs.typing.v3.Type;

import static de.team33.libs.random.v3.mapping.Primitives.normal;


/**
 * Implementation of a dispenser that uses a {@link MethodPool}.
 */
public class PoolDispenser implements Dispenser {

    private final Features features;
    private final MethodPool<Dispenser> methods;

    public PoolDispenser(final MethodPool<Dispenser> methods, final Features features) {
        this.methods = methods;
        this.features = features;
    }

    @Override
    public final <T> T get(final Type<T> type) {
        return methods
                .get(normal(type))
                .apply(this);
    }

    @Override
    public final Features getFeatures() {
        return features;
    }
}
