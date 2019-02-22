package de.team33.libs.random.v3.range;

public class Bounds {

    private static final long MAX_INT = Integer.MAX_VALUE;

    private final long span;
    private final long start;

    public Bounds(final int start, final int limit) {
      if (start < limit) {
        this.span = limit - start;
        this.start = start;
      } else {
        throw new IllegalArgumentException(String.format("start (%d) must be less than limit (%d)",
                                                         start, limit));
      }
    }

    public final int projected(final int fullSpectral) {
        return (int) (start + ((MAX_INT + fullSpectral) % span));
    }
}