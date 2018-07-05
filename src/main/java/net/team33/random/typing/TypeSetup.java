package net.team33.random.typing;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

public class TypeSetup {

    @SuppressWarnings("rawtypes")
    private final Class rawClass;
    private final List<TypeSetup> parameters;
    private transient volatile String presentation;

    private TypeSetup(final Class<?> rawClass, final List<TypeSetup> parameters) {
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

    private TypeSetup(final Type type, final Variant variant, final Map<String, TypeSetup> map) {
        this(variant.rawClass(type, map), variant.parameters(type, map));
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public TypeSetup(final Class<?> rawClass, final TypeSetup... parameters) {
        this(rawClass, asList(parameters));
    }

    public TypeSetup(final Type type, final Map<String, TypeSetup> map) {
        this(type, Variant.valueOf(type), map);
    }

    @SuppressWarnings("rawtypes")
    public final Class getRawClass() {
        return rawClass;
    }

    public final List<TypeSetup> getParameters() {
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
        return (this == obj) || ((obj instanceof TypeSetup) && isEqual((TypeSetup) obj));
    }

    private boolean isEqual(final TypeSetup other) {
        return rawClass.equals(other.rawClass) && parameters.equals(other.parameters);
    }

    @Override
    public final String toString() {
        return Optional.ofNullable(presentation).orElseGet(() -> {
            presentation = rawClass.getSimpleName() + (
                    parameters.isEmpty() ? "" : parameters.stream()
                            .map(TypeSetup::toString)
                            .collect(Collectors.joining(", ", "<", ">")));
            return presentation;
        });
    }
}
