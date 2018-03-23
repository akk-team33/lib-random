package net.team33.patterns;

import java.util.Random;

public class BasicRandom {

    private final Random backing = new Random();

    public final void anyBytes(final byte[] bytes) {
        backing.nextBytes(bytes);
    }

    public final int anyInt() {
        return backing.nextInt();
    }

    public final int anyInt(final int bound) {
        return backing.nextInt(bound);
    }

    public final long anyLong() {
        return backing.nextLong();
    }

    public final boolean anyBoolean() {
        return backing.nextBoolean();
    }

    public final float anyFloat() {
        return backing.nextFloat();
    }

    public final double anyDouble() {
        return backing.nextDouble();
    }

    public final double anyGaussian() {
        return backing.nextGaussian();
    }
}
