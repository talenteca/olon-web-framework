package olon.util;

import olon.common.Func0;
import java.util.concurrent.Callable;

/**
 * The bridge from Java to Lift's Vendor stuff
 */
public final class VendorJ {
    private static VendorJBridge j = new VendorJBridge();
    
    /**
     * Create a Vendor from a Func0
     */
    public static<T> Vendor<T> vendor(Func0<T> f) {
	return j.vendor(f);
    }

    /**
     * Create a Vendor from a Callable
     */
    public static<T> Vendor<T> vendor(Callable<T> f) {
	return j.vendor(f);
    }

    /**
     * Create a Vendor from a value
     */
    public static<T> Vendor<T> vendor(T v) {
	return j.vendor(v);
    }
}