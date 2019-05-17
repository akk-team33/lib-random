package de.team33.libs.random.v4;

import java.lang.reflect.Array;
import java.util.function.Function;

import de.team33.libs.typing.v3.Type;


class Methods4Arrays extends Methods {

    private static final Bounds BOUNDS = new Bounds(1, 8);

  private final Methods fallback;

    Methods4Arrays(final Methods fallback) {
        this.fallback = fallback;
    }

    @SuppressWarnings("unchecked")
    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
      if (type.getUnderlyingClass().isArray())
      {
        final Class<?> componentType = type.getUnderlyingClass().getComponentType();
        return dsp -> {
          final int length = BOUNDS.limited(dsp.any(int.class));
          final Object result = Array.newInstance(componentType, length);
          for ( int index = 0; index < length; ++index )
          {
            Array.set(result, index, dsp.any(type.getActualParameters().get(0)));
          }
          return (T)result;
        };
      }
      else
      {
        return fallback.get(type);
      }
    }
}
