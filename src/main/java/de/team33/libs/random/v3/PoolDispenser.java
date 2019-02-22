package de.team33.libs.random.v3;

import static de.team33.libs.random.v3.mapping.Primitives.normal;

import de.team33.libs.random.v3.methods.MethodCache;
import de.team33.libs.random.v3.methods.MethodPool;
import de.team33.libs.typing.v3.Type;


/**
 * Implementation of a dispenser that uses a {@link MethodPool}.
 */
public class PoolDispenser<C> implements Dispenser {

    private final MethodPool<C> methods;
    private final C context;

    public PoolDispenser(final MethodPool<C> methods, final C context)
    {
        this.methods = methods;
        this.context = context;
    }

  public PoolDispenser(final MethodCache<C> methods)
  {
    this.methods = methods;
    this.context = (C) this;
  }

  @Override
    public final <T> T get(final Type<T> type) {
        return methods.get(normal(type))
                      .apply(context);
    }
}
