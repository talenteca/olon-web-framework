package olon.common;

import scala.*;

import java.util.concurrent.Callable;

/**
 * The bridge from various arity FuncX to
 * Scala's function instances
 */
public final class Func {
    private static final FuncJBridge bridge = new FuncJBridge();

    /**
     * Lift a Java Func0 to a Scala Function0
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<Z> Function0<Z> lift(Func0<Z> f) {
	return bridge.lift(f);
    }

    /**
     * Lift a Java Func1 to a Scala Function1
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<A, Z> Function1<A, Z> lift(Func1<A, Z> f) {
	return bridge.lift(f);
    }

    /**
     * Lift a Java Func2 to a Scala Function2
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<A, B, Z> Function2<A, B, Z> lift(Func2<A, B, Z> f) {
	return bridge.lift(f);
    }

    /**
     * Lift a Java Func3 to a Scala Function3
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<A, B, C, Z> Function3<A, B, C, Z> lift(Func3<A, B, C, Z> f) {
	return bridge.lift(f);
    }

    /**
     * Lift a Java Func4 to a Scala Function4
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<A, B, C, D, Z> Function4<A, B, C, D, Z> lift(Func4<A, B, C, D, Z> f) {
	return bridge.lift(f);
    }

    /**
     * Lift a Java Callable to a Scala Function0
     *
     * @param f the function to lift
     *
     * @returns the Scala function
     */
    public static<Z> Function0<Z> lift(Callable<Z> f) {
	return bridge.lift(f);
    }
}