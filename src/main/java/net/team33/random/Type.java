package net.team33.random;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor", "AbstractClassWithoutAbstractMethods", "unused"})
public abstract class Type<T> {

    private final Compound compound;

    protected Type() {
        try {
            compound = new Compound(typeArgument(getClass()), Collections.emptyMap());
        } catch (final RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Type(final Class<T> aClass) {
        compound = new Compound(aClass);
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

    public final Compound getCompound() {
        return compound;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Type<?>) && compound.equals(((Type<?>) obj).compound));
    }

    @Override
    public final int hashCode() {
        return compound.hashCode();
    }

    @Override
    public final String toString() {
        return compound.toString();
    }

    private enum Spec {

        CLASS {
            @Override
            Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return (Class<?>) type;
            }

            @Override
            List<Compound> parameters(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return Collections.emptyList();
            }
        },

        PARAMETERIZED_TYPE {
            @Override
            Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            }

            @Override
            List<Compound> parameters(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return Stream.of(((ParameterizedType) type).getActualTypeArguments())
                        .map(arg -> new Compound(arg, map))
                        .collect(Collectors.toList());
            }
        },

        TYPE_VARIABLE {
            @Override
            Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return Optional.ofNullable(map.get(((TypeVariable<?>) type).getName()))
                        .map(Compound::getRawClass)
                        .orElseThrow(() -> new IllegalStateException(
                                String.format("<%s> is not in %s", type, map)));
            }

            @Override
            List<Compound> parameters(final java.lang.reflect.Type type, final Map<String, Compound> map) {
                return map.get(type.getTypeName()).getParameters();
            }
        };

        @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "ChainOfInstanceofChecks"})
        public static Spec valueOf(final java.lang.reflect.Type type) {
            if (type instanceof Class)
                return CLASS;
            else if (type instanceof ParameterizedType)
                return PARAMETERIZED_TYPE;
            else if (type instanceof TypeVariable)
                return TYPE_VARIABLE;
            else
                throw new IllegalArgumentException("Unsupported type: " + type.getClass().getCanonicalName());
        }

        abstract Class<?> rawClass(final java.lang.reflect.Type type, final Map<String, Compound> map);

        abstract List<Compound> parameters(final java.lang.reflect.Type type, final Map<String, Compound> map);
    }

    public static class Compound {

        @SuppressWarnings("rawtypes")
        private final Class rawClass;
        private final List<Compound> parameters;
        private transient volatile String presentation;

        private Compound(final Class<?> rawClass, final List<Compound> parameters) {
            this.rawClass = rawClass;
            final int expectedLength = rawClass.getTypeParameters().length;
            final int actualLength = parameters.size();
            if (0 == actualLength) {
                this.parameters = Collections.emptyList();
            } else if (expectedLength == actualLength) {
                this.parameters = unmodifiableList(new ArrayList<>(parameters));
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "class %s needs %d type parameter(s) but was %d",
                                rawClass.getCanonicalName(), expectedLength, actualLength));
            }
        }

        private Compound(final java.lang.reflect.Type type, final Spec spec, final Map<String, Compound> map) {
            this(spec.rawClass(type, map), spec.parameters(type, map));
        }

        @SuppressWarnings("OverloadedVarargsMethod")
        public Compound(final Class<?> rawClass, final Compound... parameters) {
            this(rawClass, asList(parameters));
        }

        public Compound(final java.lang.reflect.Type type, final Map<String, Compound> map) {
            this(type, Spec.valueOf(type), map);
        }

        @SuppressWarnings("rawtypes")
        public final Class getRawClass() {
            return rawClass;
        }

        public final List<Compound> getParameters() {
            // is already unmodifiable ...
            // noinspection AssignmentOrReturnOfFieldWithMutableType
            return parameters;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(rawClass, parameters);
        }

        @Override
        public final boolean equals(final Object obj) {
            return (this == obj) || ((obj instanceof Type.Compound) && isEqual((Compound) obj));
        }

        private boolean isEqual(final Compound other) {
            return rawClass.equals(other.rawClass) && parameters.equals(other.parameters);
        }

        @Override
        public final String toString() {
            return Optional.ofNullable(presentation).orElseGet(() -> {
                presentation = rawClass.getSimpleName() + (
                        parameters.isEmpty() ? "" : parameters.stream()
                                .map(Compound::toString)
                                .collect(Collectors.joining(", ", "<", ">")));
                return presentation;
            });
        }
    }
}
