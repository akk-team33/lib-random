package de.team33.libs.random.v3.methods;

import java.util.function.Function;

import de.team33.libs.random.v3.Dispenser;
import de.team33.libs.random.v3.range.Bounds;
import de.team33.libs.typing.v3.Type;


public class EnumMethodPool<C extends Dispenser<C>> implements MethodPool<C>
{

  private final Dispenser<?> dispenser;

  private final MethodPool<C> fallback;

  public EnumMethodPool(final Dispenser<?> dispenser, final MethodPool<C> fallback)
  {
    this.dispenser = dispenser;
    this.fallback = fallback;
  }

  @Override
  public <R> Function<C, R> get(final Type<R> type)
  {
    final Class<R> underlyingClass = (Class<R>)type.getUnderlyingClass();
    if (underlyingClass.isEnum())
    {
      final R[] values = underlyingClass.getEnumConstants();
      final Bounds bounds = new Bounds(0, values.length);
      return ctx -> {
        final R result = values[bounds.projected(ctx.get(int.class))];
        return result;
      };
    }
    else
    {
      return fallback.get(type);
    }
  }
}
