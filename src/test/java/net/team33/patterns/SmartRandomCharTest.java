package net.team33.patterns;

import org.junit.Test;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SmartRandomCharTest {

    private static final int MAX_RETRY =
            100000;
    private static final String RESULT_SHOULD_BE_IN_CHARSET =
            "Any result should be from \"%s\" but result was '%s'";

    @Test
    public final void anyCharDefault() {
        final String charset = SmartRandom.DEFAULT_CHARSET;
        final SmartRandom random = SmartRandom.builder().build();
        Stream.generate(() -> random.any(Character.TYPE)).limit(MAX_RETRY).forEach(result -> {
            assertFalse(
                    format(RESULT_SHOULD_BE_IN_CHARSET, charset, result),
                    0 > charset.indexOf(result)
            );
        });
    }

    @Test
    public final void anyCharSingle() {
        final String charset = "A";
        final SmartRandom random = SmartRandom.builder()
                .setCharset(charset)
                .build();
        Stream.generate(() -> random.any(Character.TYPE)).limit(MAX_RETRY).forEach(result -> {
            assertEquals(charset.charAt(0), result.charValue());
            assertFalse(
                    format(RESULT_SHOULD_BE_IN_CHARSET, charset, result),
                    0 > charset.indexOf(result)
            );
        });
    }

    @Test
    public final void anyCharFew() {
        final String charset = "0123456789";
        final SmartRandom random = SmartRandom.builder()
                .setCharset(charset)
                .build();
        Stream.generate(() -> random.any(Character.TYPE)).limit(MAX_RETRY).forEach(result -> {
            assertFalse(
                    format(RESULT_SHOULD_BE_IN_CHARSET, charset, result),
                    0 > charset.indexOf(result)
            );
        });
    }
}