package net.team33.random;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

final class Reflect {

    private static final Map<Class<?>, Set<Field>> FIELD_CACHE = new ConcurrentHashMap<>(0);
    private static final Map<Class<?>, Set<Method>> SETTER_CACHE = new ConcurrentHashMap<>(0);
    private static final Map<Class<?>, Set<Method>> GETTER_CACHE = new ConcurrentHashMap<>(0);
    private static final Predicate<Field> FIELD_FILTER = field -> {
        final int modifiers = field.getModifiers();
        return !(isStatic(modifiers) || isTransient(modifiers));
    };
    private static final Predicate<Method> GETTER_FILTER = method -> {
        if (0 == method.getParameterCount()) {
            final String name = method.getName();
            return !"getClass".equals(name) && (name.startsWith("get") || name.startsWith("is"));
        } else {
            return false;
        }
    };

    private Reflect() {
    }

    static Stream<Method> publicGetters(final Class<?> targetClass) {
        return Optional.ofNullable(GETTER_CACHE.get(targetClass)).orElseGet(() -> {
            final Set<Method> result = Stream.of(targetClass.getMethods())
                    .filter(GETTER_FILTER)
                    .peek(method -> method.setAccessible(true))
                    .collect(Collectors.toSet());
            GETTER_CACHE.put(targetClass, result);
            return result;
        }).stream();
    }

    static Stream<Method> publicSetters(final Class<?> targetClass) {
        return Optional.ofNullable(SETTER_CACHE.get(targetClass)).orElseGet(() -> {
            final Set<Method> result = Stream.of(targetClass.getMethods())
                    .filter(method -> method.getName().startsWith("set"))
                    .filter(method -> method.getParameterCount() == 1)
                    .peek(method -> method.setAccessible(true))
                    .collect(Collectors.toSet());
            SETTER_CACHE.put(targetClass, result);
            return result;
        }).stream();
    }

    static Stream<Field> instanceFields(final Class<?> targetClass) {
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
}
