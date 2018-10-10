package de.team33.test.random.v3;

import de.team33.libs.random.v3.FieldSetter;
import de.team33.libs.typing.v1.DefType;
import de.team33.test.random.shared.Single;
import de.team33.test.random.shared.SingleDate;
import de.team33.test.random.shared.SingleString;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
public class FieldSetterTest {

    @SuppressWarnings("ClassNewInstance")
    private static final FieldSetter.Template TEMPLATE = FieldSetter.template(type -> {
        try {
            return type.getUnderlyingClass().newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    });

    @Test
    public final void newSingle() {
        assertNull(new Single<>().getField());
    }

    @Test
    public final void setFieldsSingleString() {
        final FieldSetter<SingleString> setter = TEMPLATE.get(SingleString.class);
        final SingleString result = setter.setFields(new SingleString());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(String.class)
        ));
    }

    @Test
    public final void setFieldsSingleDate() {
        final FieldSetter<SingleDate> setter = TEMPLATE.get(SingleDate.class);
        final SingleDate result = setter.setFields(new SingleDate());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(Date.class)
        ));
    }

    @Test
    public final void setFieldsSingleList() {
        final FieldSetter<Single<ArrayList<Integer>>> setter = TEMPLATE.get(new DefType<Single<ArrayList<Integer>>>() {
        });
        final Single<ArrayList<Integer>> result = setter.setFields(new Single<>());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(ArrayList.class)
        ));
    }
}