package de.team33.libs.random.v3;

import de.team33.libs.typing.v3.Type;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Dispenser {

    private final Template template;

    private Dispenser(final Template template) {
        this.template = template;
    }

    public final <T> T get(final Class<T> type) {
        return get(Type.of(type));
    }

    public final <T> T get(final Type<T> type) {
        final Function<Dispenser, T> method = getMethod(type);
        return method.apply(this);
    }

    private <T> Function<Dispenser, T> getMethod(final Type<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((Function<Dispenser, T>) template.methods.get(type)).orElseGet(() -> {
            final Function<Dispenser, T> result = getDefaultMethod(type);
            template.methods.put(type, result);
            return result;
        });
    }

    private <T> Function<Dispenser, T> getDefaultMethod(final Type<T> type) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public static class Template {

        private final Map<Type, Function> methods = new ConcurrentHashMap<>();
    }

    public static class Builder {
    }
}
