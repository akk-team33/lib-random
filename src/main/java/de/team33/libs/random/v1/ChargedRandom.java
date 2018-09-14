package de.team33.libs.random.v1;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.typing.v1.DefType;

import java.util.function.Function;

public class ChargedRandom<T> {

    private final DefType<T> resultType;
    private final Function<DefType<?>, ?> method;

    public ChargedRandom(final DefType<T> resultType, final Function<DefType<?>, ?> method) {
        this.resultType = resultType;
        this.method = method;
    }

    public final T setFields(final T subject) {
        Fields.FLAT.apply(resultType.getUnderlyingClass())
                .filter(FieldFilter.SIGNIFICANT)
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    try {
                        field.set(subject, method.apply(resultType.getMemberType(field.getGenericType())));
                    } catch (final IllegalAccessException caught) {
                        throw new IllegalStateException(String.format("cannot access <%s>", field), caught);
                    }
                });
        return subject;
    }
}
