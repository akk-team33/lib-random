package de.team33.libs.random.v3.methods;

import java.util.Optional;
import java.util.function.Function;

import de.team33.libs.random.v3.Dispenser;
import de.team33.libs.random.v3.range.Bounds;
import de.team33.libs.typing.v3.Type;


public class EnumMethodPool<C extends Dispenser> implements MethodPool<C>
{

  private final MethodPool<C> fallback;

  public EnumMethodPool(final MethodPool<C> fallback)
  {
    this.fallback = fallback;
  }

  @Override
  public <R> Function<C, R> get(final Type<R> type)
  {
    //noinspection unchecked
    return Optional.of(type.getUnderlyingClass())
                   .filter(Class::isEnum)
                   .map(c -> newMethod((Class<R>) c))
                   .orElseGet(() -> fallback.get(type));
  }

  private <R> Function<C, R> newMethod(final Class<R> resultClass)
  {
    final R[] values = resultClass.getEnumConstants();
    final Bounds bounds = new Bounds(0, values.length);
    return dsp -> {
      final int index = bounds.projected(dsp.get(int.class));
      return values[index];
    }; 
  }
}
