package de.team33.test.random.v1;

import de.team33.libs.random.v1.SmartRandom;
import de.team33.libs.typing.v2.TypeDef;
import de.team33.test.random.shared.Generic;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SmartRandomTest {

    private static final TypeDef<Generic<String, List<String>, Map<String, List<String>>>> GENERIC_TYPE =
            new TypeDef<Generic<String, List<String>, Map<String, List<String>>>>() {
            };
    private static final TypeDef<List<String>> LIST_TYPE =
            new TypeDef<List<String>>() {
            };
    private static final TypeDef<Map<String, List<String>>> MAP_TYPE =
            new TypeDef<Map<String, List<String>>>() {
            };

    @Test
    public final void anyGeneric() {
        final Generic<String, List<String>, Map<String, List<String>>> result =
                SmartRandom.builder()
                        .put(Integer.class, (rnd, type) -> 278)
                        .put(String.class, (rnd, type) -> "a String")
                        .put(LIST_TYPE, (rnd, type) -> Arrays.asList(
                                rnd.any(String.class),
                                rnd.any(String.class),
                                rnd.any(String.class)))
                        .put(MAP_TYPE, (rnd, type) -> Collections.singletonMap(
                                rnd.any(String.class),
                                rnd.any(LIST_TYPE)
                        ))
                        .put(GENERIC_TYPE, (rnd, type) -> rnd.getCharged(type)
                                .setFields(new Generic<>()))
                        .prepare().get().any(GENERIC_TYPE);
        assertInt(result.getTheInt());
        assertString(result.getTheString());
        assertString(result.getTheE());
        assertList(result.getTheList());
        assertList(result.getTheF());
        assertList(result.getTheEList());
        assertMap(result.getTheMap());
        assertMap(result.getTheG());
        assertMap(result.getTheE2FMap());
    }

    private void assertMap(final Object value) {
        assertTrue(value instanceof Map);
        final Map<?, ?> map = (Map<?, ?>) value;
        assertEquals(1, map.size());
        for (final Object element : map.keySet()) {
            assertString(element);
        }
        for (final Object element : map.values()) {
            assertList(element);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    private static void assertList(final Object value) {
        assertTrue(value instanceof List);
        final List<?> list = (List<?>) value;
        assertEquals(3, list.size());
        for (final Object element : list) {
            assertString(element);
        }
    }

    private static void assertString(final Object value) {
        assertTrue(value instanceof String);
        assertEquals("a String", value);
    }

    private static void assertInt(final Object value) {
        assertTrue(value instanceof Integer);
        assertEquals(278, value);
    }
}