package de.team33.libs.random.v4;

class Bounds
{

  private static final long INT_MASK = -1L >>> (Long.SIZE - Integer.SIZE);

  private final long span;

  Bounds(final int span)
  {
    final long overflow = (0 == span) ? cardinal(-1) + 1 : 0;
    this.span = cardinal(span) + overflow;
  }

  private long cardinal(final long intValue)
  {
    return intValue & INT_MASK;
  }

  int limited(final int original)
  {
    return (int)(cardinal(original) % span);
  }
}
