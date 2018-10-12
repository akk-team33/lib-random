package de.team33.libs.random.v3;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.typing.v1.DefType;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * <p>A tool for filling fields of instances of a particular type with arbitrary but well-typed values.</p>
 * <p>An instance is generally not thread-safe and should be short lived.</p>
 *
 * @see Template#get(Class)
 * @see Template#get(DefType)
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
public final class FieldSetter<T> {

    private static final String CANNOT_ACCESS = "cannot access field <%s> in context <%s>";
    private static final String CANNOT_FIND = "cannot find definite type of field <%s> in context <%s>";

    private final DefType<T> type;
    private final Function<DefType<?>, ?> values;
    private final Set<Field> fields;

    private FieldSetter(final DefType<T> type, final Template template) {
        this.type = type;
        this.values = template.values;
        this.fields = Fields.DEEP.apply(type.getUnderlyingClass())
                .filter(template.filter)
                .peek(field -> field.setAccessible(true))
                .collect(toSet());
    }

    private static DefType<?> typeOf(final Field field, final DefType<?> context) throws TypeNotFoundException {
        if (null == context) {
            throw new TypeNotFoundException();
        } else {
            return typeOf(field, field.getDeclaringClass(), context, context.getUnderlyingClass());
        }
    }

    private static DefType<?> typeOf(final Field field,
                                     final Class<?> declaringClass,
                                     final DefType<?> context,
                                     final Class<?> underlyingClass) throws TypeNotFoundException {
        if (declaringClass.equals(underlyingClass)) {
            return context.getMemberType(field.getGenericType());
        } else {
            return typeOf(field, typeOf(underlyingClass.getGenericSuperclass(), context));
        }
    }

    private static DefType<?> typeOf(final Type superType, final DefType<?> context) {
        return (null == superType) ? null : context.getMemberType(superType);
    }

    /**
     * <p>Retrieves a new template for {@link FieldSetter}s using a specified method to get values of suitable
     * type for the fields.</p>
     */
    public static Template prepare(final Function<DefType<?>, ?> method) {
        return new Template(method, FieldFilter.SIGNIFICANT);
    }

    /**
     * Sets all the <em>relevant</em> fields of a given instance with an <em>arbitrary</em> value.
     *
     * @return the given subject.
     * @see Template#setFilter(Function)
     * @see Template#ignore(String...)
     * @see Template#ignore(Collection)
     * @see #prepare(Function)
     */
    public final T setFields(final T subject) {
        fields.forEach(field -> {
            try {
                field.set(subject, values.apply(typeOf(field, type)));
            } catch (final IllegalAccessException caught) {
                throw new IllegalStateException(format(CANNOT_ACCESS, field, type), caught);
            } catch (final TypeNotFoundException caught) {
                throw new IllegalStateException(format(CANNOT_FIND, field, type), caught);
            }
        });
        return subject;
    }

    private static class TypeNotFoundException extends Exception {
    }

    /**
     * <p>A template (kind of factory) for a single or several consistent {@link FieldSetter}s.</p>
     * <p>In contrast to the {@link FieldSetter} itself, a template is always thread-safe.
     * It can be long or short lived.</p>
     *
     * @see #prepare(Function)
     */
    public static class Template {

        private final Function<DefType<?>, ?> values;
        private final Predicate<Field> filter;

        private Template(final Function<DefType<?>, ?> values, final Predicate<Field> filter) {
            this.values = values;
            this.filter = filter;
        }

        /**
         * <p>Determines which fields are to be considered by a resulting FieldSetter.</p>
         * <p>By default, all but static or transient fields are considered.</p>
         *
         * @param update a {@link Function} that may update or replace the current filter.
         * @return A new template based on this, but using the updated/replaced filter.
         * This template remains unchanged.
         */
        public final Template setFilter(final Function<Predicate<Field>, Predicate<Field>> update) {
            return new Template(values, update.apply(filter));
        }

        /**
         * <p>Determines that a resulting {@link FieldSetter} should ignore certain fields based on their name.</p>
         *
         * @return A new template based on this, but using an updated filter. This template remains unchanged.
         */
        @SuppressWarnings("OverloadedVarargsMethod")
        public final Template ignore(final String... ignorable) {
            return ignore(asList(ignorable));
        }

        /**
         * <p>Determines that a resulting {@link FieldSetter} should ignore certain fields based on their name.</p>
         *
         * @return A new template based on this, but using an updated filter. This template remains unchanged.
         */
        public final Template ignore(final Collection<String> ignorable) {
            final Collection<String> names = new HashSet<>(ignorable);
            return setFilter(origin -> origin.and(field -> !names.contains(field.getName())));
        }

        /**
         * Retrieves a new {@link FieldSetter} for the given type, based on this template.
         */
        public final <T> FieldSetter<T> get(final Class<T> type) {
            return get(DefType.of(type));
        }

        /**
         * Retrieves a new {@link FieldSetter} for the given type, based on this template.
         */
        public final <T> FieldSetter<T> get(final DefType<T> type) {
            return new FieldSetter<>(type, this);
        }
    }
}
