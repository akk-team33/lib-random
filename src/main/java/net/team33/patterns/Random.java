package net.team33.patterns;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("ClassWithTooManyMethods")
public final class Random {

    private static final String DEFAULT_CHAR_POOL = "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ.012456789";

    @SuppressWarnings("PublicField")
    public final Basic basic = new Basic();
    @SuppressWarnings("PublicField")
    public final Selector select = new Selector();
    @SuppressWarnings("PublicField")
    public final Array array;
    private final java.util.Random backing = new java.util.Random();
    private final Functions functions;
    private final Bounds stringBounds;
    private final int maxDepth;

    private Random(final Builder builder) {
        functions = new Functions(builder);
        stringBounds = builder.stringBounds;
        array = new Array(builder.arrayBounds);
        maxDepth = builder.maxDepth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Bounds bounds(final int min, final int max) {
        return new Bounds(min, max);
    }

    public final Array array(final Bounds bounds) {
        return new Array(bounds);
    }

    public final <T> T next(final Class<T> resultClass) {
        if (isMaxDepthExceeded(Thread.currentThread().getStackTrace(), "next")) {
            return null;
        } else if (resultClass.isArray()) {
            return resultClass.cast(array.raw(resultClass.getComponentType()));
        } else {
            return functions.get(resultClass).apply(this);
        }
    }

    private boolean isMaxDepthExceeded(final StackTraceElement[] trace, final String methodName) {
        return Stream.of(trace)
                .filter(element -> getClass().getCanonicalName().equals(element.getClassName()))
                .filter(element -> methodName.equals(element.getMethodName()))
                .skip(maxDepth)
                .findAny()
                .isPresent();
    }

    public final boolean nextBoolean() {
        return backing.nextBoolean();
    }

    public final boolean nextBoolean(final boolean[] pool) {
        return pool[nextInt(pool.length)];
    }

    public final short nextShort() {
        //noinspection NumericCastThatLosesPrecision
        return (short) backing.nextInt();
    }

    public final short nextShort(final short[] pool) {
        return pool[nextInt(pool.length)];
    }

    /**
     * @see java.util.Random#nextInt()
     */
    public final int nextInt() {
        return backing.nextInt();
    }

    /**
     * @see java.util.Random#nextInt()
     */
    public final int nextInt(final int bound) {
        return backing.nextInt(bound);
    }

    /**
     * @see java.util.Random#nextLong()
     */
    public final long nextLong() {
        return backing.nextLong();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    public final float nextFloat() {
        return backing.nextFloat();
    }

    /**
     * @see java.util.Random#nextDouble()
     */
    public final double nextDouble() {
        return backing.nextDouble();
    }

    /**
     * @see java.util.Random#nextGaussian()
     */
    public final double nextGaussian() {
        return backing.nextGaussian();
    }

    public final String nextString() {
        return nextString(stringBounds);
    }

    public String nextString(final Bounds bounds) {
        final int length = actual(bounds);
        final char[] result = new char[length];
        for (int i = 0; i < length; ++i) {
            result[i] = DEFAULT_CHAR_POOL.charAt(nextInt(DEFAULT_CHAR_POOL.length()));
        }
        return new String(result);
    }

    private int actual(final Bounds bounds) {
        return bounds.minLength + backing.nextInt(bounds.maxLength - bounds.minLength);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class Functions {

        private final Map<Class, Function> pool;
        private final Map<Class, Function> cache;

        private Functions(final Builder builder) {
            pool = Collections.unmodifiableMap(new HashMap<>(builder.suppliers));
            cache = new HashMap<>(pool.size());
        }

        private <T> Function<Random, T> get(final Class<T> resultClass) {
            synchronized (cache) {
                return Optional
                        .ofNullable(cache.get(resultClass))
                        .orElseGet(() -> find(resultClass));
            }
        }

        private Function find(final Class<?> resultClass) {
            final Function result = pool.entrySet().stream()
                    .filter(entry -> resultClass.isAssignableFrom(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("no method specified for <" + resultClass + ">"));
            cache.put(resultClass, result);
            return result;
        }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, Function> suppliers = new HashMap<>(0);
        private Bounds stringBounds = bounds(1, 16);
        private Bounds arrayBounds = bounds(1, 4);
        private int maxDepth = 5;

        private Builder() {
            put(Boolean.class, Random::nextBoolean);
            put(Boolean.TYPE, Random::nextBoolean);
            put(Byte.class, random -> (byte) random.nextInt());
            put(Byte.TYPE, random -> (byte) random.nextInt());
            put(Short.class, random -> (short) random.nextInt());
            put(Short.TYPE, random -> (short) random.nextInt());
            put(Integer.class, Random::nextInt);
            put(Integer.TYPE, Random::nextInt);
            put(Long.class, Random::nextLong);
            put(Long.TYPE, Random::nextLong);
            put(Float.class, Random::nextFloat);
            put(Float.TYPE, Random::nextFloat);
            put(Double.class, Random::nextDouble);
            put(Double.TYPE, Random::nextDouble);
            put(Character.class, random -> random.select.next(DEFAULT_CHAR_POOL.toCharArray()));
            put(Character.TYPE, random -> random.select.next(DEFAULT_CHAR_POOL.toCharArray()));
            put(String.class, Random::nextString);
            put(Date.class, random -> new Date(random.nextLong()));
            put(BigInteger.class, random -> BigInteger.valueOf(random.nextLong()));
            put(BigDecimal.class, random -> BigDecimal.valueOf(random.nextDouble()));
        }

        public final <T> Builder put(final Class<T> resultClass, final Function<Random, T> function) {
            suppliers.put(resultClass, function);
            return this;
        }

        /**
         * Specifies the {@link Bounds} to be used by {@link Random#nextString()}
         * (in contrast to {@link Random#nextString(Bounds)}).
         */
        public final Builder setStringBounds(final Bounds bounds) {
            stringBounds = bounds;
            return this;
        }

        public final Builder setArrayBounds(final Bounds bounds) {
            arrayBounds = bounds;
            return this;
        }

        public final Builder setMaxDepth(final int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public final Random build() {
            return new Random(this);
        }
    }

    /**
     * Use {@link Random#bounds(int, int)} to get an instance.
     */
    public static final class Bounds {

        public final int minLength;
        public final int maxLength;

        private Bounds(final int minLength, final int maxLength) {
            this.minLength = minLength;
            this.maxLength = maxLength;
        }
    }

    public class Basic {
    }

    public final class Array {

        private final Bounds bounds;

        private Array(final Bounds bounds) {
            this.bounds = bounds;
        }

        public final boolean[] nextBoolean() {
            return (boolean[]) raw(Boolean.TYPE);
        }

        public final byte[] nextByte() {
            return (byte[]) raw(Byte.TYPE);
        }

        public final short[] nextShort() {
            return (short[]) raw(Short.TYPE);
        }

        public final int[] nextInt() {
            return (int[]) raw(Integer.TYPE);
        }

        public final long[] nextLong() {
            return (long[]) raw(Long.TYPE);
        }

        public final float[] nextFloat() {
            return (float[]) raw(Float.TYPE);
        }

        public final double[] nextDouble() {
            return (double[]) raw(Double.TYPE);
        }

        public final char[] nextChar() {
            return (char[]) raw(Character.TYPE);
        }

        public final <T> T[] next(final Class<T> elementClass) {
            //noinspection unchecked
            return (T[]) raw(elementClass);
        }

        @SuppressWarnings("unchecked")
        private Object raw(final Class<?> elementClass) {
            final int length = actual(bounds);
            final Object result = java.lang.reflect.Array.newInstance(elementClass, length);
            for (int index = 0; index < length; ++index) {
                java.lang.reflect.Array.set(result, index, Random.this.next(elementClass));
            }
            return result;
        }
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public class Selector {

        public final boolean next(final boolean... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final byte next(final byte... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final short next(final short... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final int next(final int... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final long next(final long... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final float next(final float... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final double next(final double... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final char next(final char... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        @SafeVarargs
        public final <T> T next(final T... pool) {
            return pool[backing.nextInt(pool.length)];
        }

        public final <T> T next(final List<T> pool) {
            return pool.get(backing.nextInt(pool.size()));
        }

        public final <T> T next(final Collection<T> pool) {
            return next(new ArrayList<>(pool));
        }
    }
}
