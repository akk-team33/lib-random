package de.team33.libs.random.v3;

import java.util.Random;

public interface BasicRandom {

    @SuppressWarnings({"AnonymousInnerClassWithTooManyMethods", "OverlyComplexAnonymousInnerClass"})
    static BasicRandom simple() {
        return new BasicRandom() {

            private final Random backing = new Random();

            @Override
            public final byte[] anyBytes(final int length) {
                final byte[] result = new byte[length];
                backing.nextBytes(result);
                return result;
            }

            @Override
            public final int anyInt() {
                return backing.nextInt();
            }

            @Override
            public final int anyInt(final int bound) {
                return backing.nextInt(bound);
            }

            @Override
            public final long anyLong() {
                return backing.nextLong();
            }

            @Override
            public final boolean anyBoolean() {
                return backing.nextBoolean();
            }

            @Override
            public final float anyFloat() {
                return backing.nextFloat();
            }

            @Override
            public final double anyDouble() {
                return backing.nextDouble();
            }

            @Override
            public final double anyGaussian() {
                return backing.nextGaussian();
            }
        };
    }

    byte[] anyBytes(final int length);

    int anyInt();

    int anyInt(final int bound);

    long anyLong();

    boolean anyBoolean();

    float anyFloat();

    double anyDouble();

    double anyGaussian();
}
