package net.team33.random.typing;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;

@SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor", "AbstractClassWithoutAbstractMethods", "unused"})
public abstract class Type<T> {

    private final TypeSetup setup;

    protected Type() {
        try {
            setup = new TypeSetup(typeArgument(getClass()), Collections.emptyMap());
        } catch (final RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Type(final Class<T> aClass) {
        setup = new TypeSetup(aClass);
    }

    private static java.lang.reflect.Type typeArgument(final Class<?> thisClass) {
        final java.lang.reflect.Type type = thisClass.getGenericSuperclass();
        return direct(parameterized(type)).getActualTypeArguments()[0];
    }

    private static ParameterizedType direct(final ParameterizedType parameterized) {
        if (Type.class.equals(parameterized.getRawType())) {
            return parameterized;
        } else {
            throw new IllegalArgumentException("The type of an instance must be derived directly from <Type>");
        }
    }

    private static ParameterizedType parameterized(final java.lang.reflect.Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        } else {
            throw new IllegalArgumentException(
                    "The type of an instance must be a concretely parameterized derivation of <Type>");
        }
    }

    public static <T> Type<T> of(final Class<T> aClass) {
        return new Type<T>(aClass) {
        };
    }

    public final TypeSetup getSetup() {
        return setup;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Type<?>) && setup.equals(((Type<?>) obj).setup));
    }

    @Override
    public final int hashCode() {
        return setup.hashCode();
    }

    @Override
    public final String toString() {
        return setup.toString();
    }

}
