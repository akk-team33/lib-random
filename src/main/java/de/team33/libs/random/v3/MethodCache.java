package de.team33.libs.random.v3;

import de.team33.libs.typing.v3.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class MethodCache implements MethodPool<Dispenser> {

    @SuppressWarnings("rawtypes")
    private final Map<Type, Function> cache;
    private final MethodPool<Dispenser> fallback;

    private MethodCache(final Builder builder) {
        cache = new ConcurrentHashMap<>(builder.map);
        fallback = builder.fallback;
    }

    static Builder builder(final MethodPool<Dispenser> fallback) {
        return new Builder(fallback);
    }

    @Override
    public <R> Function<Dispenser, R> get(final Type<R> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<Dispenser, R>) cache.get(type)).orElseGet(() -> {
            final Function<Dispenser, R> result = fallback.get(type);
            cache.put(type, result);
            return result;
        });
    }

    static class Builder {

        private final Map<Type, Function> map = new HashMap<>(0);
        private final MethodPool<Dispenser> fallback;

        private Builder(final MethodPool<Dispenser> fallback) {
            this.fallback = fallback;
        }

        final <R> Builder put(final Class<R> type, final Function<Dispenser, R> method) {
            return put(Type.of(type), method);
        }

        final <R> Builder put(final Type<R> type, final Function<Dispenser, R> method) {
            map.put(type, method);
            return this;
        }

        final MethodCache build() {
            return new MethodCache(this);
        }
    }
}
