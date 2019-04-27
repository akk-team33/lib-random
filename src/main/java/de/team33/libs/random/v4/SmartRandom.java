package de.team33.libs.random.v4;

import de.team33.libs.typing.v3.Type;

import java.util.Date;
import java.util.function.Function;
import java.util.function.Supplier;

public class SmartRandom extends DispenserBase {

    @SuppressWarnings("rawtypes")
    private final Features features;

    private SmartRandom(final Supplier<Features> features) {
        this.features = features.get();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected final <T> Function<Dispenser, T> getMethod(final Type<T> type) {

        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public final <T> T getFeature(final Key<T> key) {
        return features.get(key);
    }

    public static class Builder {

        private final Features.Builder features = new Features.Builder();

        public final SmartRandom build() {
            return prepare().get();
        }

        public final Supplier<SmartRandom> prepare() {
            final Supplier<Features> template = features.prepare();
            return () -> new SmartRandom(template);
        }

        public final <T> Builder setFeature(final Key<T> key, final Supplier<T> supplier) {
            features.setup(key, supplier);
            return this;
        }
    }
}
