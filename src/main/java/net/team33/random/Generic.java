package net.team33.random;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

@SuppressWarnings({"AbstractClassWithOnlyOneDirectInheritor", "AbstractClassWithoutAbstractMethods", "unused"})
public abstract class Generic<T> {

    private final Compound compound;

    protected Generic() {
        try {
            compound = new Compound(typeArgument(getClass()));
        } catch (final RuntimeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Type typeArgument(final Class<?> thisClass) {
        final Type type = thisClass.getGenericSuperclass();
        return direct(parameterized(type)).getActualTypeArguments()[0];
    }

    private static ParameterizedType direct(final ParameterizedType parameterized) {
        if (Generic.class.equals(parameterized.getRawType())) {
            return parameterized;
        } else {
            throw new IllegalArgumentException("The type of an instance must be derived directly from <Generic>");
        }
    }

    private static ParameterizedType parameterized(final Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        } else {
            throw new IllegalArgumentException(
                    "The type of an instance must be a concretely parameterized derivation of <Generic>");
        }
    }

    public final Compound getCompound() {
        return compound;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Generic<?>) && compound.equals(((Generic<?>) obj).compound));
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
            Class<?> rawClass(final Type type) {
                return (Class<?>) type;
            }

            @Override
            List<Compound> parameters(final Type type) {
                return Collections.emptyList();
            }
        },

        PARAMETERIZED_TYPE {
            @Override
            Class<?> rawClass(final Type type) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            }

            @Override
            List<Compound> parameters(final Type type) {
                return Stream.of(((ParameterizedType) type).getActualTypeArguments())
                        .map(Compound::new)
                        .collect(Collectors.toList());
            }
        };

        @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "ChainOfInstanceofChecks"})
        public static Spec valueOf(final Type type) {
            if (type instanceof Class)
                return CLASS;
            else if (type instanceof ParameterizedType)
                return PARAMETERIZED_TYPE;
            else
                throw new IllegalArgumentException("Unknown type: " + type.getClass().getCanonicalName());
        }

        abstract Class<?> rawClass(final Type type);

        abstract List<Compound> parameters(final Type type);
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

        private Compound(final Type type, final Spec spec) {
            this(spec.rawClass(type), spec.parameters(type));
        }

        @SuppressWarnings("OverloadedVarargsMethod")
        public Compound(final Class<?> rawClass, final Compound... parameters) {
            this(rawClass, asList(parameters));
        }

        public Compound(final Type type) {
            this(type, Spec.valueOf(type));
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
            return (this == obj) || ((obj instanceof Generic.Compound) && isEqual((Compound) obj));
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
