package de.team33.libs.random.v4;

import java.util.function.Function;
import java.util.function.Supplier;

import de.team33.libs.typing.v3.Type;


public class SmartRandom extends DispenserBase {

    private SmartRandom(final Stage stage) {
      super(stage.methods, stage.features.get());
    }

    public static Builder builder() {
        return new Builder();
    }

    private static class Stage implements Supplier<SmartRandom> {

        private final Supplier<Features> features;
        private final MethodCache methods;

        private Stage(final Builder builder) {
            this.features = builder.features.prepare();
            this.methods = builder.methods.build();
        }

        @Override
        public SmartRandom get() {
            return new SmartRandom(this);
        }
    }

    public static class Builder {

        private final Features.Builder features = new Features.Builder();
        private final MethodCache.Builder methods = new MethodCache.Builder();

        private Builder() {
        }

        public final <T> Builder setFeature(final Key<T> key, final Supplier<T> supplier) {
            features.setup(key, supplier);
            return this;
        }

        public final SmartRandom build() {
            return prepare().get();
        }

        public final Supplier<SmartRandom> prepare() {
            final Supplier<Features> template = features.prepare();
            return new Stage(this);
        }

        public final <T> Builder set(final Class<T> type, final Function<Dispenser, T> method) {
            return set(Type.of(type), method);
        }

        public final <T> Builder set(final Type<T> type, final Function<Dispenser, T> method) {
            methods.put(type, method);
            return this;
        }
    }
}
