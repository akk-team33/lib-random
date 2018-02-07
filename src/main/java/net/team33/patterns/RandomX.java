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

import static java.util.Collections.unmodifiableMap;

/**
 * A {@link RandomX} is thread-safe and immutable.
 *
 * To get an instance use {@link Builder#build()}.
 */
@SuppressWarnings({"MethodMayBeStatic", "ClassWithOnlyPrivateConstructors", "unused", "InnerClassMayBeStatic", "FieldCanBeLocal"})
public class RandomX {

    private final Functions functions;
    private final Map<Class<?>, Integer> maxDepth;
    private final Bounds bounds;

    private RandomX(final Builder builder) {
        functions = new Functions(builder);
        maxDepth = unmodifiableMap(new HashMap<>(builder.maxDepth));
        bounds = builder.bounds;
    }

    /**
     * Retrieves a new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Retrieves a new {@link Generator}.
     */
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
     * A {@link Builder} is mutable and not thread-safe.
     *
     * To get an instance use {@link RandomX#builder()}.
     */
    public static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class<?>, Function> suppliers = new HashMap<>(0);
        private final Map<Class<?>, Integer> maxDepth = new HashMap<>(0);

        private Bounds bounds = new Bounds(1, 16);

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

        /**
         * Sets the {@link Bounds} for generation of arrays and collections.
         * <p>
         * By default the values are [1..16].
         */
        public final Builder setBounds(final Bounds bounds) {
            this.bounds = bounds;
            return this;
        }

        /**
         * Retrieves a new {@link RandomX}.
         */
        public final RandomX build() {
            return new RandomX(this);
        }
    }

    /**
     * A {@link Generator} is not thread-safe but formally immutable.
     *
     * To get an Instance use {@link RandomX#generator()}.
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
        public final ArrayGenerator array = new ArrayGenerator(this);

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
                    return resultClass.cast(array.rawOf(resultClass.getComponentType()));
                } else {
                    return functions.get(resultClass).apply(this);
                }
            } finally {
                stack.remove(resultClass);
            }
        }

        final Bounds getBounds() {
            return bounds;
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
