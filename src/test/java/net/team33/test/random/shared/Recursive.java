package net.team33.test.random.shared;

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class Recursive {

    public static final Recursive EMPTY = new Recursive(new Recursive[0]);

    private final Recursive[] children;

    public Recursive(final Recursive[] children) {
        this.children = children;
    }

    public final Recursive[] getChildren() {
        return children;
    }
}
