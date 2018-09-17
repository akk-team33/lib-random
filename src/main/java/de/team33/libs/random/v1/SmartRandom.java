package de.team33.libs.random.v1;

import de.team33.libs.typing.v2.TypeDef;
import de.team33.libs.typing.v2.TypeDesc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * An instrument for the random instantiation of (in principle) any type.
 * <p>
 * An instance is to be assumed as NOT THREAD-SAFE, although it is formally immutable!
 *
 * @see #any(Class)
 * @see Builder#prepare()
 * @see Builder#build()
 * @see #builder()
 */
public final class SmartRandom {

    @SuppressWarnings("rawtypes")
    private final Map<TypeDesc, BiFunction> methods;

    private SmartRandom(final Template template) {
        methods = template.methods;
    }

    public final <T> T any(final Class<T> resultClass) {
        return any(TypeDef.of(resultClass));
    }

    public final <T> T any(final TypeDef<T> resultType) {
        //noinspection unchecked
        return (T) method(resultType).apply(this, resultType);
    }

    private BiFunction<SmartRandom, TypeDesc, ?> method(final TypeDesc resultType) {
        //noinspection unchecked
        return Optional.ofNullable(methods.get(resultType))
                .orElseThrow(() -> new IllegalArgumentException("No method specified for " + resultType));
    }

    public final ChargedRandom getCharged(final TypeDesc resultType) {
        return new ChargedRandom(resultType, type -> method(type).apply(this, resultType));
    }

    public static final class Template implements Supplier<SmartRandom> {

        @SuppressWarnings("rawtypes")
        private final Map<TypeDesc, BiFunction> methods;

        private Template(final Builder builder) {
            methods = new ConcurrentHashMap<>(builder.methods);
        }

        @Override
        public final SmartRandom get() {
            return new SmartRandom(this);
        }

        public final Builder rebuild() {
            return new Builder(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * An instrument to prepare or build {@link SmartRandom} instances.
     * <p>
     * A {@link Builder} is mutable and hence not tread-safe.
     */
    @SuppressWarnings("FieldHasSetterButNoGetter")
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private static final Map<TypeDesc, List> ALIASES = Init.aliases();

        @SuppressWarnings("rawtypes")
        private final Map<TypeDesc, BiFunction> methods;

        private Builder(final Template template) {
            methods = new HashMap<>(template.methods);
        }

        private Builder() {
            methods = new HashMap<>(0);
        }

        public final Template prepare() {
            return new Template(this);
        }

        public final SmartRandom build() {
            return prepare().get();
        }

        public final <T> Builder put(final Class<T> resultClass, final BiFunction<SmartRandom, TypeDef<T>, T> method) {
            return put(TypeDef.of(resultClass), method);
        }

        public final <T> Builder put(final TypeDef<T> resultType, final BiFunction<SmartRandom, TypeDef<T>, T> method) {
            aliases(resultType)
                    .forEach(alias -> methods.put(alias, method));
            return this;
        }

        @SuppressWarnings("unchecked")
        private static <T> List<TypeDef<T>> aliases(final TypeDef<T> resultType) {
            return Optional.ofNullable(ALIASES.get(resultType))
                    .orElseGet(() -> singletonList(resultType));
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static final class Init {

            private static final Class<?>[][] PRIMITIVES = {
                    {Boolean.TYPE, Boolean.class},
                    {Byte.TYPE, Byte.class},
                    {Short.TYPE, Short.class},
                    {Integer.TYPE, Integer.class},
                    {Long.TYPE, Long.class},
                    {Float.TYPE, Float.class},
                    {Double.TYPE, Double.class},
                    {Character.TYPE, Character.class}
            };

            private static Map<TypeDesc, List> aliases() {
                final Map<TypeDesc, List<TypeDesc>> result = new HashMap<>(2);
                Stream.of(PRIMITIVES)
                        .map(classes -> toList(TypeDef.of(classes[0]), TypeDef.of(classes[1])))
                        .forEach(types -> {
                            result.put(types.get(0), types);
                            result.put(types.get(1), types);
                        });
                return unmodifiableMap(result);
            }

            private static List<TypeDesc> toList(final TypeDesc... types) {
                return unmodifiableList(asList(types));
            }
        }
    }
}
