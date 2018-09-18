package net.team33.random;

import de.team33.libs.random.v3.BasicRandom;

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
