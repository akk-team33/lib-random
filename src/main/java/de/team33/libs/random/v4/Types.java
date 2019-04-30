package de.team33.libs.random.v4;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.team33.libs.typing.v3.Type;


class Types {

    private static final Map<Type, List> MAPPING = Inner.newMapping();

    static <T> List<Type<T>> list(final Type<T> type) {
      //noinspection unchecked
      return Optional.ofNullable((List<Type<T>>) MAPPING.get(type))
                       .orElseGet(() -> singletonList(type));
    }

    private static class Inner {

        private static final Class[][] EQUIVALENT = {
            {boolean.class, Boolean.class},
            {byte.class, Byte.class},
            {short.class, Short.class},
            {int.class, Integer.class},
            {long.class, Long.class},
            {float.class, Float.class},
            {double.class, Double.class},
            {char.class, Character.class}
        };

        private static Map<Type, List> newMapping() {
            return unmodifiableMap(
              Stream.of(EQUIVALENT)
                    .map(Inner::toTypeList)
                    .collect(HashMap::new, Inner::put, Map::putAll)
            );
        }

        private static List<? extends Type> toTypeList(final Class<?>[] classes) {
            return Stream.of(classes)
                         .map(Type::of)
                         .collect(toList());
        }

        private static void put(final Map<Type, List> map, final List<? extends Type> list) {
            list.forEach(type -> map.put(type, unmodifiableList(list)));
        }
    }
}
