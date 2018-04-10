package net.team33.random;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    /**
     * Provides {@link BasicRandom} functionality through a {@link SmartRandom} instance.
     */
    @SuppressWarnings("PublicField")
    public final BasicRandom basic;

    /**
     * Provides {@link Selector} functionality through a {@link SmartRandom} instance.
     */
    @SuppressWarnings("PublicField")
    public final Selector select;

    private final Core core;
    private final Map<Generic.Compound, int[]> limits = new ConcurrentHashMap<>(0);
    private final Bounds arrayBounds = new Bounds(1, 16); // preliminary here, TODO: move to Builder/Core

    private SmartRandom(final Core core) {
        this.core = core;
        this.basic = core.newBasic.get();
        this.select = new Selector(basic);
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
        //noinspection unchecked
        return (T) any(new Generic.Compound(resultClass));
    }

    /**
     * Randomly generates an instance of a given generic type.
     * <p>
     * Typically the result is not {@code null} but may be {@code null} in some circumstances based on the
     * configuration of this {@link SmartRandom}.
     */
    public final <T> T any(final Generic<T> resultType) {
        //noinspection unchecked
        return (T) any(resultType.getCompound());
    }

    private Object any(final Generic.Compound compound) {
        return core
                .getHandling(compound)
                .strategy.apply(this);
    }

    /**
     * Randomly sets all public setters* of a given {@code target}.
     * <p>
     * A <em>setter</em> in this sense is a method whose name starts with <em>set</em>
     * and expects exactly one parameter.
     *
     * @return The (modified) target.
     */
    public final <T> T setAll(final T target) {
        Reflect.publicSetters(target.getClass()).forEach(setter -> {
            try {
                setter.invoke(target, any(setter.getParameterTypes()[0]));
            } catch (final IllegalAccessException | InvocationTargetException caught) {
                throw new IllegalStateException("cannot set <" + setter + ">", caught);
            }
        });
        return target;
    }

    /**
     * Randomly fills all non-static, non-transient fields of a given {@code target}.
     *
     * @return The (modified) target.
     */
    public final <T> T setAllFields(final T target) {
        Reflect.instanceFields(target.getClass()).forEach(field -> {
            try {
                field.set(target, any(field.getType()));
            } catch (final IllegalAccessException caught) {
                throw new IllegalStateException("cannot set <" + field + ">", caught);
            }
        });
        return target;
    }

    private Object anyArray(final Class<?> componentType) {
        final int length = arrayBounds.actual(basic);
        final Object result = Array.newInstance(componentType, length);
        for (int index = 0; index < length; ++index) {
            Array.set(result, index, any(componentType));
        }
        return result;
    }

    private Object anyUnlimited(final Handling handling) {
        // noinspection unchecked
        return handling.method.apply(this);
    }

    private Object anyLimited(final Handling handling) {
        final int[] limit = Optional.ofNullable(limits.get(handling.compound)).orElseGet(() -> {
            final int[] result = {0};
            limits.put(handling.compound, result);
            return result;
        });
        limit[0] += 1;
        try {
            return (limit[0] > handling.maxRecursionDepth)
                    ? handling.fallback
                    : anyUnlimited(handling);
        } finally {
            limit[0] -= 1;
        }
    }

    private static final class Init {

        private static final char WHITESPACE = ' ';
        private static final char ASCII_LIMIT = 128;

        private static String defaultCharset() {
            final char[] result = new char[ASCII_LIMIT - WHITESPACE];
            for (char c = WHITESPACE; c < ASCII_LIMIT; ++c) {
                result[c - WHITESPACE] = c;
            }
            return new String(result);
        }
    }

    @SuppressWarnings("rawtypes")
    private static class Handling {

        private final Generic.Compound compound;
        private final Function method;
        private final int maxRecursionDepth;
        private final Object fallback;
        private final Function<SmartRandom, Object> strategy;

        private Handling(final Generic.Compound compound, final Function method,
                         final int maxRecursionDepth, final Object fallback) {
            this.compound = compound;
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
        private final Map<Generic.Compound, Handling> pool;
        private final Map<Generic.Compound, Handling> cache;
        private final UnknownHandling unknownHandling;
        private final char[] charset;

        private Core(final Builder builder) {
            newBasic = builder.newBasic;
            pool = Collections.unmodifiableMap(new HashMap<>(builder.handlings));
            cache = new ConcurrentHashMap<>(pool.size());
            charset = builder.charset.toCharArray();
            unknownHandling = builder.unknownHandling;
        }

        private Handling newDefaultHandling(final Generic.Compound compound) {
            final Class rawClass = compound.getRawClass();
            if (rawClass.isArray()) {
                return new Handling(compound, arrayFunction(rawClass), -1, null);
            } else if (rawClass.isEnum()) {
                return new Handling(compound, enumFunction(rawClass), -1, null);
            } else {
                return new Handling(compound, unknownHandling.function(rawClass), -1, null);
            }
        }

        private static <E> Function<SmartRandom, E> enumFunction(final Class<E> resultClass) {
            final E[] values = resultClass.getEnumConstants();
            return random -> random.select.anyOf(values);
        }

        private static <T> Function<SmartRandom, T> arrayFunction(final Class<T> resultClass) {
            return random -> resultClass.cast(random.anyArray(resultClass.getComponentType()));
        }

        @Override
        public final SmartRandom get() {
            return new SmartRandom(this);
        }

        public final Handling getHandling(final Generic.Compound compound) {
            return Optional.ofNullable(cache.get(compound)).orElseGet(() -> {
                final Handling result = pool.values().stream()
                        .filter(entry -> compound.equals(entry.compound))
                        .findAny()
                        .orElseGet(() -> newDefaultHandling(compound));
                cache.put(compound, result);
                return result;
            });
        }
    }

    /**
     * An instrument to prepare or build {@link SmartRandom} instances.
     * <p>
     * A {@link Builder} is mutable and hence not tread-safe.
     */
    public static class Builder {

        @SuppressWarnings("rawtypes")
        private static final Map<Class, Class> PRIME_CLASSES = Init.newPrimeClasses();

        @SuppressWarnings("rawtypes")
        private final Map<Generic.Compound, Handling> handlings = new HashMap<>(0);

        @SuppressWarnings("Convert2MethodRef")
        private Supplier<BasicRandom> newBasic = () -> new BasicRandom.Simple();
        private String charset = DEFAULT_CHARSET;
        private UnknownHandling unknownHandling = UnknownHandling.FAIL;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private Builder() {
            put(Boolean.TYPE, random -> random.basic.anyBoolean());
            put(Byte.TYPE, random -> (byte) random.basic.anyInt());
            put(Short.TYPE, random -> (short) random.basic.anyInt());
            put(Integer.TYPE, random -> random.basic.anyInt());
            put(Long.TYPE, random -> random.basic.anyLong());
            put(Float.TYPE, random -> random.basic.anyFloat());
            put(Double.TYPE, random -> random.basic.anyDouble());
            put(Character.TYPE, random -> random.select.anyOf(random.core.charset));
            put(String.class, random -> new String(random.any(char[].class)));
            put(Date.class, random -> new Date(random.basic.anyLong()));
            put(BigInteger.class, random -> BigInteger.valueOf(random.basic.anyLong()));
            put(BigDecimal.class, random -> BigDecimal.valueOf(random.basic.anyDouble()));
        }

        /**
         * Defines a special method to generate an instance of a given class
         * using a given {@link SmartRandom} instance.
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
            return put(new Generic.Compound(resultClass), method, maxRecursionDepth, fallback);
        }

        /**
         * Defines a special method to generate an instance of a given class
         * using a given {@link SmartRandom} instance.
         */
        public final <T> Builder put(final Generic<T> resultType, final Function<SmartRandom, T> method) {
            return put(resultType, method, -1, null);
        }

        /**
         * Defines a special method to generate an instance of a given generic type
         * using a given {@link SmartRandom} instance.
         * <p>
         * In contrast to {@link #put(Generic, Function)} you can specify a max recursion depth for the related class
         * and a fixed fallback value to be returned in case the maximum recursion depth is exceeded.
         * The fallback may also be {@code null}.
         */
        public final <T> Builder put(final Generic<T> resultType, final Function<SmartRandom, T> method,
                                     final int maxRecursionDepth, final T fallback) {
            return put(resultType.getCompound(), method, maxRecursionDepth, fallback);
        }

        @SuppressWarnings("rawtypes")
        private Builder put(final Generic.Compound compound, final Function method,
                            final int maxRecursionDepth, final Object fallback) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            final Consumer<Generic.Compound> putting =
                    cmp -> handlings.put(cmp, new Handling(cmp, method, maxRecursionDepth, fallback));
            putting.accept(compound);
            Optional.ofNullable(PRIME_CLASSES.get(compound.getRawClass()))
                    .map(Generic.Compound::new)
                    .ifPresent(putting);
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

        /**
         * Sets the handling of unknown data types. Default is {@link UnknownHandling#FAIL}.
         */
        public final Builder setUnknownHandling(final UnknownHandling unknownHandling) {
            this.unknownHandling = unknownHandling;
            return this;
        }

        private static final class Init {

            @SuppressWarnings("rawtypes")
            private static final Class[][] COUPLED_CLASSES = {
                    {Boolean.TYPE, Boolean.class},
                    {Byte.TYPE, Byte.class},
                    {Short.TYPE, Short.class},
                    {Integer.TYPE, Integer.class},
                    {Long.TYPE, Long.class},
                    {Float.TYPE, Float.class},
                    {Double.TYPE, Double.class},
                    {Character.TYPE, Character.class},
            };

            @SuppressWarnings("rawtypes")
            private static Map<Class, Class> newPrimeClasses() {
                final BiConsumer<HashMap<Class, Class>, Class[]> putPair = (map, pair) -> {
                    map.put(pair[0], pair[1]);
                    map.put(pair[1], pair[0]);
                };
                return Collections.unmodifiableMap(Stream
                        .of(COUPLED_CLASSES)
                        .collect(HashMap::new, putPair, Map::putAll));
            }
        }
    }

}
