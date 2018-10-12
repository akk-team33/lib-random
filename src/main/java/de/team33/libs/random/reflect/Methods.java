package de.team33.libs.random.reflect;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Methods implements Function<Class<?>, Stream<Method>> {

    PUBLIC_METHODS {
        @Override
        public Stream<Method> apply(final Class<?> aClass) {
            return Stream.of(aClass.getMethods());
        }
    },

    PUBLIC_SETTERS {
        @Override
        public Stream<Method> apply(final Class<?> aClass) {
            return PUBLIC_METHODS.apply(aClass)
                    .filter(method -> method.getName().startsWith("set"))
                    .filter(method -> 1 == method.getParameterCount());
        }
    }
}
