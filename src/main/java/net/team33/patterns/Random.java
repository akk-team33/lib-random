package net.team33.patterns;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Random {

    private static final String DEFAULT_CHAR_POOL = "abcdefghijklmnopqrstuvwxyz,ABCDEFGHIJKLMNOPQRSTUVWXYZ.012456789";

    private final java.util.Random backing = new java.util.Random();
    private final Functions functions;
    private final StringBounds stringBounds;

    private Random(final Builder builder) {
        functions = new Functions(builder);
        stringBounds = builder.stringBounds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final <T> T next(final Class<T> resultClass) {
        return functions.get(resultClass).apply(this);
    }

    /**
     * @see java.util.Random#nextBoolean()
     */
    public final boolean nextBoolean() {
        return backing.nextBoolean();
    }

    /**
     * @see java.util.Random#nextInt()
     */
    public final int nextInt() {
        return backing.nextInt();
    }

    /**
     * @see java.util.Random#nextInt()
     */
    public final int nextInt(final int bound) {
        return backing.nextInt(bound);
    }

    /**
     * @see java.util.Random#nextLong()
     */
    public final long nextLong() {
        return backing.nextLong();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    public final float nextFloat() {
        return backing.nextFloat();
    }

    /**
     * @see java.util.Random#nextDouble()
     */
    public final double nextDouble() {
        return backing.nextDouble();
    }

    /**
     * @see java.util.Random#nextGaussian()
     */
    public final double nextGaussian() {
        return backing.nextGaussian();
    }

    public final String nextString() {
        return nextString(stringBounds);
    }

    public String nextString(final StringBounds bounds) {
        final char[] result = new char[bounds.length(this)];
        for (int i = 0; i < result.length; ++i) {
            result[i] = bounds.nextChar(this);
        }
        return new String(result);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class Functions {

        private final Map<Class, Function> pool;
        private final Map<Class, Function> cache;

        private Functions(final Builder builder) {
            pool = Collections.unmodifiableMap(new HashMap<>(builder.suppliers));
            cache = new HashMap<>(pool.size());
        }

        private <T> Function<Random, T> get(final Class<T> resultClass) {
            synchronized (cache) {
                return Optional
                        .ofNullable(cache.get(resultClass))
                        .orElseGet(() -> find(resultClass));
            }
        }

        private Function find(final Class<?> resultClass) {
            final Function result = pool.entrySet().stream()
                    .filter(entry -> resultClass.isAssignableFrom(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("no method specified for <" + resultClass + ">"));
            cache.put(resultClass, result);
            return result;
        }
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static final class Builder {

        @SuppressWarnings("rawtypes")
        private final Map<Class, Function> suppliers = new HashMap<>(0);
        private StringBounds stringBounds = new StringBounds(1, 16, DEFAULT_CHAR_POOL);

        private Builder() {
            put(Boolean.class, Random::nextBoolean);
            put(Boolean.TYPE, Random::nextBoolean);
            put(Byte.class, random -> (byte) random.nextInt());
            put(Byte.TYPE, random -> (byte) random.nextInt());
            put(Short.class, random -> (short) random.nextInt());
            put(Short.TYPE, random -> (short) random.nextInt());
            put(Integer.class, Random::nextInt);
            put(Integer.TYPE, Random::nextInt);
            put(Long.class, Random::nextLong);
            put(Long.TYPE, Random::nextLong);
            put(Float.class, Random::nextFloat);
            put(Float.TYPE, Random::nextFloat);
            put(Double.class, Random::nextDouble);
            put(Double.TYPE, Random::nextDouble);
            put(Character.class, random -> (char) random.nextInt());
            put(Character.TYPE, random -> (char) random.nextInt());
            put(String.class, Random::nextString);
            put(Date.class, random -> new Date(random.nextLong()));
        }

        public <T> Builder put(final Class<T> resultClass, final Function<Random, T> function) {
            suppliers.put(resultClass, function);
            return this;
        }

        public Builder setStringBounds(final StringBounds stringBounds) {
            this.stringBounds = stringBounds;
            return this;
        }

        public final Random build() {
            return new Random(this);
        }
    }

    public static class StringBounds {

        public final int minLength;
        public final int maxLength;
        public final String charPool;

        public StringBounds(final int minLength, final int maxLength, final String charPool) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.charPool = charPool;
        }

        private int length(final Random random) {
            return minLength + random.nextInt(maxLength - minLength);
        }

        private char nextChar(final Random random) {
            return charPool.charAt(random.nextInt(charPool.length()));
        }
    }
}
