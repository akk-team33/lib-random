package de.team33.libs.random.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;

/**
 * <p>Represents a kind of map that allows {@linkplain Key special keys} to {@linkplain #get(Key) access} variable but
 * well-typed features.</p>
 *
 * <p>To get an instance use {@link Builder#build()} or {@link Builder#prepare()} and {@link Stage#get()}</p>
 */
public final class Features {

    private static final String NO_FEATURE = "no feature specified for key (%s)";

    @SuppressWarnings("rawtypes")
    private final Map<Key, Object> map;

    private Features(final Stage stage) {
        this.map = unmodifiableMap(
                stage.map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()))
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public final <T> T get(final Key<T> key) {
        //noinspection unchecked
        return Optional.ofNullable((T) map.get(key))
                .orElseThrow(() -> new IllegalArgumentException(String.format(NO_FEATURE, key)));
    }

    public static class Key<T> {

        private final String creation;

        public Key() {
            final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            creation = (2 < stackTrace.length)
                    ? stackTrace[2].toString()
                    : ("unknown(" + Arrays.toString(stackTrace) + ")");
        }

        @Override
        public final String toString() {
            return creation;
        }
    }

    /**
     * Represents a preliminary stage of {@link Features}.
     */
    public static final class Stage implements Supplier<Features> {

        @SuppressWarnings("rawtypes")
        private final Map<Key, Supplier> map;

        private Stage(final Builder builder) {
            this.map = unmodifiableMap(new HashMap<>(builder.map));
        }

        /**
         * Finally supplies a new instance of {@link Features}.
         */
        @Override
        public final Features get() {
            return new Features(this);
        }

        /**
         * Creates a new {@link Builder} based on this stage.
         */
        public final Builder builder() {
            return new Builder(this);
        }
    }

    /**
     * Represents a builder for {@link Features}.
     */
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Key, Supplier> map;

        private Builder() {
            map = new HashMap<>(0);
        }

        private Builder(final Stage stage) {
            map = new HashMap<>(stage.map);
        }

        public final <T> Builder set(final Key<T> key, final T feature) {
            return setup(key, () -> feature);
        }

        public final <T> Builder setup(final Key<T> key, final Supplier<T> supplier) {
            map.put(key, supplier);
            return this;
        }


        /**
         * Prepares new instances of {@link Features}. Use {@link Stage#get()} to finally get such instances.
         */
        public final Stage prepare() {
            return new Stage(this);
        }

        /**
         * Builds a new instance of {@link Features}.
         */
        public final Features build() {
            return prepare().get();
        }
    }
}
