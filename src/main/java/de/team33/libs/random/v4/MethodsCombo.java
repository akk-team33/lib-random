package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

class MethodsCombo extends Methods {

    static final MethodsCombo INSTANCE = new MethodsCombo();

    private static Methods delegate(final Type<?> type) {
        if (Methods4Arrays.PREDICATE.test(type)) {
            return Methods4Arrays.INSTANCE;
        } else if (Methods4Enum.PREDICATE.test(type)) {
            return Methods4Arrays.INSTANCE;
        } else {
            return Methods.FAIL;
        }
    }

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        return delegate(type).get(type);
    }
}
