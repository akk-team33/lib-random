package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.function.Predicate;


class Methods4Arrays extends Methods {

    static final Methods4Arrays INSTANCE = new Methods4Arrays();

    private static final Bounds BOUNDS = new Bounds(1, 8);

    @SuppressWarnings("unchecked")
    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        final Class<?> componentType = type.getUnderlyingClass().getComponentType();
        return dsp -> {
            final int length = BOUNDS.limited(dsp.any(int.class));
            final Object result = Array.newInstance(componentType, length);
            for (int index = 0; index < length; ++index) {
                Array.set(result, index, dsp.any(type.getActualParameters().get(0)));
            }
            return (T) result;
        };
    }

    static boolean test(final Type<?> type) {
        return type.getUnderlyingClass().isArray();
    }
}
