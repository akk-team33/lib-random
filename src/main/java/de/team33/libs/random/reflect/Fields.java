package de.team33.libs.random.reflect;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.stream.Stream;

public enum Fields implements Function<Class<?>, Stream<Field>> {

    FLAT {
        @Override
        public Stream<Field> apply(final Class<?> subject) {
            return Stream.of(subject.getDeclaredFields());
        }
    },

    DEEP {
        @Override
        public Stream<Field> apply(final Class<?> subject) {
            return (null == subject)
                    ? Stream.empty()
                    : Stream.concat(apply(subject.getSuperclass()), Stream.of(subject.getDeclaredFields()));
        }
    }
}
