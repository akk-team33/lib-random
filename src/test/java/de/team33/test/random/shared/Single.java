package de.team33.test.random.shared;

@SuppressWarnings({"NonFinalFieldReferencedInHashCode", "NonFinalFieldReferenceInEquals"})
public class Single<T> {

    private T field;

    public final T getField() {
        return field;
    }

    public final void setField(final T field) {
        this.field = field;
    }

    @Override
    public final boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Single) && field.equals(((Single<?>) obj).field));
    }

    @Override
    public final int hashCode() {
        return field.hashCode();
    }

    @Override
    public final String toString() {
        return "Single{" + field + "}";
    }
}
