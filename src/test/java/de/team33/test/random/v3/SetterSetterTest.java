package de.team33.test.random.v3;

import de.team33.libs.random.v3.SetterSetter;
import de.team33.libs.typing.v3.Type;
import de.team33.test.random.shared.Single;
import de.team33.test.random.shared.SingleDate;
import de.team33.test.random.shared.SingleInteger;
import de.team33.test.random.shared.SingleString;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@SuppressWarnings("ConstantConditions")
public class SetterSetterTest {

    private static final Type<Single<List<Integer>>> SINGLE_LIST_INT_TYPE = new Type<Single<List<Integer>>>() {
    };
    private static final Type<List<Integer>> LIST_INT_TYPE = new Type<List<Integer>>() {
    };
    private static final Type<Single<List<String>>> SINGLE_LIST_STRING_TYPE = new Type<Single<List<String>>>() {
    };
    private static final Type<List<String>> LIST_STRING_TYPE = new Type<List<String>>() {
    };
    private static final Pool POOL = new Pool()
            .put(Type.of(String.class), "a string")
            .put(Type.of(Integer.class), 278)
            .put(Type.of(Date.class), new Date(0))
            .put(LIST_INT_TYPE, Collections.singletonList(278))
            .put(LIST_STRING_TYPE, Collections.singletonList("another string"));
    private static final SetterSetter.Template TEMPLATE = SetterSetter.prepare(POOL::get);

    @Test
    public final void newSingle() {
        assertNull(new Single<>().getField());
    }

    @Test
    public final void setFieldsSingleString() {
        final SetterSetter<SingleString> setter = TEMPLATE.get(SingleString.class);
        final SingleString result = setter.setFields(new SingleString());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(String.class)
        ));
    }

    @Test
    public final void setFieldsSingleInteger() {
        final SetterSetter<SingleInteger> setter = TEMPLATE.get(SingleInteger.class);
        final SingleInteger result = setter.setFields(new SingleInteger());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(Integer.class)
        ));
    }

    @Test
    public final void setFieldsSingleDate() {
        final SetterSetter<SingleDate> setter = TEMPLATE.get(SingleDate.class);
        final SingleDate result = setter.setFields(new SingleDate());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(Date.class)
        ));
    }

    @Test
    public final void setFieldsSingleListInt() {
        final SetterSetter<Single<List<Integer>>> setter = TEMPLATE.get(SINGLE_LIST_INT_TYPE);
        final Single<List<Integer>> result = setter.setFields(new Single<>());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(List.class),
                is(POOL.get(LIST_INT_TYPE))
        ));
    }

    @Test
    public final void setFieldsSingleListString() {
        final SetterSetter<Single<List<String>>> setter = TEMPLATE.get(SINGLE_LIST_STRING_TYPE);
        final Single<List<String>> result = setter.setFields(new Single<>());
        assertThat(result.getField(), allOf(
                notNullValue(),
                instanceOf(List.class),
                is(POOL.get(LIST_STRING_TYPE))
        ));
    }

    private static class Pool {

        private final Map<Type<?>, Object> map = new HashMap<>(0);

        public final <T> Pool put(final Type<T> type, final T value) {
            map.put(type, value);
            return this;
        }

        public final <T> T get(final Type<T> type) {
            //noinspection unchecked
            return (T) Optional.ofNullable(map.get(type))
                    .orElseThrow(() -> new IllegalArgumentException("unknown typ: " + type));
        }
    }
}