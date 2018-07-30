package net.team33.random.v2;

import net.team33.random.BasicRandom;
import net.team33.random.Selector;
import net.team33.random.v2.typing.DefiniteType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public abstract class SmartRandom {

    public static Builder builder() {
        return new Builder();
    }

    public final <T> T any(final Class<T> resultClass) {
        return any(DefiniteType.of(resultClass));
    }

    public abstract <T> T any(final DefiniteType<T> resultType);

    public abstract BasicRandom basic();

    public abstract Selector select();

    public abstract String charset();

    private enum Primitive {

        BYTE(Byte.TYPE, Byte.class),
        SHORT(Short.TYPE, Short.class),
        INT(Integer.TYPE, Integer.class),
        LONG(Long.TYPE, Long.class),
        FLOAT(Float.TYPE, Float.class),
        DOUBLE(Double.TYPE, Double.class),
        BOOL(Boolean.TYPE, Boolean.class),
        CHAR(Character.TYPE, Character.class);

        private final List<DefiniteType<?>> representations;

        Primitive(final Class<?>... representations) {
            this.representations = unmodifiableList(Stream.of(representations)
                    .map(DefiniteType::of)
                    .collect(Collectors.toList()));
        }

        private static Optional<Primitive> optional(final DefiniteType<?> representation) {
            return Stream.of(values())
                    .filter(value -> value.representations.contains(representation))
                    .findAny();
        }
    }

    private static final class Init {

        private static final char WHITESPACE = ' ';
        private static final char ASCII_LIMIT = 128;

        private static String defaultCharset() {
            final char[] result = new char[ASCII_LIMIT - WHITESPACE];
            for (char c = WHITESPACE; c < ASCII_LIMIT; ++c) {
                result[c - WHITESPACE] = c;
            }
            return new String(result);
        }
    }

    public static final class Builder {

        private static final String DEFAULT_CHARSET = Init.defaultCharset();

        @SuppressWarnings("rawtypes")
        private final Map<DefiniteType, BiFunction> methods = new HashMap<>(0);

        @SuppressWarnings("Convert2MethodRef")
        private Supplier<BasicRandom> newBasic = () -> new BasicRandom.Simple();
        private String charset = DEFAULT_CHARSET;

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private Builder() {
            put(Boolean.TYPE, (random, type) -> random.basic().anyBoolean());
            put(Byte.TYPE, (random, type) -> (byte) random.basic().anyInt());
            put(Short.TYPE, (random, type) -> (short) random.basic().anyInt());
            put(Integer.TYPE, (random, type) -> random.basic().anyInt());
            put(Long.TYPE, (random, type) -> random.basic().anyLong());
            put(Float.TYPE, (random, type) -> random.basic().anyFloat());
            put(Double.TYPE, (random, type) -> random.basic().anyDouble());
            put(Character.TYPE, (random, type) -> random.select().anyOf(random.charset().toCharArray()));
        }

        private static Stream<DefiniteType<?>> siblings(final DefiniteType<?> type) {
            return Primitive.optional(type)
                    .map(primitive -> primitive.representations.stream())
                    .orElseGet(() -> Stream.of(type));
        }

        public final <T> Builder put(final Class<T> resultClass,
                                     final BiFunction<SmartRandom, DefiniteType<T>, T> method) {
            return put(DefiniteType.of(resultClass), method);
        }

        public final <T> Builder put(final DefiniteType<T> resultType,
                                     final BiFunction<SmartRandom, DefiniteType<T>, T> method) {
            siblings(resultType)
                    .forEach(type -> methods.put(type, method));
            return this;
        }

        public final Builder setNewBasic(final Supplier<BasicRandom> newBasic) {
            this.newBasic = newBasic;
            return this;
        }

        public final SmartRandom build() {
            return new Stage(this);
        }

        public Builder setCharset(final String charset) {
            this.charset = charset;
            return this;
        }
    }

    @SuppressWarnings("AbstractClassWithOnlyOneDirectInheritor")
    private abstract static class Proxy extends SmartRandom {

        @Override
        public final <T> T any(final DefiniteType<T> resultType) {
            return delegate().any(resultType);
        }

        @Override
        public final BasicRandom basic() {
            return delegate().basic();
        }

        @Override
        public final Selector select() {
            return delegate().select();
        }

        @Override
        public final String charset() {
            return delegate().charset();
        }

        abstract SmartRandom delegate();
    }

    private static final class Stage extends Proxy {

        @SuppressWarnings("rawtypes")
        private final Map<DefiniteType, BiFunction> methods;
        private final Supplier<BasicRandom> newBasic;
        private final String charset;

        private Stage(final Builder builder) {
            this.methods = unmodifiableMap(new HashMap<>(builder.methods));
            this.newBasic = builder.newBasic;
            this.charset = builder.charset;
        }

        @Override
        final SmartRandom delegate() {
            return new Worker(this);
        }
    }

    private static final class Worker extends SmartRandom {

        private final Stage stage;
        private final BasicRandom basic;
        private final Selector selector;

        private Worker(final Stage stage) {
            this.stage = stage;
            this.basic = stage.newBasic.get();
            this.selector = new Selector(basic);
        }

        @Override
        public final <T> T any(final DefiniteType<T> resultType) {
            return method(resultType).apply(this, resultType);
        }

        @Override
        public final BasicRandom basic() {
            return basic;
        }

        @Override
        public final Selector select() {
            return selector;
        }

        @Override
        public final String charset() {
            return stage.charset;
        }

        @SuppressWarnings("unchecked")
        private <T> BiFunction<SmartRandom, DefiniteType<T>, T> method(final DefiniteType<T> type) {
            return Optional.ofNullable(stage.methods.get(type)).orElseGet(() -> {
                final BiFunction<SmartRandom, DefiniteType<T>, T> result = defaultMethod(type);
                return result;
            });
        }

        private <T> BiFunction<SmartRandom, DefiniteType<T>, T> defaultMethod(final DefiniteType<T> type) {
            if (type.getUnderlyingClass().isArray())
                return arrayMethod(type);
            else
                throw new IllegalArgumentException("No method specified for " + type);
        }

        private <T> BiFunction<SmartRandom, DefiniteType<T>, T> arrayMethod(final DefiniteType<T> type) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }
}
