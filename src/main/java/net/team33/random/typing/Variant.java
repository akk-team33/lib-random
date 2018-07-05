package net.team33.random.typing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Variant {

    CLASS {
        @Override
        Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return (Class<?>) type;
        }

        @Override
        List<Type.Compound> parameters(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return Collections.emptyList();
        }
    },

    PARAMETERIZED_TYPE {
        @Override
        Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }

        @Override
        List<Type.Compound> parameters(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return Stream.of(((ParameterizedType) type).getActualTypeArguments())
                    .map(arg -> new Type.Compound(arg, map))
                    .collect(Collectors.toList());
        }
    },

    TYPE_VARIABLE {
        @Override
        Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return Optional.ofNullable(map.get(((TypeVariable<?>) type).getName()))
                    .map(Type.Compound::getRawClass)
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("<%s> is not in %s", type, map)));
        }

        @Override
        List<Type.Compound> parameters(final java.lang.reflect.Type type, final Map<String, Type.Compound> map) {
            return map.get(type.getTypeName()).getParameters();
        }
    };

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "ChainOfInstanceofChecks"})
    static Variant valueOf(final java.lang.reflect.Type type) {
        if (type instanceof Class)
            return CLASS;
        else if (type instanceof ParameterizedType)
            return PARAMETERIZED_TYPE;
        else if (type instanceof TypeVariable)
            return TYPE_VARIABLE;
        else
            throw new IllegalArgumentException("Unsupported type: " + type.getClass().getCanonicalName());
    }

    abstract Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Type.Compound> map);

    abstract List<Type.Compound> parameters(final java.lang.reflect.Type type, final Map<String, Type.Compound> map);
}
