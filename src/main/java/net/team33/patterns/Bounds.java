package net.team33.patterns;

public class Bounds {

    private final int lower;
    private final int upper;

    public Bounds(final int lower, final int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public final int actual(final BasicRandom random) {
        return lower + random.anyInt((upper + 1) - lower);
    }
}
