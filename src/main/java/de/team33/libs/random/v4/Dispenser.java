package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;


/**
 * Abstraction of a dispenser of arbitrary instances of virtually any but defined types.
 */
public interface Dispenser {

    /**
     * Retrieves an arbitrary instance of a given type (class).
     *
     * @throws IllegalArgumentException when there is no method specified to get an instance of the given type.
     */
    <T> T any(Class<T> type);

    /**
     * Retrieves an arbitrary instance of a given type (definite type description).
     *
     * @throws IllegalArgumentException when there is no method specified to get an instance of the given type.
     */
    <T> T any(Type<T> type);

    /**
     * Retrieves an associated feature specified by a key.
     *
     * @throws IllegalArgumentException when there is no feature specified for the given key.
     */
    <T> T getFeature(Key<T> key);

    interface Key<T> {}
}
