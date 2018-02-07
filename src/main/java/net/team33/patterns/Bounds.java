package net.team33.patterns;

public class Bounds {

    public final int minLength;
    public final int maxLength;

    public Bounds(final int minLength, final int maxLength) {
        if (0 > minLength)
            throw new IllegalArgumentException("minLength must be >= 0 but is " + minLength);
        if (minLength > maxLength)
            throw new IllegalArgumentException("maxLength must be >= minLength but is " + maxLength);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
}
