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
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Random} is not thread-safe but formally immutable.
 * <p>
 * To get an Instance use {@link Builder#build()}.
 */
@SuppressWarnings("ClassWithTooManyMethods")
public final class Random {

    private static final String DEFAULT_CHAR_POOL = "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ.012456789";

    /**
     * Provides some basic methods to generate random values.
     */
    @SuppressWarnings("PublicField")
    public final java.util.Random basic = new java.util.Random();

    /**
     * Provides a {@link Selector}.
     */
    @SuppressWarnings("PublicField")
    public final Selector select = new Selector();

    /**
     * Provides an {@link ArrayGenerator} using the default {@link Bounds}.
     *
     * @see #array(Bounds)
     * @see Builder#setArrayBounds(Bounds)
     */
    @SuppressWarnings("PublicField")
    public final ArrayGenerator array;

    private final Core core;

    private Random(final Core core) {
        this.core = core;
        array = new ArrayGenerator(core.arrayBounds);
    }

    /**
     * Retrieves a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves new {@link Bounds}.
     */
    public static Bounds bounds(final int min, final int max) {
        return new Bounds(min, max);
    }

    /**
     * Retrieves an {@link ArrayGenerator} using the given {@link Bounds bounds}.
     *
     * @see #array
     */
    public final ArrayGenerator array(final Bounds bounds) {
        return new ArrayGenerator(bounds);
    }

    /**
     * Retrieves a new, randomly generated instance of the given class.
     */
    public final <T> T next(final Class<T> resultClass) {
        if (isMaxDepthExceeded(Thread.currentThread().getStackTrace(), "next")) {
            return null;
        } else if (resultClass.isArray()) {
            return resultClass.cast(array.raw(resultClass.getComponentType()));
        } else {
            return core.functions.get(resultClass).apply(this);
        }
    }

    private boolean isMaxDepthExceeded(final StackTraceElement[] trace, final String methodName) {
        return Stream.of(trace)
                .filter(element -> getClass().getCanonicalName().equals(element.getClassName()))
                .filter(element -> methodName.equals(element.getMethodName()))
                .skip(core.maxDepth)
                .findAny()
                .isPresent();
    }

    public final short nextShort() {
        //noinspection NumericCastThatLosesPrecision
        return (short) basic.nextInt();
    }

    /**
     * @see java.util.Random#nextLong()
     */
    public final long nextLong() {
        return basic.nextLong();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    public final float nextFloat() {
        return basic.nextFloat();
    }

    /**
     * @see java.util.Random#nextDouble()
     */
    public final double nextDouble() {
        return basic.nextDouble();
    }

    /**
     * @see java.util.Random#nextGaussian()
     */
    public final double nextGaussian() {
        return basic.nextGaussian();
    }

    public final String nextString() {
        return nextString(core.stringBounds);
    }

    public String nextString(final Bounds bounds) {
        final int length = actual(bounds);
        final char[] result = new char[length];
        for (int i = 0; i < length; ++i) {
            result[i] = DEFAULT_CHAR_POOL.charAt(basic.nextInt(DEFAULT_CHAR_POOL.length()));
        }
        return new String(result);
    }

    private int actual(final Bounds bounds) {
        return bounds.minLength + basic.nextInt(bounds.maxLength - bounds.minLength);
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

    /**
     * A {@link Builder} is mutable and not thread-safe.
     *
     * To get an instance use {@link Random#builder()}.
     */
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, Function> suppliers = new HashMap<>(0);
        private Bounds stringBounds = bounds(1, 16);
        private Bounds arrayBounds = bounds(1, 4);
        private int maxDepth = 5;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private Builder() {
            put(Boolean.class, random -> random.basic.nextBoolean());
            put(Boolean.TYPE, random -> random.basic.nextBoolean());
            put(Byte.class, random -> (byte) random.basic.nextInt());
            put(Byte.TYPE, random -> (byte) random.basic.nextInt());
            put(Short.class, random -> (short) random.basic.nextInt());
            put(Short.TYPE, random -> (short) random.basic.nextInt());
            put(Integer.class, random -> random.basic.nextInt());
            put(Integer.TYPE, random -> random.basic.nextInt());
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

        public final Supplier<Random> build() {
            final Core core = new Core(this);
            return () -> new Random(core);
        }
    }

    private static final class Core {

        private final Functions functions;
        private final Bounds stringBounds;
        private final Bounds arrayBounds;
        private final int maxDepth;

        private Core(final Builder builder) {
            functions = new Functions(builder);
            stringBounds = builder.stringBounds;
            arrayBounds = builder.arrayBounds;
            maxDepth = builder.maxDepth;
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

    /**
     * Provides some methods to generate random arrays.
     * <p>
     * To get an instance use {@link Random#array} or {@link Random#array(Bounds)}.
     */
    public final class ArrayGenerator {

        private final Bounds bounds;

        private ArrayGenerator(final Bounds bounds) {
            this.bounds = bounds;
        }

        public final boolean[] ofBoolean() {
            return (boolean[]) raw(Boolean.TYPE);
        }

        public final byte[] ofByte() {
            return (byte[]) raw(Byte.TYPE);
        }

        public final short[] ofShort() {
            return (short[]) raw(Short.TYPE);
        }

        public final int[] ofInt() {
            return (int[]) raw(Integer.TYPE);
        }

        public final long[] ofLong() {
            return (long[]) raw(Long.TYPE);
        }

        public final float[] ofFloat() {
            return (float[]) raw(Float.TYPE);
        }

        public final double[] ofDouble() {
            return (double[]) raw(Double.TYPE);
        }

        public final char[] ofChar() {
            return (char[]) raw(Character.TYPE);
        }

        public final <T> T[] of(final Class<T> elementClass) {
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
            return pool[basic.nextInt(pool.length)];
        }

        public final byte next(final byte... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final short next(final short... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final int next(final int... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final long next(final long... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final float next(final float... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final double next(final double... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final char next(final char... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        @SafeVarargs
        public final <T> T next(final T... pool) {
            return pool[basic.nextInt(pool.length)];
        }

        public final <T> T next(final List<T> pool) {
            return pool.get(basic.nextInt(pool.size()));
        }

        public final <T> T next(final Collection<T> pool) {
            return next(new ArrayList<>(pool));
        }
    }
}
