package de.team33.libs.random.v4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;

final class Features {

    private final Map<Dispenser.Key<?>, Supplier<?>> template;
    private final Map<Dispenser.Key<?>, Object> output;

    private Features(final Map<Dispenser.Key<?>, Supplier<?>> template) {
        this.template = template;
        this.output = new HashMap<>();
    }

    final <T> T get(final Dispenser.Key<T> key) {
        //noinspection unchecked
        return Optional
                .ofNullable((T) output.get(key))
                .orElseGet(() -> {
                    final T result = getMethod(key).get();
                    output.put(key, result);
                    return result;
                });
    }

    private <T> Supplier<T> getMethod(final Dispenser.Key<T> key) {
        //noinspection unchecked
        return Optional
                .ofNullable((Supplier<T>) template.get(key))
                .orElseThrow(() -> new IllegalArgumentException("No feature specified for key: " + key));
    }

    private static class Stage implements Supplier<Features> {

        private final Map<Dispenser.Key<?>, Supplier<?>> template;

        private Stage(final Map<Dispenser.Key<?>, Supplier<?>> template) {
            this.template = unmodifiableMap(new HashMap<>(template));
        }

        @Override
        public Features get() {
            return new Features(template);
        }
    }

    static class Builder {

        private final Map<Dispenser.Key<?>, Supplier<?>> backing = new HashMap<>(0);

        final Supplier<Features> prepare() {
            return new Stage(backing);
        }

        final <T> Builder setup(final Dispenser.Key<T> key, final Supplier<T> supplier) {
            backing.put(key, supplier);
            return this;
        }
    }
}
