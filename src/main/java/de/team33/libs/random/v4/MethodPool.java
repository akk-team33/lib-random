package de.team33.libs.random.v4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import de.team33.libs.random.v4.proto.Selector;
import de.team33.libs.typing.v3.Type;


final class MethodPool extends Methods {

    @SuppressWarnings("rawtypes")
    private final Map<Type, Function> core;

    private MethodPool(final Builder builder) {
        this.core = new ConcurrentHashMap<>(builder.core);
    }

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<Dispenser, T>) core.get(type)).orElseGet(() -> {
            final Function<Dispenser, T> result = MethodsCombo.INSTANCE.get(type);
            core.put(type, result);
            return result;
        });
    }

    static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Type, Function> core = new HashMap<>(0);

        final <T> Builder put(final Type<T> type0, final Function<Dispenser, T> method) {
            for ( final Type<T> type : Types.list(type0) ) {
                core.put(type, method);
            }
            return this;
        }

        final MethodPool build() {
            return new MethodPool(this);
        }
    }
}
