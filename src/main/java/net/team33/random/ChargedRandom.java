package net.team33.random;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;

public class ChargedRandom {

    /**
     * Provides {@link BasicRandom} functionality through a {@link SmartRandom} instance.
     */
    @SuppressWarnings("PublicField")
    public final BasicRandom basic;

    /**
     * Provides {@link Selector} functionality through a {@link SmartRandom} instance.
     */
    @SuppressWarnings("PublicField")
    public final Selector select;

    private final Core core;

    private ChargedRandom(final Core core) {
        this.core = core;
        this.basic = new BasicRandom.Simple(); // TODO
        this.select = new Selector(basic);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final <T> T any(final Generic<T> type) {
        return any(type.getCompound());
    }

    private <T> T any(final Generic.Compound compound) {
        return core.<T>getMethod(compound).apply(this);
    }

    private static final class Core implements Supplier<ChargedRandom> {

        @SuppressWarnings("rawtypes")
        private final Map<Generic.Compound, Function> methods;
        @SuppressWarnings("rawtypes")
        private final Map<Generic.Compound, Function> defaults;

        private Core(final Builder builder) {
            methods = unmodifiableMap(new HashMap<>(builder.methods));
            defaults = new ConcurrentHashMap<>(0);
        }

        @Override
        public ChargedRandom get() {
            return new ChargedRandom(this);
        }

        @SuppressWarnings("unchecked")
        private <T> Function<ChargedRandom, T> getMethod(final Generic.Compound compound) {
            return Optional
                    .ofNullable(methods.get(compound))
                    .orElseGet(() -> defaultMethod(compound));
            //.orElseThrow(() -> new IllegalArgumentException("No method available for " + compound));
        }

        @SuppressWarnings("unchecked")
        private <T> Function<ChargedRandom, T> defaultMethod(final Generic.Compound compound) {
            return Optional.ofNullable(defaults.get(compound)).orElseGet(() -> {
                final Function<ChargedRandom, T> result;
                final Class<T> rawClass = compound.getRawClass();
                if (rawClass.isEnum()) {
                    final T[] values = rawClass.getEnumConstants();
                    result = rnd -> rnd.select.anyOf(values);
                } else {
                    throw new IllegalArgumentException("No method available for " + compound);
                }
                defaults.put(compound, result);
                return result;
            });
        }
    }

    public static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Generic.Compound, Function> methods = new HashMap<>(0);

        /**
         * Prepares uniform but independent {@link ChargedRandom} instances based on the builder's actual state.
         * <p>
         * More specifically, it builds an IMMUTABLE AND THREAD-SAFE instrument ({@link Supplier}) that can repeatedly
         * provide uniform but independent new {@link ChargedRandom} instances.
         */
        public final Supplier<ChargedRandom> prepare() {
            return new Core(this);
        }
    }
}
