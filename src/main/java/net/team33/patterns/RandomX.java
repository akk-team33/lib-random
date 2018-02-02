package net.team33.patterns;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Array.set;
import static java.util.Collections.unmodifiableMap;

/**
 * A {@link RandomX} is thread-safe and immutable.
 */
@SuppressWarnings({"MethodMayBeStatic", "ClassWithOnlyPrivateConstructors", "unused", "InnerClassMayBeStatic", "FieldCanBeLocal"})
public class RandomX {

    private final Functions functions;
    private final Map<Class<?>, Integer> maxDepth;

    private RandomX(final Builder builder) {
        functions = new Functions(builder);
        maxDepth = unmodifiableMap(new HashMap<>(builder.maxDepth));
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Generator generator() {
        return new Generator();
    }

    private interface Stack {
        void add(Class<?> resultClass);

        void remove(Class<?> resultClass);

        boolean reached(Class<?> resultClass);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class Functions {

        private final Map<Class, Function> pool;
        private final Map<Class, Function> cache;

        private Functions(final Builder builder) {
            pool = unmodifiableMap(new HashMap<>(builder.suppliers));
            cache = new ConcurrentHashMap<>(pool.size());
        }

        private <T> Function<Generator, T> get(final Class<T> resultClass) {
            return Optional
                    .ofNullable(cache.get(resultClass))
                    .orElseGet(() -> find(resultClass));
        }

        private Function find(final Class<?> resultClass) {
            final Function result = pool.entrySet().stream()
                    .filter(entry -> resultClass.isAssignableFrom(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("no method specified for <" + resultClass + ">"));
            cache.put(resultClass, result);
            return result;
        }
    }

    /**
     * Provides some methods to generate random arrays.
     * To get an instance use {@link Generator#array}.
     */
    public static class Array {

        private final Generator generator;

        private Array(final Generator generator) {
            this.generator = generator;
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code boolean} values.
         */
        public final boolean[] nextBoolean() {
            return (boolean[]) nextRaw(Boolean.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code byte} values.
         */
        public final byte[] nextByte() {
            return (byte[]) nextRaw(Byte.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code short} values.
         */
        public final short[] nextShort() {
            return (short[]) nextRaw(Short.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code int} values.
         */
        public final int[] nextInt() {
            return (int[]) nextRaw(Integer.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code long} values.
         */
        public final long[] nextLong() {
            return (long[]) nextRaw(Long.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code float} values.
         */
        public final float[] nextFloat() {
            return (float[]) nextRaw(Float.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code double} values.
         */
        public final double[] nextDouble() {
            return (double[]) nextRaw(Double.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated {@code char} values.
         */
        public final char[] nextChar() {
            return (char[]) nextRaw(Character.TYPE);
        }

        /**
         * Retrieves an array of random length filled with randomly generated instances of the given
         * {@code elementClass}.
         */
        public final <T> T[] next(final Class<T> elementClass) {
            //noinspection unchecked
            return (T[]) nextRaw(elementClass);
        }

        private Object nextRaw(final Class<?> elementClass) {
            final int length = 1 + generator.simple.nextInt(16);
            final Object result = newInstance(elementClass, length);
            for (int index = 0; index < length; ++index) {
                set(result, index, generator.next(elementClass));
            }
            return result;
        }
    }

    /**
     * A {@link Builder} is mutable and not thread-safe.
     */
    public static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class<?>, Function> suppliers = new HashMap<>(0);
        private final Map<Class<?>, Integer> maxDepth = new HashMap<>(0);

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private Builder() {
            put(Boolean.class, generator -> generator.simple.nextBoolean());
            put(Boolean.TYPE, generator -> generator.simple.nextBoolean());
            put(Byte.class, generator -> (byte) generator.simple.nextInt());
            put(Byte.TYPE, generator -> (byte) generator.simple.nextInt());
            put(Short.class, generator -> (short) generator.simple.nextInt());
            put(Short.TYPE, generator -> (short) generator.simple.nextInt());
            put(Integer.class, generator -> generator.simple.nextInt());
            put(Integer.TYPE, generator -> generator.simple.nextInt());
            put(Long.class, generator -> generator.simple.nextLong());
            put(Long.TYPE, generator -> generator.simple.nextLong());
            put(Float.class, generator -> generator.simple.nextFloat());
            put(Float.TYPE, generator -> generator.simple.nextFloat());
            put(Double.class, generator -> generator.simple.nextDouble());
            put(Double.TYPE, generator -> generator.simple.nextDouble());
            put(Character.class, generator -> (char) (32 + generator.simple.nextInt(64)));
            put(Character.TYPE, generator -> (char) (32 + generator.simple.nextInt(64)));
            put(String.class, generator -> Long.toString(generator.simple.nextLong(), Character.MAX_RADIX));
            put(Date.class, generator -> new Date(generator.simple.nextLong()));
            put(BigInteger.class, generator -> BigInteger.valueOf(generator.simple.nextLong()));
            put(BigDecimal.class, generator -> BigDecimal.valueOf(generator.simple.nextDouble()));
        }

        public final <T> Builder put(final Class<T> resultClass, final Function<RandomX.Generator, T> function) {
            suppliers.put(resultClass, function);
            return this;
        }

        public final <T> Builder put(final Class<T> resultClass,
                                     final Function<RandomX.Generator, T> function,
                                     final int maxDepth) {
            suppliers.put(resultClass, function);
            return this.put(resultClass, function).setMaxDepth(resultClass, maxDepth);
        }

        /**
         * Sets the max. recursion depth for recursive structures.
         * <p>
         * By default the value is 5.
         */
        public final Builder setMaxDepth(final Class<?> resultClass, final int maxDepth) {
            this.maxDepth.put(resultClass, maxDepth);
            return this;
        }

        public final RandomX build() {
            return new RandomX(this);
        }
    }

    /**
     * A {@link Generator} is not thread-safe but formally immutable.
     */
    public class Generator {

        /**
         * Provides some basic methods to generate random values.
         */
        @SuppressWarnings("PublicField")
        public final java.util.Random simple = new Random();
        /**
         * Provides some methods to generate random arrays.
         */
        @SuppressWarnings({"PublicField", "ThisEscapedInObjectConstruction"})
        public final Array array = new Array(this);
        // private final List<Class> stack = new LinkedList<>();
        private final Stack limited = new Limited();
        private final Stack dummy = new Dummy();

        /**
         * Retrieves a randomly generated instance of the given class.
         *
         * @return the instance or {@code null}, when the max. recursion depth is reached for the given class.
         * @see Builder#put(Class, Function, int)
         * @see Builder#setMaxDepth(Class, int)
         */
        public final <T> T next(final Class<T> resultClass) {
            final Stack stack = stack(resultClass);
            stack.add(resultClass);
            try {
                if (stack.reached(resultClass)) {
                    return null;
                } else if (resultClass.isArray()) {
                    return resultClass.cast(array.nextRaw(resultClass.getComponentType()));
                } else {
                    return functions.get(resultClass).apply(this);
                }
            } finally {
                stack.remove(resultClass);
            }
        }

        private Stack stack(final Class<?> resultClass) {
            return (maxDepth.containsKey(resultClass) ? limited : dummy);
        }
    }

    private class Limited implements Stack {
        private final Map<Class<?>, int[]> core = new HashMap<>(0);

        private int[] depth(final Class<?> resultClass) {
            return Optional.ofNullable(core.get(resultClass)).orElseGet(() -> {
                final int[] result = new int[1];
                core.put(resultClass, result);
                return result;
            });
        }

        @Override
        public final void add(final Class<?> resultClass) {
            depth(resultClass)[0] += 1;
        }

        @Override
        public final void remove(final Class<?> resultClass) {
            depth(resultClass)[0] -= 1;
        }

        @Override
        public final boolean reached(final Class<?> resultClass) {
            return maxDepth.get(resultClass) < depth(resultClass)[0];
        }
    }

    private class Dummy implements Stack {
        @Override
        public final void add(final Class<?> resultClass) {
        }

        @Override
        public final void remove(final Class<?> resultClass) {
        }

        @Override
        public final boolean reached(final Class<?> resultClass) {
            return false;
        }
    }
}
