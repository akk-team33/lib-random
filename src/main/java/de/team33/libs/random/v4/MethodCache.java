package de.team33.libs.random.v4;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.team33.libs.typing.v3.Type;


class MethodCache extends MethodPool {

    private final Map<Type, Function> core = new ConcurrentHashMap<>(0);
    private final MethodPool fallback;

    MethodCache(final Builder builder) {
        this.fallback = builder.fallback;
    }

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<Dispenser, T>) core.get(type)).orElseGet(() -> {
            final Function<Dispenser, T> result = fallback.get(type);
            core.put(type, result);
            return result;
        });
    }

    static class Builder {

        private MethodPool fallback = MethodPool.FAIL;

        final Builder setFallback(final MethodPool fallback) {
            this.fallback = fallback;
            return this;
        }

        final MethodCache build() {
            return new MethodCache(this);
        }
    }
}
