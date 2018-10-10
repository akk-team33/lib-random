package de.team33.libs.random.v3;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.typing.v1.DefType;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Represents a tool for filling fields of instances of a particular type with arbitrary but well-typed values.
 * <p>An instance is not thread-safe!</p>
 *
 * @see Template#get(DefType)
 * @see Template#get(Class)
 * @see Builder#build(DefType)
 * @see Builder#build(Class)
 */
public final class FieldSetter<T> {

    private static final Function<Class<?>, Stream<Field>> DEFAULT_FIELDS_FUNCTION = type -> Fields.DEEP.apply(type)
            .filter(FieldFilter.SIGNIFICANT);

    private final DefType<T> type;
    private final Function<DefType<?>, ?> values;
    private final Set<Field> fields;

    private FieldSetter(final Template template, final DefType<T> type) {
        this.type = type;
        this.values = template.valueFunction;
        this.fields = template.fieldsFunction.apply(type.getUnderlyingClass())
                .peek(field -> field.setAccessible(true))
                .collect(Collectors.toSet());
    }

    private static DefType<?> typeOf(final Field field, final DefType<?> context) {
        if (null == context) {
            throw new IllegalStateException(format("cannot find definite type of field <%s>", field));
        } else {
            return typeOf(field, field.getDeclaringClass(), context, context.getUnderlyingClass());
        }
    }

    private static DefType<?> typeOf(final Field field,
                                     final Class<?> declaringClass,
                                     final DefType<?> context,
                                     final Class<?> underlyingClass) {
        if (declaringClass.equals(underlyingClass)) {
            return context.getMemberType(field.getGenericType());
        } else {
            return typeOf(field, defTypeOf(underlyingClass.getGenericSuperclass(), context));
        }
    }

    private static DefType<?> defTypeOf(final Type superType, final DefType<?> context) {
        return (null == superType) ? null : context.getMemberType(superType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Template template(final Function<DefType<?>, ?> valueFunction) {
        return builder()
                .setValueFunction(valueFunction)
                .prepare();
    }

    /**
     * Sets all the <em>relevant</em> fields of a given instance with an <em>arbitrary</em> value.
     *
     * @return the given subject.
     */
    public final T setFields(final T subject) {
        fields.forEach(field -> {
            try {
                field.set(subject, values.apply(typeOf(field, type)));
            } catch (final IllegalAccessException caught) {
                throw new IllegalStateException(format("cannot access field <%s>", field), caught);
            }
        });
        return subject;
    }

    public static final class Template {

        private final Function<DefType<?>, ?> valueFunction;
        private final Function<Class<?>, Stream<Field>> fieldsFunction;
        private final int maxRecursionDepth;

        private Template(final Builder builder) {
            this.valueFunction = builder.valueFunction;
            this.fieldsFunction = builder.fieldsFunction;
            this.maxRecursionDepth = builder.maxRecursionDepth;
        }

        public final <T> FieldSetter<T> get(final Class<T> type) {
            return get(DefType.of(type));
        }

        public final <T> FieldSetter<T> get(final DefType<T> type) {
            return new FieldSetter<>(this, type);
        }

        public final Builder builder() {
            return new Builder(this);
        }
    }

    public static final class Builder {

        private Function<DefType<?>, ?> valueFunction;
        private Function<Class<?>, Stream<Field>> fieldsFunction;
        private int maxRecursionDepth;

        private Builder() {
            this.valueFunction = type -> null;
            this.fieldsFunction = DEFAULT_FIELDS_FUNCTION;
            this.maxRecursionDepth = 3;
        }

        private Builder(final Template template) {
            this.valueFunction = template.valueFunction;
            this.fieldsFunction = template.fieldsFunction;
            this.maxRecursionDepth = template.maxRecursionDepth;
        }

        public final Builder setValueFunction(final Function<DefType<?>, ?> valueFunction) {
            this.valueFunction = valueFunction;
            return this;
        }

        public final Builder setFieldsFunction(final Function<Class<?>, Stream<Field>> fieldsFunction) {
            this.fieldsFunction = fieldsFunction;
            return this;
        }

        public final Builder setMaxRecursionDepth(final int maxRecursionDepth) {
            this.maxRecursionDepth = maxRecursionDepth;
            return this;
        }

        public final Template prepare() {
            return new Template(this);
        }

        public final <T> FieldSetter<T> build(final Class<T> type) {
            return prepare().get(type);
        }

        public final <T> FieldSetter<T> build(final DefType<T> type) {
            return prepare().get(type);
        }
    }
}
