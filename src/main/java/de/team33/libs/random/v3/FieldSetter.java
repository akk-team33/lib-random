package de.team33.libs.random.v3;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.typing.v1.DefType;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;

@SuppressWarnings({"MethodMayBeStatic", "unused"})
public class FieldSetter<T> {

    private final Template core;
    private final DefType<T> type;

    public FieldSetter(final Template core, final DefType<T> type) {
        this.core = core;
        this.type = type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Template template() {
        return builder().prepare();
    }

    public static <T> FieldSetter<T> instance(final Class<T> type) {
        return template().apply(type);
    }

    public static <T> FieldSetter<T> instance(final DefType<T> type) {
        return template().apply(type);
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

    public final T setFields(final T subject) {
        fields(type.getUnderlyingClass()).forEach(field -> {
            try {
                field.set(subject, core.method.apply(typeOf(field, type)));
            } catch (final IllegalAccessException caught) {
                throw new IllegalStateException(format("cannot access field <%s>", field), caught);
            }
        });
        return subject;
    }

    private Stream<Field> fields(final Class<?> underlyingClass) {
        return core.fields.apply(underlyingClass)
                .filter(core.filter)
                .peek(field -> field.setAccessible(true));
    }

    public static final class Template {

        private final Function<DefType<?>, ?> method;
        private final Function<Class<?>, Stream<Field>> fields;
        private final Predicate<Field> filter;

        private Template(final Builder builder) {
            this.method = builder.method;
            this.fields = builder.fields;
            this.filter = builder.filter;
        }

        public final <T> FieldSetter<T> apply(final Class<T> type) {
            return apply(DefType.of(type));
        }

        public final <T> FieldSetter<T> apply(final DefType<T> type) {
            return new FieldSetter<>(this, type);
        }

        public final Builder builder() {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    public static final class Builder {

        private Function<DefType<?>, ?> method = type -> null;
        private Function<Class<?>, Stream<Field>> fields = Fields.DEEP;
        private Predicate<Field> filter = FieldFilter.SIGNIFICANT;

        private Builder() {
        }

        public final Builder setMethod(final Function<DefType<?>, ?> method) {
            this.method = method;
            return this;
        }

        public final Builder setFields(final Function<Class<?>, Stream<Field>> fields) {
            this.fields = fields;
            return this;
        }

        public final Builder setFilter(final Predicate<Field> filter) {
            this.filter = filter;
            return this;
        }

        public final Template prepare() {
            return new Template(this);
        }

        public final <T> FieldSetter<T> build(final DefType<T> type) {
            return prepare().apply(type);
        }
    }
}
