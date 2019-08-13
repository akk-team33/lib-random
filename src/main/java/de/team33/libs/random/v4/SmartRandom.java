package de.team33.libs.random.v4;

import de.team33.libs.identification.v1.Unique;
import de.team33.libs.typing.v3.Type;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;


public final class SmartRandom extends DispenserBase {

    private static final char[] DEFAULT_CHARACTERS =
            "@ 0123456789-abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private SmartRandom(final Stage stage) {
        super(stage.methods, stage.features.get());
    }

    public static Builder builder() {
        //noinspection NumericCastThatLosesPrecision
        return new Builder()
                .setFeature(Key.BASIC, Random::new)
                .setFeature(Key.CHARACTERS, () -> DEFAULT_CHARACTERS)
                .set(boolean.class, dsp -> dsp.getFeature(Key.BASIC).nextBoolean())
                .set(byte.class, dsp -> (byte) dsp.getFeature(Key.BASIC).nextInt())
                .set(short.class, dsp -> (short) dsp.getFeature(Key.BASIC).nextInt())
                .set(int.class, dsp -> dsp.getFeature(Key.BASIC).nextInt())
                .set(long.class, dsp -> dsp.getFeature(Key.BASIC).nextLong())
                .set(float.class, dsp -> (float) dsp.getFeature(Key.BASIC).nextDouble())
                .set(double.class, dsp -> dsp.getFeature(Key.BASIC).nextDouble())
                .set(char.class, dsp -> {
                    final char[] characters = dsp.getFeature(Key.CHARACTERS);
                    final Bounds bounds = new Bounds(characters.length);
                    return characters[bounds.limited(dsp.any(int.class))];
                })
                .set(String.class, dsp -> new String(dsp.any(char[].class)));
    }

    private static final class Stage implements Supplier<SmartRandom> {

        private final Supplier<Features> features;
        private final MethodPool methods;

        private Stage(final Builder builder) {
            this.features = builder.features.prepare();
            this.methods = builder.methods.build();
        }

        @Override
        public SmartRandom get() {
            return new SmartRandom(this);
        }
    }

    public static final class Builder {

        private final Features.Builder features = new Features.Builder();
        private final MethodPool.Builder methods = new MethodPool.Builder();

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

    public static class Key<T> extends Unique implements Dispenser.Key<T> {

        public static final Key<Random> BASIC = new Key<>();
        public static final Key<char[]> CHARACTERS = new Key<>();
    }
}
