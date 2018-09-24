package de.team33.test.random.v3;

import de.team33.libs.random.reflect.FieldFilter;
import de.team33.libs.random.reflect.Fields;
import de.team33.libs.random.v3.FieldSetter;
import de.team33.libs.typing.v1.DefType;
import de.team33.test.random.shared.Single;
import de.team33.test.random.shared.SingleDate;
import de.team33.test.random.shared.SingleString;
import org.junit.Test;

import java.util.Date;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
public class FieldSetterTest {

    @SuppressWarnings("ClassNewInstance")
    private static final Function<DefType<?>, ?> METHOD = type -> {
        try {
            return type.getUnderlyingClass().newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    };
    private static final FieldSetter.Template TEMPLATE = FieldSetter.builder()
            .setMethod(METHOD)
            .prepare();

    @Test
    public final void newSingle() {
        assertNull(new Single<>().getField());
    }

    @Test
    public final void setFieldsSingleString() {
        final FieldSetter<SingleString> setter = TEMPLATE.apply(SingleString.class);
        final SingleString result = setter.setFields(new SingleString());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(String.class)
        ));
    }

    @Test
    public final void setFieldsSingleDate() {
        final FieldSetter<SingleDate> setter = TEMPLATE.apply(SingleDate.class);
        final SingleDate result = setter.setFields(new SingleDate());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(Date.class)
        ));
    }

    @Test
    public final void setFieldsRebuilt() {
        final FieldSetter<SingleString> setter = TEMPLATE.builder()
                .setFields(Fields.FLAT)
                .setFilter(FieldFilter.STATIC)
                .build(SingleString.class);
        final SingleString result = setter.setFields(new SingleString());
        assertNull(result.getField());
    }
}