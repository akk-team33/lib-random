package de.team33.test.random.v3;

import de.team33.libs.random.v3.SetterSetter;
import de.team33.libs.typing.v1.DefType;
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

    private static final DefType<Single<List<Integer>>> SINGLE_LIST_INT_TYPE = new DefType<Single<List<Integer>>>() {
    };
    private static final DefType<List<Integer>> LIST_INT_TYPE = new DefType<List<Integer>>() {
    };
    private static final DefType<Single<List<String>>> SINGLE_LIST_STRING_TYPE = new DefType<Single<List<String>>>() {
    };
    private static final DefType<List<String>> LIST_STRING_TYPE = new DefType<List<String>>() {
    };
    private static final Pool POOL = new Pool()
            .put(DefType.of(String.class), "a string")
            .put(DefType.of(Integer.class), 278)
            .put(DefType.of(Date.class), new Date(0))
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

        private final Map<DefType<?>, Object> map = new HashMap<>(0);

        public final <T> Pool put(final DefType<T> type, final T value) {
            map.put(type, value);
            return this;
        }

        public final <T> T get(final DefType<T> type) {
            //noinspection unchecked
            return (T) Optional.ofNullable(map.get(type))
                    .orElseThrow(() -> new IllegalArgumentException("unknown typ: " + type));
        }
    }
}