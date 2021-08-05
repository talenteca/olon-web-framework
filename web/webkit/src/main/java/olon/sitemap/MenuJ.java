package olon.sitemap;

/**
 * The bridge from Java-land into Menus
 */
public final class MenuJ {
    private final static MenuSingleton j = 
	(new MenuJBridge()).menu();

    /**
     * Get the Menu singleton
     */
    public static MenuSingleton j() {
	return j;
    }

}