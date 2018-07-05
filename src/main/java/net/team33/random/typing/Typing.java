package net.team33.random.typing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;

@SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor", "AbstractClassWithoutAbstractMethods", "unused"})
public abstract class Typing<T> {

    private final TypeSetup setup;

    protected Typing() {
        try {
            setup = new TypeSetup(typeArgument(getClass()), Collections.emptyMap());
        } catch (final RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Typing(final Class<T> aClass) {
        setup = new TypeSetup(aClass);
    }

    private static Type typeArgument(final Class<?> thisClass) {
        final Type type = thisClass.getGenericSuperclass();
        return direct(parameterized(type)).getActualTypeArguments()[0];
    }

    private static ParameterizedType direct(final ParameterizedType parameterized) {
        if (Typing.class.equals(parameterized.getRawType())) {
            return parameterized;
        } else {
            throw new IllegalArgumentException("The type of an instance must be derived directly from <Typing>");
        }
    }

    private static ParameterizedType parameterized(final Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        } else {
            throw new IllegalArgumentException(
                    "The type of an instance must be a concretely parameterized derivation of <Typing>");
        }
    }

    public static <T> Typing<T> of(final Class<T> aClass) {
        return new Typing<T>(aClass) {
        };
    }

    public final TypeSetup getSetup() {
        return setup;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Typing<?>) && setup.equals(((Typing<?>) obj).setup));
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
