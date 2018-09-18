package net.team33.random;

import de.team33.libs.typing.v1.DefType;

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
        public <T> Function<SmartRandom, T> function(final DefType<?> setup) {
            return random -> {
                throw new IllegalArgumentException("no method specified for " + setup);
            };
        }
    },

    /**
     * Handles unknown classes so that {@link SmartRandom#any(Class)} returns {@code null}.
     */
    RETURN_NULL {
        @Override
        public <T> Function<SmartRandom, T> function(final DefType<?> setup) {
            return random -> null;
        }
    };

    public abstract <T> Function<SmartRandom, T> function(final DefType<?> setup);
}
