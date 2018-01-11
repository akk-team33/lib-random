package net.team33.patterns.test;

public class Recursive {

    private final Recursive child;

    public Recursive(final Recursive child) {
        this.child = child;
    }

    public final Recursive getChild() {
        return child;
    }
}
