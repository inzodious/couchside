package couchside.shim;

/**
 * The TV screen's size in pixels at xhdpi (2 px per dp). It's 1080 high, like a 1080p TV, and as wide as the window's
 * shape makes it (1920 for a 16:9 window), so the app fills the window with no black bars. TvScreen sets it.
 */
public final class ScreenSize {
    public static volatile int width = 1920, height = 1080;

    private ScreenSize() {}

    public static int widthDp() { return width / 2; }
    public static int heightDp() { return height / 2; }

    /** Brings a metrics or configuration object the app holds up to the current size. */
    public static android.util.DisplayMetrics apply(android.util.DisplayMetrics m) { m.widthPixels = width; m.heightPixels = height; return m; }
    public static android.content.res.Configuration apply(android.content.res.Configuration c) {
        c.screenWidthDp = widthDp(); c.screenHeightDp = heightDp(); c.smallestScreenWidthDp = Math.min(widthDp(), heightDp()); return c;
    }
}
