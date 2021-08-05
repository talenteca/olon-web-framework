package olon.common;

/**
 * A one argument function that returns
 * something of type Z
 */
public interface Func1<A,Z> {
    /**
     * Apply the function
     */
    public Z apply(A a);
}