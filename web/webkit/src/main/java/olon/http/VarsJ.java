package olon.http;

/**
 * The Java interface to SessionVar, RequestVar, ContainerVar,
 * and TransientRequestVar
 */

import java.util.concurrent.Callable;

/**
 * Vend SessionVar, RequestVar, etc. to Java callers
 */
public class VarsJ {
    /**
     * Vend a SessionVar with the default value
     */
    public static<T> SessionVar<T> vendSessionVar(T defValue) {
	return (new VarsJBridge()).vendSessionVar(defValue, new Exception());
    }

    /**
     * Vend a SessionVar with the function to create the default value
     */
    public static<T>  SessionVar<T> vendSessionVar(Callable<T> defFunc) {
	return (new VarsJBridge()).vendSessionVar(defFunc, new Exception());
    }
}