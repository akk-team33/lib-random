package net.team33.patterns;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An instrument to randomly generate instances of in principle arbitrary classes.
 * <p>
 * An instance is to be assumed as NOT TREAD-SAFE even though being formally immutable!
 *
 * @see #any(Class)
 * @see Builder#prepare()
 * @see Builder#build()
 * @see #builder()
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
public class SmartRandom {

    /**
     * Defines a charset to be used for generating characters and strings
     * if not otherwise specified through {@link Builder#setCharset(char[])}.
     * It consists of all ASCII characters without control characters.
     */
    public static final String DEFAULT_CHARSET = Init.defaultCharset();

    private static final class Init {

        private static final char WHITESPACE = ' ';

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private static String defaultCharset() {
            final int length = 128 - WHITESPACE;
            final char[] result = new char[length];
            for (int index = 0; index < length; ++index) {
                result[index] = (char) (WHITESPACE + index);
            }
            return new String(result);
        }
    }

    /**
     * Provides {@link BasicRandom} functionality through a {@link SmartRandom} instance.
     */
    @SuppressWarnings("PublicField")
    public final BasicRandom basic;

    /**
     * Provides {@link Selector} functionality through a {@link SmartRandom} instance.
     */
    public final Selector selector;

    private final Core core;
    private Bounds arrayBounds = new Bounds(0, 16); // preliminary here

