package de.team33.libs.random.v1;

import de.team33.libs.typing.v1.DefType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SuppressWarnings({"ClassWithOnlyPrivateConstructors", "MethodMayBeStatic"})
public class SmartRandom {

    @SuppressWarnings("rawtypes")
    private final Map<DefType, Function> methods;

    private SmartRandom(final Template template) {
        methods = template.methods;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final <T> T any(final Class<T> resultType) {
        return any(DefType.of(resultType));
    }

    public final <T> T any(final DefType<T> resultType) {
        return findMethod(resultType).apply(this);
    }

    @SuppressWarnings("unchecked")
    private <T> Function<SmartRandom, T> findMethod(final DefType<T> resultType) {
        return Optional.ofNullable(methods.get(resultType))
                .orElseThrow(() -> new IllegalArgumentException("No method specified for " + resultType));
    }

    public static class Template {

        @SuppressWarnings("rawtypes")
        private final Map<DefType, Function> methods;

        private Template(final Builder builder) {
            this.methods = new ConcurrentHashMap<>(builder.methods);
        }

        public final SmartRandom get() {
            return new SmartRandom(this);
        }

        public final Builder builder() {
            return new Builder(this);
        }
    }

    public static class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<DefType, Function> methods = new HashMap<>(0);

        private Builder() {
        }

        private Builder(final Template template) {
        }

        public final Template prepare() {
            return new Template(this);
        }

        public final SmartRandom build() {
            return prepare().get();
        }

        public final <T> Builder put(final Class<T> resultType,
                                     final Function<DefType<T>, Function<SmartRandom, T>> method) {
            return put(DefType.of(resultType), method);
        }

        public final <T> Builder put(final DefType<T> resultType,
                                     final Function<DefType<T>, Function<SmartRandom, T>> method) {
            methods.put(resultType, method.apply(resultType));
            return this;
        }
    }
}
