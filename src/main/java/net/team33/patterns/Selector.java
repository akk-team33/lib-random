package net.team33.patterns;

import java.util.List;

@SuppressWarnings("OverloadedVarargsMethod")
public class Selector {

    private final BasicRandom basic;

    public Selector(final BasicRandom basic) {
        this.basic = basic;
    }

    public final boolean next(final boolean... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final byte next(final byte... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final short next(final short... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final int next(final int... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final long next(final long... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final float next(final float... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final double next(final double... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final char next(final char... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    @SafeVarargs
    public final <T> T next(final T... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final <T> T next(final List<T> pool) {
        return pool.get(basic.anyInt(pool.size()));
    }
}
