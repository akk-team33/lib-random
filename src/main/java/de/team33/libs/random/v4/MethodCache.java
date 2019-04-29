package de.team33.libs.random.v4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.team33.libs.typing.v3.Type;


class MethodCache extends Methods
{
    private static final Map<Class, Class> NORMAL = new HashMap<Class, Class>() {{
      put(boolean.class, Boolean.class);
    }};

    private final Map<Type, Function> core;
    private final Methods fallback;

    private MethodCache(final Builder builder) {
        this.fallback = builder.fallback;
        this.core = new ConcurrentHashMap<>(builder.core);
    }

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        return getNormalized(normal(type));
    }

    private <T> Type<T> normal(final Type<T> type) {
      //noinspection unchecked
      return Optional.ofNullable((Class<T>) NORMAL.get(type.getUnderlyingClass()))
                .map(Type::of)
                .orElse(type);
    }

    private <T> Function<Dispenser, T> getNormalized(final Type<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<Dispenser, T>) core.get(type)).orElseGet(() -> {
            final Function<Dispenser, T> result = fallback.get(type);
            core.put(type, result);
            return result;
        });
    }

    static class Builder {

        private Methods fallback = Methods.FAIL;
        private Map<Type, Function> core = new HashMap<>(0);

        final Builder setFallback(final Methods fallback) {
            this.fallback = fallback;
            return this;
        }

        final <T> Builder put(final Type<T> type, final Function<Dispenser, T> method) {
            core.put(type, method);
            return this;
        }

        final MethodCache build() {
            return new MethodCache(this);
        }
    }
}
