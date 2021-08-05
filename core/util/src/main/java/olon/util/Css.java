package olon.util;

import scala.xml.NodeSeq;
import scala.Function1;

/**
 * The bridge from Java to Lift's CSS Selector Transforms
 */
public final class Css {
    private static CssJBridge j = new CssJBridge();

    /**
     * Create a Css Selector Transform 
     */
    public static CssSel sel(String selector, String value) {
	return j.sel(selector, value);
    }
    
    /**
     * Create a Css Selector Transform 
     */
    public static CssSel sel(String selector, NodeSeq value) {
	return j.sel(selector, value);
    }
    
    /**
     * Create a Css Selector Transform 
     */
    public static CssSel sel(String selector, Function1<NodeSeq, NodeSeq> value) {
	return j.sel(selector, value);
    }
    
    /**
     * Create a Css Selector Transform 
     */
    public static CssSel sel(String selector, Bindable value) {
	return j.sel(selector, value);
    }
}