package net.team33.random;

import de.team33.libs.random.v3.BasicRandom;
import de.team33.libs.typing.v1.DefType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;

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
@SuppressWarnings("unused")
public final class SmartRandom {

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

    @SuppressWarnings("rawtypes")
    private final Map<DefType, Function> methods;
    private final Core core;
    private final Bounds arrayBounds = new Bounds(4, 16); // preliminary here, TODO: move to Builder/Core

    private SmartRandom(final Core core) {
        this.core = core;
        this.methods = new ConcurrentHashMap<>(core.methods.size());
        this.basic = core.newBasic.get();
        this.select = new Selector(basic);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Map<String, DefType<?>> newMap(final Class<?> aClass) {
        final List<DefType<?>> actual = DefType.of(aClass)
                .getActualParameters();
        final TypeVariable<? extends Class<?>>[] formal = aClass.getSuperclass().getTypeParameters();
        final Map<String, DefType<?>> result = new HashMap<>(actual.size());
        for (int i = 0; i < actual.size(); ++i)
            result.put(formal[i].getName(), actual.get(i));
        return result;
    }

    /**
     * Randomly generates an instance of a given class.
     * <p>
     * Typically the result is not {@code null} but may be {@code null} in some circumstances based on the
     * configuration of this {@link SmartRandom}.
     */
    public final <T> T any(final Class<T> resultClass) {
        return any(DefType.of(resultClass));
    }

    /**
     * Randomly generates an instance of a given generic type.
     * <p>
     * Typically the result is not {@code null} but may be {@code null} in some circumstances based on the
     * configuration of this {@link SmartRandom}.
     */
    public final <T> T any(final DefType<T> resultType) {
        return (T) getMethod(resultType).apply(this);
    }

    /**
     * Randomly sets all public setters* of a given {@code target}.
     * <p>
     * A <em>setter</em> in this sense is a method whose name starts with <em>set</em>
     * and expects exactly one parameter.
     *
     * @return The (modified) target.
     */
    @SuppressWarnings("OverloadedVarargsMethod")
    public final <T> T setAll(final T target, final String... ignore) {
        final Collection<String> ignorable = new HashSet<>(Arrays.asList(ignore));
        return setAll(target, setter -> !ignorable.contains(setter.getName()));
    }

    @SuppressWarnings("rawtypes")
    private Function getMethod(final DefType<?> setup) {
        return Optional.ofNullable(methods.get(setup)).orElseGet(() -> {
            final Function result = core.getMethod(setup).get();
            methods.put(setup, result);
            return result;
        });
    }

    /**
     * Randomly sets all public setters* of a given {@code target}.
     * <p>
     * A <em>setter</em> in this sense is a method whose name starts with <em>set</em>
     * and expects exactly one parameter.
     *
     * @return The (modified) target.
     */
    public final <T> T setAll(final T target, final Predicate<Method> filter) {
        final DefType<?> targetType = DefType.of(target.getClass());// TODO: insufficient
        Reflect.publicSetters(target.getClass())
                .filter(filter)
                .forEach(setter -> {
                    try {
                        final Type parameterType = setter.getGenericParameterTypes()[0];
                        final Object value = any(targetType.getMemberType(parameterType));
                        setter.invoke(target, value);
                    } catch (final Exception caught) {
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
    @SuppressWarnings("OverloadedVarargsMethod")
    public final <T> T setAllFields(final T target, final String... ignore) {
        final Collection<String> ignorable = new HashSet<>(Arrays.asList(ignore));
        return setAllFields(target, field -> !ignorable.contains(field.getName()));
    }

    /**
     * Randomly fills all non-static, non-transient fields of a given {@code target}.
     *
     * @return The (modified) target.
     */
    public final <T> T setAllFields(final T target, final Predicate<Field> filter) {
        final DefType<?> targetType = DefType.of(target.getClass());// TODO: insufficient
        Reflect.instanceFields(target.getClass())
                .filter(filter)
                .forEach(field -> {
                    try {
                        final Type parameterType = field.getGenericType();
                        field.set(target, any(targetType.getMemberType(parameterType)));
                    } catch (final Exception caught) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class Core implements Supplier<SmartRandom> {

        private final Supplier<BasicRandom> newBasic;
        private final Map<DefType, Supplier> methods;
        private final UnknownHandling unknownHandling;
        private final char[] charset;

        private Core(final Builder builder) {
            newBasic = builder.newBasic;
            methods = new ConcurrentHashMap<>(builder.methods);
            charset = builder.charset.toCharArray();
            unknownHandling = builder.unknownHandling;
        }

        private static <E> Function<SmartRandom, E> enumFunction(final Class<E> resultClass) {
            final E[] values = resultClass.getEnumConstants();
            return random -> random.select.anyOf(values);
        }

        private static <T> Function<SmartRandom, T> arrayFunction(final Class<T> resultClass) {
            return random -> resultClass.cast(random.anyArray(resultClass.getComponentType()));
        }

        private static <E> Function<SmartRandom, List<E>> listFunction(final DefType<E> elmCmp) {
            return random -> Stream.generate(() -> random.any(elmCmp))
                    .limit(random.arrayBounds.actual(random.basic))
                    .collect(ArrayList::new, List::add, List::addAll);
        }

        private static <E> Function<SmartRandom, Set<E>> setFunction(final DefType<E> elmCmp) {
            return random -> Stream.<E>generate(() -> random.any(elmCmp))
                    .limit(random.arrayBounds.actual(random.basic))
                    .collect(HashSet::new, Set::add, Set::addAll);
        }

        private static <K, V> Function<SmartRandom, Map<K, V>> mapFunction(final DefType<K> keyCmp,
                                                                           final DefType<V> valCmp) {
            return random -> Stream.<K>generate(() -> random.any(keyCmp))
                    .limit(random.arrayBounds.actual(random.basic))
                    .collect(HashMap::new, (map, key) -> map.put(key, random.any(valCmp)), Map::putAll);
        }

        @Override
        public final SmartRandom get() {
            return new SmartRandom(this);
        }

        private Supplier<Function> getMethod(final DefType<?> setup) {
            return Optional.ofNullable(methods.get(setup)).orElseGet(() -> {
                final Function method = getDefaultMethod(setup);
                final Supplier<Function> result = () -> method;
                methods.put(setup, result);
                return result;
            });
        }

        @SuppressWarnings("IfStatementWithTooManyBranches")
        private Function getDefaultMethod(final DefType<?> setup) {
            final Class rawClass = setup.getUnderlyingClass();
            if (rawClass.isArray()) {
                return arrayFunction(rawClass);
            } else if (rawClass.isEnum()) {
                return enumFunction(rawClass);
            } else if (List.class.equals(rawClass) && (1 == setup.getParameters().size())) {
                return listFunction(setup.getParameters().get(0));
            } else if (Set.class.equals(rawClass) && (1 == setup.getParameters().size())) {
                return setFunction(setup.getParameters().get(0));
            } else if (Map.class.equals(rawClass) && (2 == setup.getParameters().size())) {
                return mapFunction(setup.getParameters().get(0), setup.getParameters().get(1));
            } else {
                return unknownHandling.function(setup);
            }
        }
    }

    /**
     * An instrument to prepare or build {@link SmartRandom} instances.
     * <p>
     * A {@link Builder} is mutable and hence not tread-safe.
     */
    @SuppressWarnings("FieldHasSetterButNoGetter")
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private static final Map<Class, Class> PRIME_CLASSES = Init.newPrimeClasses();

        @SuppressWarnings("rawtypes")
        private final Map<DefType, Supplier> methods = new HashMap<>(0);

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
         *
         * @see #put(Class, Supplier)
         * @see #put(DefType, Function)
         */
        public final <T> Builder put(final Class<T> resultClass, final Function<SmartRandom, T> method) {
            return put(DefType.of(resultClass), () -> method);
        }

        /**
         * Defines a special method to generate an instance of a given class
         * using a given {@link SmartRandom} instance.
         * <p>
         * In contrast to {@link #put(Class, Function)} this should be used, when the method itself is not thread-safe
         * and must be instantiated along to a {@link SmartRandom} instance.
         *
         * @see #put(Class, Function)
         * @see #put(DefType, Supplier)
         */
        public final <T> Builder put(final Class<T> resultClass, final Supplier<Function<SmartRandom, T>> supplier) {
            return put(DefType.of(resultClass), supplier);
        }

        /**
         * Defines a special method to generate an instance of a given generic type
         * using a given {@link SmartRandom} instance.
         *
         * @see #put(DefType, Supplier)
         * @see #put(Class, Function)
         */
        public final <T> Builder put(final DefType<T> resultType, final Function<SmartRandom, T> method) {
            return put(resultType, () -> method);
        }

        /**
         * Defines a special method to generate an instance of a given generic type
         * using a given {@link SmartRandom} instance.
         * <p>
         * In contrast to {@link #put(DefType, Function)} this should be used, when the method itself is not thread-safe
         * and must be instantiated along to a {@link SmartRandom} instance.
         *
         * @see #put(DefType, Function)
         * @see #put(Class, Supplier)
         */
        public final <T> Builder put(final DefType<T> resultType, final Supplier<Function<SmartRandom, T>> supplier) {
            final Consumer<DefType> put = cmp -> methods.put(cmp, supplier);
            put.accept(resultType);
            Optional.ofNullable(PRIME_CLASSES.get(resultType.getUnderlyingClass()))
                    .map(c -> DefType.of(c))
                    .ifPresent(put);
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
                return unmodifiableMap(Stream
                        .of(COUPLED_CLASSES)
                        .collect(HashMap::new, putPair, Map::putAll));
            }
        }
    }
}
