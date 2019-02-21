package de.team33.libs.random.v3.methods;

import static de.team33.libs.random.v3.mapping.Primitives.normal;

import de.team33.libs.random.v3.mapping.Primitives;
import de.team33.libs.typing.v3.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class MethodCache<C> implements MethodPool<C> {

    @SuppressWarnings("rawtypes")
    private final Map<Type, Function> cache;
    private final MethodPool<C> fallback;

    private MethodCache(final Builder<C> builder) {
        cache = new ConcurrentHashMap<>(builder.map);
        fallback = builder.fallback;
    }

    public static <C> Builder<C> builder(final MethodPool<C> fallback) {
        return new Builder<>(fallback);
    }

    @Override
    public <R> Function<C, R> get(final Type<R> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<C, R>) cache.get(type)).orElseGet(() -> {
            final Function<C, R> result = fallback.get(type);
            cache.put(type, result);
            return result;
        });
    }

    public static final class Builder<C> {

        @SuppressWarnings("rawtypes")
        private final Map<Type, Function> map = new HashMap<>(0);
        private final MethodPool<C> fallback;

        private Builder(final MethodPool<C> fallback) {
            this.fallback = fallback;
        }

        public final <R> Builder<C> put(final Class<R> type, final Function<C, R> method) {
            return put(Type.of(type), method);
        }

        public final <R> Builder<C> put(final Type<R> type, final Function<C, R> method) {
            map.put(normal(type), method);
            return this;
        }

        public final MethodCache<C> build() {
            return new MethodCache<>(this);
        }
    }
}
