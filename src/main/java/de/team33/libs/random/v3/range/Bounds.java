package de.team33.libs.random.v3.range;

public class Bounds {

    private static final long MAX_INT = (1L << Integer.SIZE) - 1;

    private final long span;
    private final long start;

    public Bounds(final int start, final int limit) {
        this(new Limits(start, limit));
    }

    public Bounds(final int start) {
        this(new Limits(start, MAX_INT + 1));
    }

    private Bounds(final Limits limits) {
        this.span = (limits.limit - limits.start) & MAX_INT;
        this.start = limits.start;
    }

    public final int projected(final int fullSpectral) {
        return (int) projected((fullSpectral - start) & MAX_INT);
    }

    private long projected(final long fullSpectral) {
        return (fullSpectral % span) + start;
    }

    private static final class Limits {

        private final long start;
        private final long limit;

        private Limits(final long start, final long limit) {
            this.start = start;
            this.limit = limit;
        }
    }
}
