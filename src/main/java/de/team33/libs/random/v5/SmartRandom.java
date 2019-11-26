package de.team33.libs.random.v5;

import de.team33.libs.typing.v3.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Represents an instrument to randomly generate instances of in principle arbitrary classes.</p>
 * <p>An instance is to be assumed as NOT TREAD-SAFE even though being formally immutable!</p>
 */
public class SmartRandom {

    private final Stage stage;

    private SmartRandom(final Stage stage) {
        this.stage = stage;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves an arbitrary instance of a given type (class).
     *
     * @throws IllegalArgumentException when there is no method specified to get an instance of the given type.
     */
    public final <T> T any(Class<T> type) {
        return any(Type.of(type));
    }

    /**
     * Retrieves an arbitrary instance of a given type (definite type description).
     *
     * @throws IllegalArgumentException when there is no method specified to get an instance of the given type.
     */
    public final <T> T any(Type<T> type) {
        //noinspection unchecked
        final Function<SmartRandom, T> method = Optional
                .ofNullable((Function<SmartRandom, T>) stage.methods.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No method specified for " + type));
        return method.apply(this);
    }

    private static class Stage implements Supplier<SmartRandom> {

        private final Map<Type, Function> methods;

        private Stage(final Builder builder) {
            this.methods = new HashMap<>(builder.methods);
        }

        @Override
        public SmartRandom get() {
            return new SmartRandom(this);
        }
    }

    /**
     * <p>Represents a builder for instances of {@link SmartRandom}.</p>
     * <p></p>
     */
    public static class Builder {

        private Map<Type, Function> methods = new HashMap<>(0);

        public final <T> Builder addMethod(final Class<T> type, final Function<SmartRandom, T> method) {
            return addMethod(Type.of(type), method);
        }

        public final <T> Builder addMethod(final Type<T> type, final Function<SmartRandom, T> method) {
            methods.put(type, method);
            return this;
        }

        /**
         * Retrieves a new instance of {@link SmartRandom}.
         */
        public final SmartRandom build() {
            return this.prepare().get();
        }

        /**
         * Retrieves an immutable and thread-safe {@link Supplier} for consistent instances of {@link SmartRandom}.
         */
        public final Supplier<SmartRandom> prepare() {
            return new Stage(this);
        }
    }
}
