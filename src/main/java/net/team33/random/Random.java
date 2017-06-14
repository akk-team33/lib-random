package net.team33.random;

@SuppressWarnings({"unused", "ClassNamePrefixedWithPackageName"})
public final class Random {

    private final java.util.Random backing = new java.util.Random();

    private Random(final Builder builder) {
    }

    public static Builder builder() {
        return new Builder();
    }

    public final boolean nextBoolean() {
        return backing.nextBoolean();
    }

    public static final class Builder {
        private Builder() {
        }

        public final Random build() {
            return new Random(this);
        }
    }
}
