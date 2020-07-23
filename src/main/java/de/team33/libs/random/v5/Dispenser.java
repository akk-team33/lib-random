package de.team33.libs.random.v5;

import de.team33.libs.typing.v3.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Represents an instrument to generate instances of in principle arbitrary classes.</p>
 * <p>An instance is to be assumed as NOT TREAD-SAFE even though being formally immutable!
 * In order to obtain equivalent instances without much effort, it is appropriate to first statically define a
 * kind of factory using {@link Builder#prepare()} in order to then create independent but equivalent
 * {@link Dispenser} instances for a unique thread context when required.</p>
 */
public class Dispenser {

    private final Stage stage;

    private Dispenser(final Stage stage) {
        this.stage = stage;
    }

    public static Builder builder(final Preset ... presets) {
        //Stream.of(presets).collect(Builder::new, (b, p) -> {p.update.apply(b);}, null);
        return new Builder();
    }

    public static Builder builder(final Presets presets) {
        return new Builder();
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
        final Function<Dispenser, T> method = Optional
                .ofNullable((Function<Dispenser, T>) stage.methods.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No method specified for " + type));
        return method.apply(this);
    }

    public enum Preset {

        AUTO_PRIMITIVES(builder -> {
            builder.addMethod(boolean.class, ignored -> new Random().nextBoolean())
                   .addMethod(byte.class, ignored -> (byte) new Random().nextInt())
                   .addMethod(short.class, ignored -> (short) new Random().nextInt());
        }),
        AUTO_ARRAYS(builder -> {});

        private final Consumer<Builder> update;

        Preset(final Consumer<Builder> update) {
            this.update = update;
        }
    }

    public enum Presets {

        NONE(),
        ALL(Preset.values());

        private final Preset[] presets;

        Presets(final Preset ... presets) {
            this.presets = presets;
        }
    }

    private static class Stage implements Supplier<Dispenser> {

        private final Map<Type, Function> methods;

        private Stage(final Builder builder) {
            this.methods = new HashMap<>(builder.methods);
        }

        @Override
        public Dispenser get() {
            return new Dispenser(this);
        }
    }

    /**
     * <p>Represents an instrument to create or prepare {@link Dispenser} instances.</p>
     * <p>Use {@link #builder()} to retrieve an instance.</p>
     */
    public static class Builder {

        private Map<Type, Function> methods = new HashMap<>(0);

        public final <T> Builder addMethod(final Class<T> type, final Function<Dispenser, T> method) {
            return addMethod(Type.of(type), method);
        }

        public final <T> Builder addMethod(final Type<T> type, final Function<Dispenser, T> method) {
            methods.put(type, method);
            return this;
        }

        /**
         * Retrieves a new instance of {@link Dispenser}.
         */
        public final Dispenser build() {
            return this.prepare().get();
        }

        /**
         * Retrieves an immutable and thread-safe {@link Supplier} for consistent instances of {@link Dispenser}.
         */
        public final Supplier<Dispenser> prepare() {
            return new Stage(this);
        }
    }
}
