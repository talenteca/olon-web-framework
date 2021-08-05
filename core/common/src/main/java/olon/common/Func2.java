package olon.common;

/**
 * A two argument function that returns
 * something of type Z
 */
public interface Func2<A, B, Z> {
    /**
     * Apply the function
     */
    public Z apply(A a, B b);
}