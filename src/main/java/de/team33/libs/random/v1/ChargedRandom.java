package de.team33.libs.random.v1;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.typing.v2.TypeDesc;

import java.util.function.Function;

public class ChargedRandom {

    private final TypeDesc resultType;
    private final Function<TypeDesc, Object> method;

    public ChargedRandom(final TypeDesc resultType, final Function<TypeDesc, Object> method) {
        this.resultType = resultType;
        this.method = method;
    }

    public final <T> T setFields(final T subject) {
        Fields.FLAT.apply(resultType.getUnderlyingClass())
                .filter(FieldFilter.SIGNIFICANT)
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    try {
                        field.set(subject, method.apply(resultType.toTypeDesc(field.getGenericType())));
                    } catch (final IllegalAccessException caught) {
                        throw new IllegalStateException(String.format("cannot access <%s>", field), caught);
                    }
                });
        return subject;
    }
}
