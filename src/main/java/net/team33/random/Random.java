package net.team33.random;

import java.math.BigInteger;

public class Random {

    private final java.util.Random backing = new java.util.Random();

    private Random(final Builder builder) {
    }

    public static Builder builder() {
        return new Builder();
    }

    public final boolean nextBoolean() {
        return next(false, true);
    }

    public final int nextInteger(final int min, final int limit) {
        new BigInteger(Integer.SIZE, backing);
        return backing.nextInt();
    }

    @SafeVarargs
    public final <T> T next(final T... values) {
        return values[backing.nextInt(values.length)];
    }

    public static class Builder {
        private Builder() {
        }

        public final Random build() {
            return new Random(this);
        }
    }
}
