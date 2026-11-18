package android.util;

public class DisplayMetrics {
    public static final int DENSITY_LOW = 120, DENSITY_MEDIUM = 160, DENSITY_TV = 213, DENSITY_HIGH = 240, DENSITY_XHIGH = 320, DENSITY_XXHIGH = 480, DENSITY_XXXHIGH = 640,
        DENSITY_DEFAULT = 160, DENSITY_DEVICE_STABLE = 320;
    public int widthPixels = couchside.shim.ScreenSize.width, heightPixels = couchside.shim.ScreenSize.height, densityDpi = 320;
    public float density = 2f, scaledDensity = 2f, xdpi = 320f, ydpi = 320f;
    public DisplayMetrics() {}
    public void setTo(DisplayMetrics o) { widthPixels = o.widthPixels; heightPixels = o.heightPixels; densityDpi = o.densityDpi; density = o.density; scaledDensity = o.scaledDensity; xdpi = o.xdpi; ydpi = o.ydpi; }
    public void setToDefaults() { setTo(new DisplayMetrics()); }
}
