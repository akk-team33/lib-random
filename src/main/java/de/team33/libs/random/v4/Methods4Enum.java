package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class Methods4Enum extends Methods {

    @SuppressWarnings("rawtypes")
    private static final Map<Type, List> CACHE = new ConcurrentHashMap<>(0);

    private final Methods fallback;

    Methods4Enum(final Methods fallback) {
        this.fallback = fallback;
    }

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        //noinspection unchecked
        return Optional
                .ofNullable(enumValues(type))
                .map(Methods4Enum::toMethod)
                .orElseGet(() -> fallback.get(type));
    }

    private static <T> Function<Dispenser, T> toMethod(final List<? extends T> values) {
        final Bounds bounds = new Bounds(values.size());
        return dsp -> values.get(bounds.limited(dsp.any(int.class)));
    }

    @SuppressWarnings({"OptionalContainsCollection", "unchecked", "rawtypes", "ReturnOfNull", "OverlyLongLambda"})
    private static <T> List<T> enumValues(final Type<T> type) {
        return Optional
                .ofNullable((List<T>) CACHE.get(type))
                .orElseGet(() -> Optional.ofNullable(enumClass(type))
                        .map(Class::getEnumConstants)
                        .map(Arrays::asList)
                        .map(values -> {
                            CACHE.put(type, values);
                            return values;
                        })
                        .orElse(null));
    }

    private static <T> Class<T> enumClass(final Type<T> type) {
        return type.getFormalParameters().isEmpty() ? enumClass(type.getUnderlyingClass()) : null;
    }

    @SuppressWarnings("rawtypes")
    private static <T> Class<T> enumClass(final Class type) {
        return type.isEnum() ? type : null;
    }
}
