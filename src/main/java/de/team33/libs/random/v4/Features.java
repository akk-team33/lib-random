package de.team33.libs.random.v4;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class Features {

    private final Map<Dispenser.Key<?>, Supplier<?>> template;

    private final Map<Dispenser.Key<?>, Object> map;

    private Features(final Map<Dispenser.Key<?>, Supplier<?>> template) {
        this.template = template;
        this.map = new HashMap<>();
    }

    final <T> T get(final Dispenser.Key<T> key) {
        //noinspection unchecked
        return Optional
                .ofNullable((T) map.get(key))
                .orElseGet(() -> {
                  final T result = getMethod(key).get();
                  map.put(key, result);
                  return result;
                });
    }

    private <T> Supplier<T> getMethod(final Dispenser.Key<T> key) {
      //noinspection unchecked
      return Optional
          .ofNullable((Supplier<T>) template.get(key))
          .orElseThrow(() -> new IllegalArgumentException("No feature specified for key: " + key));
    }

    static class Builder {

        private final Map<Dispenser.Key<?>, Supplier<?>> backing = new HashMap<>(0);

        final Supplier<Features> prepare() {
            final Map<Dispenser.Key<?>, Supplier<?>> template = unmodifiableMap(new HashMap<>(backing));
            return () -> new Features(template);
        }

        final <T> Builder setup(final Dispenser.Key<T> key, final Supplier<T> supplier) {
            backing.put(key, supplier);
            return this;
        }
    }
}
