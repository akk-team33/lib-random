package net.team33.patterns;

import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Array.set;

/**
 * Provides some methods to generate random arrays.
 * <p>
 * To get an instance use {@link RandomX.Generator#array}.
 */
public class ArrayGenerator {

    private final RandomX.Generator generator;

    ArrayGenerator(final RandomX.Generator generator) {
        this.generator = generator;
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code boolean} values.
     */
    public final boolean[] ofBoolean() {
        return (boolean[]) rawOf(Boolean.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code byte} values.
     */
    public final byte[] ofByte() {
        return (byte[]) rawOf(Byte.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code short} values.
     */
    public final short[] ofShort() {
        return (short[]) rawOf(Short.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code int} values.
     */
    public final int[] ofInt() {
        return (int[]) rawOf(Integer.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code long} values.
     */
    public final long[] ofLong() {
        return (long[]) rawOf(Long.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code float} values.
     */
    public final float[] ofFloat() {
        return (float[]) rawOf(Float.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code double} values.
     */
    public final double[] ofDouble() {
        return (double[]) rawOf(Double.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated {@code char} values.
     */
    public final char[] ofChar() {
        return (char[]) rawOf(Character.TYPE);
    }

    /**
     * Retrieves an array of random length filled with randomly generated instances of the given
     * {@code elementClass}.
     */
    public final <T> T[] of(final Class<T> elementClass) {
        //noinspection unchecked
        return (T[]) rawOf(elementClass);
    }

    final Object rawOf(final Class<?> elementClass) {
        final int length = generator.getBounds().minLength + generator.simple.nextInt(16);
        final Object result = newInstance(elementClass, length);
        for (int index = 0; index < length; ++index) {
            set(result, index, generator.next(elementClass));
        }
        return result;
    }
}
