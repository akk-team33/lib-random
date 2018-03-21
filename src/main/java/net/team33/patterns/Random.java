package net.team33.patterns;

import java.lang.reflect.Array;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Random} is formally immutable but not thread-safe.
 *
 * @see Builder#build()
 * @see Builder#prepare()
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
    private final Map<Class<?>, int[]> recursionDepths = new ConcurrentHashMap<>(0);

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
        final Handling<T> handling = core.getHandling(resultClass);
        return (0 > handling.maxRecursionDepth)
                ? handling.method.apply(this)
                : nextLimited(handling);
    }

    private <T> T nextLimited(final Handling<T> handling) {
        final int[] recursionDepth = getRecursionDepth(handling.resultClass);
        recursionDepth[0] += 1;
        try {
            return (recursionDepth[0] > handling.maxRecursionDepth)
                    ? handling.fallback
                    : handling.method.apply(this);
        } finally {
            recursionDepth[0] -= 1;
        }
    }

    @SuppressWarnings("unchecked")
    private int[] getRecursionDepth(final Class<?> resultClass) {
        return Optional.ofNullable(recursionDepths.get(resultClass)).orElseGet(() -> {
            final int[] result = new int[1];
            recursionDepths.put(resultClass, result);
            return result;
        });
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
    private static final class Core {

        private final Map<Class, Handling> pool;
        private final Map<Class, Handling> cache;
        private final Bounds stringBounds;
        private final Bounds arrayBounds;

        private Core(final Builder builder) {
            pool = Collections.unmodifiableMap(new HashMap<>(builder.generations));
            cache = new ConcurrentHashMap<>(pool.size());
            stringBounds = builder.stringBounds;
            arrayBounds = builder.arrayBounds;
        }

        private static <T> Handling<T> newArrayHandling(final Class<T> resultClass) {
            if (resultClass.isArray()) {
                return new Handling<>(resultClass, arrayFunction(resultClass), -1, null);
            } else {
                throw new IllegalStateException("no method specified for <" + resultClass + ">");
            }
        }

        private static <T> Function<Random, T> arrayFunction(final Class<T> resultClass) {
            return random -> resultClass.cast(random.array.raw(resultClass.getComponentType()));
        }

        private <T> Handling<T> getHandling(final Class<T> resultClass) {
            return Optional.ofNullable(cache.get(resultClass)).orElseGet(() -> {
                final Handling result;
                result = pool.entrySet().stream()
                        .filter(entry -> resultClass.isAssignableFrom(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findAny()
                        .orElseGet(() -> newArrayHandling(resultClass));
                cache.put(resultClass, result);
                return result;
            });
        }
    }

    /**
     * A {@link Builder} is mutable and not thread-safe.
     * <p>
     * To get an instance use {@link Random#builder()}.
     */
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, Handling> generations = new HashMap<>(0);
        private Bounds stringBounds = bounds(1, 16);
        private Bounds arrayBounds = bounds(1, 4);

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
            put(Long.class, random -> random.basic.nextLong());
            put(Long.TYPE, random -> random.basic.nextLong());
            put(Float.class, random -> random.basic.nextFloat());
            put(Float.TYPE, random -> random.basic.nextFloat());
            put(Double.class, random -> random.basic.nextDouble());
            put(Double.TYPE, random -> random.basic.nextDouble());
            put(Character.class, random -> random.select.next(DEFAULT_CHAR_POOL.toCharArray()));
            put(Character.TYPE, random -> random.select.next(DEFAULT_CHAR_POOL.toCharArray()));
            put(String.class, Random::nextString);
            put(Date.class, random -> new Date(random.basic.nextLong()));
            put(BigInteger.class, random -> BigInteger.valueOf(random.basic.nextLong()));
            put(BigDecimal.class, random -> BigDecimal.valueOf(random.basic.nextDouble()));
        }

        /**
         * Associates a new generation method with a specific result class
         */
        public final <T> Builder put(final Class<T> resultClass, final Function<Random, T> method) {
            return put(resultClass, method, -1, null);
        }

        /**
         * Associates a new set of generation parameters with a specific result class
         */
        public final <T> Builder put(final Class<T> resultClass, final Function<Random, T> method,
                                     final int recursionDepth, final T fallback) {
            return put(new Handling<T>(resultClass, method, recursionDepth, fallback));
        }

        private <T> Builder put(final Handling<T> handling) {
            generations.put(handling.resultClass, handling);
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

        public final Random build() {
            return prepare().get();
        }

        public final Supplier<Random> prepare() {
            final Core core = new Core(this);
            return () -> new Random(core);
        }
    }

    private static final class Handling<T> {

        private final Class<T> resultClass;
        private final Function<Random, T> method;
        private final int maxRecursionDepth;
        private final T fallback;

        private Handling(final Class<T> resultClass, final Function<Random, T> method,
                         final int maxRecursionDepth, final T fallback) {
            this.resultClass = resultClass;
            this.method = method;
            this.maxRecursionDepth = maxRecursionDepth;
            this.fallback = fallback;
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
            final Object result = Array.newInstance(elementClass, length);
            for (int index = 0; index < length; ++index) {
                Array.set(result, index, next(elementClass));
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
