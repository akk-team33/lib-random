package de.team33.libs.random.v4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

final class Features {

    private final Map<Dispenser.Key<?>, Object> map;

    private Features(final Map<Dispenser.Key<?>, Supplier<?>> template) {
        this.map = unmodifiableMap(
                template.entrySet()
                        .stream()
                        .collect(toMap(Map.Entry::getKey, Features::getValue))
        );
    }

    private static Object getValue(final Map.Entry<Dispenser.Key<?>, ? extends Supplier<?>> entry) {
        return entry.getValue().get();
    }

    final <T> T get(final Dispenser.Key<T> key) {
        //noinspection unchecked
        return Optional
                .ofNullable((T) map.get(key))
                .orElseThrow(() -> new IllegalArgumentException("No feature specified for key: " + key));
    }

    static class Builder {

        private final Map<Dispenser.Key<?>, Supplier<?>> backing = new HashMap<>(0);

        final Supplier<Features> prepare() {
            final Map<Dispenser.Key<?>, Supplier<?>> template = new HashMap<>(backing);
            return () -> new Features(template);
        }

        final <T> void setup(final Dispenser.Key<T> key, final Supplier<T> supplier) {
            backing.put(key, supplier);
        }
    }
}
