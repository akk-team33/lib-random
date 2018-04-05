package net.team33.random;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

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

    private static final Map<Class<?>, Set<Field>> FIELD_CACHE = new ConcurrentHashMap<>(0);
    private static final Predicate<Field> FIELD_FILTER = field -> {
        final int modifiers = field.getModifiers();
        return !(isStatic(modifiers) || isTransient(modifiers));
    };

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
    private final Map<Class<?>, int[]> limits = new ConcurrentHashMap<>(0);
    private final Bounds arrayBounds = new Bounds(0, 16); // preliminary here, TODO: move to Builder/Core

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
        return core
                .getHandling(resultClass).strategy
                .apply(this);
    }

    /**
     * Randomly fills all non-static, non-transient fields of a given {@code target} and returns it.
     */
    public final <T> T fillFields(final T target) {
        instanceFields(target.getClass()).forEach(field -> {
            try {
                field.set(target, any(field.getType()));
            } catch (final IllegalAccessException caught) {
                throw new IllegalStateException("cannot set <" + field + ">", caught);
            }
        });
        return target;
    }

    private static Stream<Field> instanceFields(final Class<?> targetClass) {
        return Optional.ofNullable(FIELD_CACHE.get(targetClass)).orElseGet(() -> {
            final Set<Field> result = declaredFields(targetClass)
                    .filter(FIELD_FILTER)
                    .peek(field -> field.setAccessible(true))
                    .collect(Collectors.toSet());
            FIELD_CACHE.put(targetClass, result);
            return result;
        }).stream();
    }

    private static Stream<Field> declaredFields(final Class<?> targetClass) {
        return (null == targetClass) ? Stream.empty() : Stream.concat(
                Stream.of(targetClass.getDeclaredFields()),
                declaredFields(targetClass.getSuperclass()));
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
        final int[] limit = Optional.ofNullable(limits.get(handling.resultClass)).orElseGet(() -> {
            final int[] result = {0};
            limits.put(handling.resultClass, result);
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

        private static <T> Handling<T> newDefaultHandling(final Class<T> resultClass) {
            if (resultClass.isArray()) {
                return new Handling<>(resultClass, arrayFunction(resultClass), -1, null);
            } else if (resultClass.isEnum()) {
                return new Handling<>(resultClass, enumFunction(resultClass), -1, null);
            } else {
                throw new IllegalStateException("no method specified for <" + resultClass + ">");
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

        public final <T> Handling<T> getHandling(final Class<T> resultClass) {
            return Optional.ofNullable(cache.get(resultClass)).orElseGet(() -> {
                final Handling<T> result = pool.values().stream()
                        .filter(entry -> resultClass.isAssignableFrom(entry.resultClass))
                        .findAny()
                        .orElseGet(() -> newDefaultHandling(resultClass));
                cache.put(resultClass, result);
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
        private final Map<Class, Handling> handlings = new HashMap<>(0);
        @SuppressWarnings("Convert2MethodRef")
        private Supplier<BasicRandom> newBasic = () -> new BasicRandom.Simple();
        private String charset = DEFAULT_CHARSET;

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
            @SuppressWarnings({"unchecked", "rawtypes"})
            final Consumer<Class> rawPut =
                    aClass -> handlings.put(aClass, new Handling(aClass, method, maxRecursionDepth, fallback));
            rawPut.accept(resultClass);
            Optional.ofNullable(PRIME_CLASSES.get(resultClass)).ifPresent(rawPut);
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
