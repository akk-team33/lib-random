package de.team33.libs.random.reflect;

import java.lang.reflect.Modifier;
import java.util.function.IntPredicate;

@SuppressWarnings({"unused", "ClassWithTooManyFields"})
final class Modifiers {

    public static final IntPredicate TRUE = modifiers -> true;
    public static final IntPredicate FALSE = modifiers -> false;
    public static final IntPredicate NONE = modifiers -> 0 == modifiers;
    public static final IntPredicate PUBLIC = Modifier::isPublic;
    public static final IntPredicate PRIVATE = Modifier::isPrivate;
    public static final IntPredicate PROTECTED = Modifier::isProtected;
    public static final IntPredicate STATIC = Modifier::isStatic;
    public static final IntPredicate FINAL = Modifier::isFinal;
    public static final IntPredicate SYNCHRONIZED = Modifier::isSynchronized;
    public static final IntPredicate VOLATILE = Modifier::isVolatile;
    public static final IntPredicate TRANSIENT = Modifier::isTransient;
    public static final IntPredicate NATIVE = Modifier::isNative;
    public static final IntPredicate INTERFACE = Modifier::isInterface;
    public static final IntPredicate ABSTRACT = Modifier::isAbstract;

    private Modifiers() {
    }
}
