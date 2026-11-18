package android.content.res;

import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import couchside.shim.Device;
import java.io.InputStream;

/** The app's `res` folders, copied onto the classpath by the build with an index of the ids in R (see ResTable). */
public class Resources {
    public static class NotFoundException extends RuntimeException {
        public NotFoundException() {}
        public NotFoundException(String name) { super(name); }
    }
    public final class Theme {
        public boolean resolveAttribute(int resid, android.util.TypedValue outValue, boolean resolveRefs) { return false; }
        public void applyStyle(int resId, boolean force) {}
        public Resources getResources() { return Resources.this; }
    }
    private final Theme theme = new Theme();
    public final Theme newTheme() { return theme; }

    private final AssetManager assets = new AssetManager();
    private final DisplayMetrics metrics = new DisplayMetrics();
    private final Configuration configuration = new Configuration();

    public static Resources getSystem() { return Device.resources(); }
    public final AssetManager getAssets() { return assets; }
    public DisplayMetrics getDisplayMetrics() { return couchside.shim.ScreenSize.apply(metrics); }
    public Configuration getConfiguration() { return couchside.shim.ScreenSize.apply(configuration); }
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {}

    public int getIdentifier(String name, String defType, String defPackage) { return Device.res().id(name, defType, defPackage); }
    public String getResourceName(int resid) { return Device.res().name(resid, true); }
    public String getResourceEntryName(int resid) { return Device.res().name(resid, false); }
    public String getResourceTypeName(int resid) { return Device.res().type(resid); }
    public String getResourcePackageName(int resid) { return Device.res().pkg(resid); }

    public InputStream openRawResource(int id) { return Device.res().open(id); }
    public InputStream openRawResource(int id, android.util.TypedValue value) { return Device.res().open(id); }
    public CharSequence getText(int id) { return getString(id); }
    public CharSequence getText(int id, CharSequence def) { String s = Device.res().string(id); return s == null ? def : s; }
    public String getString(int id) {
        String s = Device.res().string(id);
        if (s == null) throw new NotFoundException("String resource ID #0x" + Integer.toHexString(id));
        return s;
    }
    public String getString(int id, Object... formatArgs) { return String.format(getString(id), formatArgs); }
    public String[] getStringArray(int id) { return Device.res().strings(id); }
    public CharSequence[] getTextArray(int id) { return Device.res().strings(id); }
    public String getQuantityString(int id, int quantity) { return getString(id); }
    public String getQuantityString(int id, int quantity, Object... formatArgs) { return String.format(getString(id), formatArgs); }
    public int getColor(int id) { return Device.res().color(id); }
    public int getColor(int id, Theme theme) { return Device.res().color(id); }
    public boolean getBoolean(int id) { return "true".equals(Device.res().value(id)); }
    public int getInteger(int id) { return Integer.parseInt(Device.res().value(id)); }
    public float getDimension(int id) { return Device.res().dimension(id, metrics); }
    public int getDimensionPixelSize(int id) { return Math.round(getDimension(id)); }
    public int getDimensionPixelOffset(int id) { return (int) getDimension(id); }
    public Drawable getDrawable(int id) { return Device.res().drawable(id); }
    public Drawable getDrawable(int id, Theme theme) { return Device.res().drawable(id); }
    public Drawable getDrawableForDensity(int id, int density) { return Device.res().drawable(id); }
    public Drawable getDrawableForDensity(int id, int density, Theme theme) { return Device.res().drawable(id); }
    public android.graphics.Typeface getFont(int id) { return Device.res().font(id); }
    public void getValue(int id, android.util.TypedValue outValue, boolean resolveRefs) { outValue.resourceId = id; outValue.string = Device.res().value(id); }
}
