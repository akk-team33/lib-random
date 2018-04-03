package net.team33.random;

import com.google.common.base.CaseFormat;
import org.junit.Assert;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public class SmartRandomEnumTest {

    private static final int MAX_RETRY = CaseFormat.values().length * 10;

    @Test
    public final void anyEnum() {
        final Set<CaseFormat> results = EnumSet.noneOf(CaseFormat.class);
        final SmartRandom random = SmartRandom.builder().build();
        Stream.generate(() -> random.any(CaseFormat.class)).limit(MAX_RETRY)
                .forEach(results::add);
        Stream.of(CaseFormat.values())
                .forEach(value -> Assert.assertTrue(
                        String.format("results should contain <%s>", value),
                        results.contains(value)));
    }
}