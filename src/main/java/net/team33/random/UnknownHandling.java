package net.team33.random;

import java.util.function.Function;

/**
 * Defines different approaches to unknown data types.
 */
public enum UnknownHandling {

    /**
     * Handles unknown classes so that {@link SmartRandom#any(Class)} leads to fail with exception.
     */
    FAIL {
        @Override
        public <T> Function<SmartRandom, T> function(final Class<T> resultClass) {
            return random -> {
                throw new IllegalArgumentException("no method specified for <" + resultClass + ">");
            };
        }
    },

    /**
     * Handles unknown classes so that {@link SmartRandom#any(Class)} returns {@code null}.
     */
    RETURN_NULL {
        @Override
        public <T> Function<SmartRandom, T> function(final Class<T> resultClass) {
            return random -> null;
        }
    };

    public abstract <T> Function<SmartRandom, T> function(final Class<T> resultClass);
}
