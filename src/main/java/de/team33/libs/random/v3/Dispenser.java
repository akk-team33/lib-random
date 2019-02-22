package de.team33.libs.random.v3;

import static de.team33.libs.random.v3.mapping.Primitives.normal;

import de.team33.libs.random.v3.methods.MethodPool;
import de.team33.libs.typing.v3.Type;


/**
 * Abstraction of a dispenser of arbitrary instances of virtually any but defined types.
 */
public interface Dispenser {

    /**
     * Retrieves an arbitrary instance of a given type (class).
     */
    default <T> T get(Class<T> type) {
        return get(Type.of(type));
    }

    /**
     * Retrieves an arbitrary instance of a given type (definite type description).
     */
    <T> T get(Type<T> type);
}