    private SmartRandom(final Core core) {
        this.core = core;
        this.basic = core.newBasic.get();
        this.selector = new Selector(basic);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Randomly generates an instance of a given class.
     * <p>
     * Typically the result is not {@code null} but may be {@code null} in some circumstances based on the
     * configuration of this {@link SmartRandom}.
     */
    public final <T> T any(final Class<T> resultClass) {
        return core
                .getHandling(resultClass).strategy
                .apply(this);
    }

    private Object anyArray(final Class<?> componentType) {
        final int length = arrayBounds.actual(basic);
        final Object result = Array.newInstance(componentType, length);
        for (int index = 0; index < length; ++index) {
            Array.set(result, index, any(componentType));
        }
        return result;
    }

    private <T> T anyUnlimited(final Handling<T> handling) {
        return handling.method.apply(this);
    }

    private <T> T anyLimited(final Handling<T> handling) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private static class Handling<T> {

        private final Class<T> resultClass;
        private final Function<SmartRandom, T> method;
        private final int maxRecursionDepth;
        private final T fallback;
        private final Function<SmartRandom, T> strategy;

        private Handling(final Class<T> resultClass, final Function<SmartRandom, T> method,
                         final int maxRecursionDepth, final T fallback) {
            this.resultClass = resultClass;
            this.method = method;
            this.maxRecursionDepth = maxRecursionDepth;
            this.fallback = fallback;
            this.strategy = (0 > maxRecursionDepth)
                    ? random -> random.anyUnlimited(this)
                    : random -> random.anyLimited(this);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class Core implements Supplier<SmartRandom> {

        private final Supplier<BasicRandom> newBasic;
        private final Map<Class, Handling> pool;
        private final Map<Class, Handling> cache;
        private final char[] charset;

        private Core(final Builder builder) {
            newBasic = builder.newBasic;
            pool = Collections.unmodifiableMap(new HashMap<>(builder.handlings));
            cache = new ConcurrentHashMap<>(pool.size());
            charset = builder.charset.toCharArray();
        }

        @Override
        public final SmartRandom get() {
            return new SmartRandom(this);
        }

        public final <T> Handling<T> getHandling(final Class<T> resultClass) {
            return Optional.ofNullable(cache.get(resultClass)).orElseGet(() -> {
                final Handling<T> result = pool.values().stream()
                        .filter(entry -> resultClass.isAssignableFrom(entry.resultClass))
                        .findAny()
                        .orElseGet(() -> newArrayHandling(resultClass));
                cache.put(resultClass, result);
                return result;
            });
        }

        private static <T> Handling<T> newArrayHandling(final Class<T> resultClass) {
            if (resultClass.isArray()) {
                return new Handling<>(resultClass, arrayFunction(resultClass), -1, null);
            } else {
                throw new IllegalStateException("no method specified for <" + resultClass + ">");
            }
        }

        private static <T> Function<SmartRandom, T> arrayFunction(final Class<T> resultClass) {
            if (resultClass.isArray()) {
                return random -> resultClass.cast(random.anyArray(resultClass.getComponentType()));
            } else {
                throw new UnsupportedOperationException("not yet implemented");
            }
        }
    }

    /**
     * An instrument to prepare and build {@link SmartRandom} instances.
     * <p>
     * An instance is mutable and hence not tread-safe.
     */
    public static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, Handling> handlings = new HashMap<>(0);

        @SuppressWarnings("Convert2MethodRef")
        private Supplier<BasicRandom> newBasic = () -> new BasicRandom.Simple();
        private String charset = DEFAULT_CHARSET;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private Builder() {
            put(Boolean.TYPE, random -> random.basic.anyBoolean());
            put(Boolean.class, random -> random.basic.anyBoolean());
            put(Byte.TYPE, random -> (byte) random.basic.anyInt());
            put(Byte.class, random -> (byte) random.basic.anyInt());
            put(Short.TYPE, random -> (short) random.basic.anyInt());
            put(Short.class, random -> (short) random.basic.anyInt());
            put(Integer.TYPE, random -> random.basic.anyInt());
            put(Integer.class, random -> random.basic.anyInt());
            put(Long.TYPE, random -> random.basic.anyLong());
            put(Long.class, random -> random.basic.anyLong());
            put(Float.TYPE, random -> random.basic.anyFloat());
            put(Float.class, random -> random.basic.anyFloat());
            put(Double.TYPE, random -> random.basic.anyDouble());
            put(Double.class, random -> random.basic.anyDouble());
            put(Character.TYPE, random -> random.selector.next(random.core.charset));
            put(Character.class, random -> random.selector.next(random.core.charset));
        }

        /**
         * Defines a special method to generate an instance of a given class using a given {@link SmartRandom} instance.
         */
        public final <T> Builder put(final Class<T> resultClass, final Function<SmartRandom, T> method) {
            return put(resultClass, method, -1, null);
        }

        /**
         * Defines a special method to generate an instance of a given class using a given {@link SmartRandom} instance.
         * <p>
         * In contrast to {@link #put(Class, Function)} you can specify a max recursion depth for the related class
         * and a fixed fallback value to be returned in case the maximum recursion depth is exceeded.
         * The fallback may also be {@code null}.
         */
        public final <T> Builder put(final Class<T> resultClass, final Function<SmartRandom, T> method,
                                     final int maxRecursionDepth, final T fallback) {

            handlings.put(resultClass, new Handling<T>(resultClass, method, maxRecursionDepth, fallback));
            return this;
        }

        public final Builder setNewBasic(final Supplier<BasicRandom> newBasic) {
            this.newBasic = newBasic;
            return this;
        }

        /**
         * Defines a charset to be used for generating characters and strings.
         *
         * @see SmartRandom#DEFAULT_CHARSET
         */
        public final Builder setCharset(final char[] charset) {
            this.charset = new String(charset);
            return this;
        }

        /**
         * Defines a charset to be used for generating characters and strings.
         *
         * @see SmartRandom#DEFAULT_CHARSET
         */
        public final Builder setCharset(final String charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Prepares uniform but independent {@link SmartRandom} instances based on the builder's actual state.
         * <p>
         * More specifically, it builds an IMMUTABLE AND THREAD-SAFE instrument ({@link Supplier}) that can repeatedly
         * provide uniform but independent new {@link SmartRandom} instances.
         */
        public final Supplier<SmartRandom> prepare() {
            return new Core(this);
        }

        /**
         * Builds a single new {@link SmartRandom} based on the builder's actual state.
         */
        public final SmartRandom build() {
            return prepare().get();
        }
    }
}
