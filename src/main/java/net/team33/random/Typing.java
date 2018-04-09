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
public abstract class Typing<T> {

    private final Setup setup;

    protected Typing() {
        setup = new Setup(typingArgument(getClass()));
    }

    private static Type typingArgument(final Class<?> thisClass) {
        final Type type = thisClass.getGenericSuperclass();
        return direct(parameterized(type)).getActualTypeArguments()[0];
    }

    private static ParameterizedType direct(final ParameterizedType parameterized) {
        if (Typing.class.equals(parameterized.getRawType())) {
            return parameterized;
        } else {
            throw newIllegalStateException();
        }
    }

    private static ParameterizedType parameterized(final Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType) type;
        } else {
            throw newIllegalStateException();
        }
    }

    private static IllegalStateException newIllegalStateException() {
        return new IllegalStateException("The type of an instance must be derived directly from <Typing>");
    }

    public final Setup getSetup() {
        return setup;
    }

    private enum Spec {

        CLASS {
            @Override
            Class<?> rawClass(final Type type) {
                return (Class<?>) type;
            }

            @Override
            List<Setup> parameters(final Type type) {
                return Collections.emptyList();
            }
        },

        PARAMETERIZED_TYPE {
            @Override
            Class<?> rawClass(final Type type) {
                return (Class<?>) ((ParameterizedType) type).getRawType();
            }

            @Override
            List<Setup> parameters(final Type type) {
                return Stream.of(((ParameterizedType) type).getActualTypeArguments())
                        .map(Setup::new)
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

        abstract List<Setup> parameters(final Type type);
    }

    public static class Setup {

        @SuppressWarnings("rawtypes")
        private final Class rawClass;
        private final List<Setup> parameters;
        private transient volatile String presentation;

        private Setup(final Class<?> rawClass, final List<Setup> parameters) {
            final int expectedLength = rawClass.getTypeParameters().length;
            final int actualLength = parameters.size();
            if (expectedLength == actualLength) {
                this.rawClass = rawClass;
                this.parameters = unmodifiableList(new ArrayList<>(parameters));
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "class %s needs %d type parameters but was %d",
                                rawClass.getCanonicalName(), expectedLength, actualLength));
            }
        }

        private Setup(final Type type, final Spec spec) {
            this(spec.rawClass(type), spec.parameters(type));
        }

        @SuppressWarnings("OverloadedVarargsMethod")
        public Setup(final Class<?> rawClass, final Setup... parameters) {
            this(rawClass, asList(parameters));
        }

        public Setup(final Type type) {
            this(type, Spec.valueOf(type));
        }

        @SuppressWarnings("rawtypes")
        public final Class getRawClass() {
            return rawClass;
        }

        public final List<Setup> getParameters() {
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
            return (this == obj) || ((obj instanceof Typing.Setup) && isEqual((Setup) obj));
        }

        private boolean isEqual(final Setup other) {
            return rawClass.equals(other.rawClass) && parameters.equals(other.parameters);
        }

        @Override
        public final String toString() {
            return Optional.ofNullable(presentation).orElseGet(() -> {
                presentation = rawClass.getSimpleName() + (
                        parameters.isEmpty() ? "" : parameters.stream()
                                .map(Setup::toString)
                                .collect(Collectors.joining(", ", "<", ">")));
                return presentation;
            });
        }
    }
}
