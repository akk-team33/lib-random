package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.util.List;

public class Typex {

    public static <T> List<Type<T>> list(final Type<T> type) {
        return Types.list(type);
    }

    public static boolean isInstance(final Class<?> type, final Object any) {
        return isInstance(Type.of(type), any);
    }

    public static boolean isInstance(final Type<?> type, final Object any) {
        return list(type)
                .stream()
                .map(Type::getUnderlyingClass)
                .anyMatch(clss -> clss.isInstance(any));
    }
}
