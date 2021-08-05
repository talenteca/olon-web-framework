package olon.common;

/**
 * A three argument function that returns
 * something of type Z
 */
public interface Func3<A, B, C, Z> {
    /**
     * Apply the function
     */
    public Z apply(A a, B b, C c);
}