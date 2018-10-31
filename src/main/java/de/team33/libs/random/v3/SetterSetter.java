package de.team33.libs.random.v3;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Methods;
import de.team33.libs.typing.v3.Type;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

/**
 * <p>A tool for setting properties of instances of a particular type with arbitrary but well-typed values.</p>
 * <p>An instance is generally not thread-safe and should be short lived.</p>
 *
 * @see Template#get(Class)
 * @see Template#get(Type)
 */
@SuppressWarnings("ClassWithOnlyPrivateConstructors")
public final class SetterSetter<T> {

    private static final String CANNOT_ACCESS = "cannot access <%s> in context <%s>";
    private static final String CANNOT_FIND = "cannot find definite type of <%s> in context <%s>";

    private final Type<T> type;
    private final Function<Type<?>, ?> values;
    private final Set<Method> methods;

    private SetterSetter(final Type<T> type, final Template template) {
        this.type = type;
        this.values = template.values;
        this.methods = Methods.PUBLIC_SETTERS.apply(type.getUnderlyingClass())
                .collect(toSet());
    }

    private static Type<?> typeOf(final Method method, final Type<?> context) throws TypeNotFoundException {
        if (null == context) {
            throw new TypeNotFoundException();
        } else {
            return typeOf(method, method.getDeclaringClass(), context, context.getUnderlyingClass());
        }
    }

    private static Type<?> typeOf(final Method method,
                                  final Class<?> declaringClass,
                                  final Type<?> context,
                                  final Class<?> underlyingClass) throws TypeNotFoundException {
        if (declaringClass.equals(underlyingClass)) {
            return context.getMemberType(method.getGenericParameterTypes()[0]);
        } else {
            return typeOf(method, typeOf(underlyingClass.getGenericSuperclass(), context));
        }
    }

    private static Type<?> typeOf(final java.lang.reflect.Type superType, final Type<?> context) {
        return (null == superType) ? null : context.getMemberType(superType);
    }

    /**
     * <p>Retrieves a new template for {@link SetterSetter}s using a specified method to get values of suitable
     * type for the fields.</p>
     */
    public static Template prepare(final Function<Type<?>, ?> method) {
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
        methods.forEach(method -> {
            try {
                method.invoke(subject, values.apply(typeOf(method, type)));
            } catch (final IllegalAccessException | InvocationTargetException caught) {
                throw new IllegalStateException(format(CANNOT_ACCESS, method, type), caught);
            } catch (final TypeNotFoundException caught) {
                throw new IllegalStateException(format(CANNOT_FIND, method, type), caught);
            }
        });
        return subject;
    }

    private static class TypeNotFoundException extends Exception {
    }

    /**
     * <p>A template (kind of factory) for a single or several consistent {@link SetterSetter}s.</p>
     * <p>In contrast to the {@link SetterSetter} itself, a template is always thread-safe.
     * It can be long or short lived.</p>
     *
     * @see #prepare(Function)
     */
    public static class Template {

        private final Function<Type<?>, ?> values;
        private final Predicate<Field> filter;

        private Template(final Function<Type<?>, ?> values, final Predicate<Field> filter) {
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
         * <p>Determines that a resulting {@link SetterSetter} should ignore certain fields based on their name.</p>
         *
         * @return A new template based on this, but using an updated filter. This template remains unchanged.
         */
        @SuppressWarnings("OverloadedVarargsMethod")
        public final Template ignore(final String... ignorable) {
            return ignore(asList(ignorable));
        }

        /**
         * <p>Determines that a resulting {@link SetterSetter} should ignore certain fields based on their name.</p>
         *
         * @return A new template based on this, but using an updated filter. This template remains unchanged.
         */
        public final Template ignore(final Collection<String> ignorable) {
            final Collection<String> names = new HashSet<>(ignorable);
            return setFilter(origin -> origin.and(field -> !names.contains(field.getName())));
        }

        /**
         * Retrieves a new {@link SetterSetter} for the given type, based on this template.
         */
        public final <T> SetterSetter<T> get(final Class<T> type) {
            return get(Type.of(type));
        }

        /**
         * Retrieves a new {@link SetterSetter} for the given type, based on this template.
         */
        public final <T> SetterSetter<T> get(final Type<T> type) {
            return new SetterSetter<>(type, this);
        }
    }
}
