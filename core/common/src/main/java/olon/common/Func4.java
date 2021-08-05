package olon.common;

/**
 * A four argument function that returns
 * something of type Z
 */
public interface Func4<A, B, C, D, Z> {
    /**
     * Apply the function
     */
    public Z apply(A a, B b, C c, D d);
}