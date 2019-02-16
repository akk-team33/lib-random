package de.team33.libs.random.v3;

import de.team33.libs.typing.v3.Type;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ClassWithOnlyPrivateConstructors")
public class ChargedRandom {

    private final Template template;

    public final BasicRandom basics;

    private ChargedRandom(final Template template) {
        this.template = template;
        this.basics = template.basics.get();
    }

    public static ChargedRandom instance() {
        return builder().build();
    }

    public static Template template() {
        return builder().prepare();
    }

    public static Builder builder() {
        return new Builder()
                .put(boolean.class, type -> rnd -> rnd.basics.anyBoolean());
    }

    public final <T> T get(final Class<T> type) {
        return get(Type.of(type));
    }

    public final <T> T get(final Type<T> type) {
        return getMethod(type).apply(this);
    }

    private <T> Function<ChargedRandom, T> getMethod(final Type<T> type) {
        //noinspection unchecked
        return Optional.ofNullable(template.methods.get(type))
                .orElseGet(() -> {
                    final Function<ChargedRandom, T> result = getDefaultMethod(type);
                    template.methods.put(type, result);
                    return result;
                });
    }

    private <T> Function<ChargedRandom, T> getDefaultMethod(final Type<T> type) {
        if (type.getUnderlyingClass().isArray()) {
            return rnd -> {
                final Type<?> componentType = type.getActualParameters().get(0);
                final int length = rnd.basics.anyInt(8) + 1;
                final Object result = Array.newInstance(componentType.getUnderlyingClass(), length);
                for (int index = 0; index < length; ++index) {
                    Array.set(result, index, rnd.get(componentType));
                }
                return (T) result;
            };
        } else {
            throw new IllegalArgumentException("no method specified for type " + type);
        }
    }

    public static class Template {

        @SuppressWarnings("rawtypes")
        private final Map<Type, Function> methods;
        private final Supplier<BasicRandom> basics;

        private Template(final Builder builder) {
            methods = new ConcurrentHashMap<>(builder.methods);
            basics = builder.basics;
        }

        public final ChargedRandom get() {
            return new ChargedRandom(this);
        }

        public final Builder builder() {
            return new Builder(this);
        }
    }

    public static class Builder {

        private static final Map<Type<?>, List<Type<?>>> ALTERNATIVES = Statics.alternatives();

        @SuppressWarnings("rawtypes")
        private final Map<Type, Function> methods;
        @SuppressWarnings("InnerClassReferencedViaSubclass")
        private Supplier<BasicRandom> basics = BasicRandom::simple;

        private Builder() {
            methods = new HashMap<>(0);
        }

        private Builder(final Template template) {
            methods = new HashMap<>(template.methods);
        }

        private static Stream<Type<?>> stream(final Type<?> type) {
            return Optional.ofNullable(ALTERNATIVES.get(type))
                    .map(Collection::stream)
                    .orElseGet(() -> Stream.of(type));
        }

        public final Template prepare() {
            return new Template(this);
        }

        public final ChargedRandom build() {
            return prepare().get();
        }

        public final <T> Builder put(final Class<T> type, final Function<Type<T>, Function<ChargedRandom, T>> method) {
            return put(Type.of(type), method);
        }

        public final <T> Builder put(final Type<T> type, final Function<Type<T>, Function<ChargedRandom, T>> method) {
            stream(type)
                    .forEach(alt -> methods.put(alt, method.apply(type)));
            return this;
        }

        public final Builder setBasics(final Supplier<BasicRandom> basics) {
            this.basics = basics;
            return this;
        }
    }

    private static final class Statics {

        private static final Class<?>[][] ALTERNATIVES = {
                {boolean.class, Boolean.class},
                {byte.class, Byte.class},
                {short.class, Short.class},
                {int.class, Integer.class},
                {long.class, Long.class},
                {float.class, Float.class},
                {double.class, Double.class},
                {char.class, Character.class}
        };

        private static Map<Type<?>, List<Type<?>>> alternatives() {
            return Stream.of(ALTERNATIVES)
                    .map(Statics::toChoices)
                    .collect(HashMap::new, Statics::addChoices, Map::putAll);
        }

        private static void addChoices(final Map<Type<?>, List<Type<?>>> result, final List<Type<?>> choices) {
            choices.forEach(choice -> result.put(choice, choices));
        }

        private static List<Type<?>> toChoices(final Class<?>[] classes) {
            return Stream.of(classes)
                    .map(Type::of)
                    .collect(Collectors.toList());
        }
    }
}
