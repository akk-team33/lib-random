package de.team33.libs.random.v3.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.team33.libs.random.v3.Dispenser;
import de.team33.libs.typing.v3.Type;

public class Primitives {

    private static final Map<Type, Type> PRIMITIVES = Statics.newPrimitives();

    public static <T> Type<T> normal(final Type<T> original) {
        //noinspection unchecked
        return Optional.ofNullable(PRIMITIVES.get(original))
                       .orElse(original);
    }

    private static class Statics {

        private static final Class<?>[][] PRIMITIVES = {
            {boolean.class, Boolean.class},
            {byte.class, Byte.class},
            {short.class, Short.class},
            {int.class, Integer.class},
            {long.class, Long.class},
            {float.class, Float.class},
            {double.class, Double.class},
            {char.class, Character.class}
        };

        private static Map<Type, Type> newPrimitives()
        {
            final Map<Type, Type> result = new HashMap<>(PRIMITIVES.length);
            for ( final Class<?>[] classes : PRIMITIVES ) {
                result.put(Type.of(classes[0]), Type.of(classes[1]));
            }
            return Collections.unmodifiableMap(result);
        }
    }
}
