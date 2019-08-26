package de.team33.libs.random.v5;

import java.util.function.Supplier;

public class SmartRandom {

    private SmartRandom(final Stage stage) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Retrieves a simple instance of {@link SmartRandom}.
     */
    public static SmartRandom instance() {
        return builder().build();
    }

    /**
     * Retrieves a {@link Supplier} for simple instances of {@link SmartRandom}.
     */
    public static Supplier<SmartRandom> prepare() {
        return builder().prepare();
    }

    /**
     * Retrieves a {@link Builder} for custom instances of {@link SmartRandom}.
     */
    private static Builder builder() {
        return new Builder();
    }

    public final <T> T any(final Class<T> type) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private static final class Stage implements Supplier<SmartRandom> {

//        private final Supplier<Features> features;
//        private final MethodPool methods;

        private Stage(final Builder builder) {
//            this.features = builder.features.prepare();
//            this.methods = builder.methods.build();
        }

        @Override
        public final SmartRandom get() {
            return new SmartRandom(this);
        }
    }

    /**
     * Represents a builder for instances of {@link SmartRandom}.
     */
    public static class Builder {

        private final MethodPool.Builder methods = new MethodPool.Builder();

        /**
         * Retrieves an instance of {@link SmartRandom}.
         */
        public final SmartRandom build() {
            return this.prepare().get();
        }

        /**
         * Retrieves a {@link Supplier} for instances of {@link SmartRandom}.
         */
        public final Supplier<SmartRandom> prepare() {
            return new Stage(this);
        }
    }
}
