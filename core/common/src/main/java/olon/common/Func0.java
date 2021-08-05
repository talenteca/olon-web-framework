package olon.common;

/**
 * A zero argument function that returns
 * something of type Z
 */
public interface Func0<Z> {
    /**
     * Apply the function
     */
    public Z apply();
}