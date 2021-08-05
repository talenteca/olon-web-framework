package olon.sitemap;

/**
 * The bridge from Java-land into SiteMap
 */
public final class SiteMapJ {
    private final static SiteMapSingleton j = 
	(new SiteMapJBridge()).siteMap();

    /**
     * Get the SiteMap singleton
     */
    public static SiteMapSingleton j() {
	return j;
    }

    /**
     * Given a bunch of Menu items, create a
     * SiteMap
     */
    public static SiteMap build(ConvertableToMenu... m) {
	return j().build(m);
    }
				
}