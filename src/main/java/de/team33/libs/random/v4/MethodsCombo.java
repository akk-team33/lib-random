package de.team33.libs.random.v4;

import de.team33.libs.decisions.v1.Choices;
import de.team33.libs.typing.v3.Type;

import java.util.function.Function;

class MethodsCombo extends Methods {

    static final MethodsCombo INSTANCE = new MethodsCombo();

    private static final Function<Type<?>, Methods> DELEGATE = new Choices<Type<?>, Methods>()
            .when(Methods4Arrays::test).then(Methods4Arrays.INSTANCE)
            .orWhen(Methods4Enum::test).then(Methods4Enum.INSTANCE)
            .orElse(Methods.FAIL);

    @Override
    final <T> Function<Dispenser, T> get(final Type<T> type) {
        return DELEGATE.apply(type).get(type);
    }
}
