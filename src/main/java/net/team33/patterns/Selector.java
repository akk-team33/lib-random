package net.team33.patterns;

import java.util.List;

@SuppressWarnings("OverloadedVarargsMethod")
public class Selector {

    private final BasicRandom basic;

    public Selector(final BasicRandom basic) {
        this.basic = basic;
    }

    public final boolean anyOf(final boolean... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final byte anyOf(final byte... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final short anyOf(final short... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final int anyOf(final int... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final long anyOf(final long... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final float anyOf(final float... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final double anyOf(final double... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final char anyOf(final char... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    @SafeVarargs
    public final <T> T anyOf(final T... pool) {
        return pool[basic.anyInt(pool.length)];
    }

    public final <T> T anyOf(final List<T> pool) {
        return pool.get(basic.anyInt(pool.size()));
    }
}
