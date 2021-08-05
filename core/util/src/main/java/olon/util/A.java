package olon.util;

/**
 * Array helpers
 */
public final class A {
    /**
     * Create an array to pass to another method
     */
    @SuppressWarnings({"unchecked", "varargs"})
    public static<T> T[] a(T... t) {
      return t;
    }
}
