package android.view;

import android.util.DisplayMetrics;

/** The simulated TV's screen: 1080p at 60 Hz unless the desktop host says otherwise. */
public final class Display {
    public static final int DEFAULT_DISPLAY = 0, INVALID_DISPLAY = -1, STATE_UNKNOWN = 0, STATE_OFF = 1, STATE_ON = 2, STATE_DOZE = 3, STATE_DOZE_SUSPEND = 4, STATE_VR = 5, STATE_ON_SUSPEND = 6, FLAG_SECURE = 2, FLAG_PRIVATE = 4;

    public static final class Mode {
        private final int id, width, height;
        private final float refreshRate;
        public Mode(int id, int width, int height, float refreshRate) { this.id = id; this.width = width; this.height = height; this.refreshRate = refreshRate; }
        public int getModeId() { return id; }
        public int getPhysicalWidth() { return width; }
        public int getPhysicalHeight() { return height; }
        public float getRefreshRate() { return refreshRate; }
        public float[] getAlternativeRefreshRates() { return new float[0]; }
        public int[] getSupportedHdrTypes() { return new int[0]; }
        @Override public boolean equals(Object o) { return o instanceof Mode m && m.id == id; }
        @Override public int hashCode() { return id; }
        @Override public String toString() { return "{id=" + id + ", width=" + width + ", height=" + height + ", fps=" + refreshRate + "}"; }
    }

    public static final class HdrCapabilities {
        public static final int HDR_TYPE_DOLBY_VISION = 1, HDR_TYPE_HDR10 = 2, HDR_TYPE_HLG = 3, HDR_TYPE_HDR10_PLUS = 4, HDR_TYPE_INVALID = -1;
        public static final float INVALID_LUMINANCE = -1;
        public int[] getSupportedHdrTypes() { return new int[0]; }
        public float getDesiredMaxLuminance() { return 500f; }
        public float getDesiredMaxAverageLuminance() { return 500f; }
        public float getDesiredMinLuminance() { return 0f; }
    }

    private final Mode[] modes = {new Mode(1, 1920, 1080, 60f)};

    public DisplayCutout getCutout() { return null; }
    public int getDisplayId() { return DEFAULT_DISPLAY; }
    public boolean isValid() { return true; }
    public String getName() { return "Couchside"; }
    public int getState() { return STATE_ON; }
    public int getFlags() { return 0; }
    public int getRotation() { return 0; }
    public float getRefreshRate() { return couchside.shim.Device.refreshRate(); }
    public float[] getSupportedRefreshRates() { return new float[] {getRefreshRate()}; }
    public Mode getMode() { return modes[0]; }
    public Mode[] getSupportedModes() { return modes.clone(); }
    public boolean isHdr() { return false; }
    public boolean isWideColorGamut() { return false; }
    public HdrCapabilities getHdrCapabilities() { return new HdrCapabilities(); }
    public boolean isMinimalPostProcessingSupported() { return false; }
    public int getWidth() { return couchside.shim.ScreenSize.width; }
    public int getHeight() { return 1080; }
    public void getSize(android.graphics.Point outSize) { outSize.set(couchside.shim.ScreenSize.width, couchside.shim.ScreenSize.height); }
    public void getRealSize(android.graphics.Point outSize) { outSize.set(couchside.shim.ScreenSize.width, couchside.shim.ScreenSize.height); }
    public void getRectSize(android.graphics.Rect outSize) { outSize.set(0, 0, couchside.shim.ScreenSize.width, couchside.shim.ScreenSize.height); }
    public void getMetrics(DisplayMetrics outMetrics) { outMetrics.setToDefaults(); }
    public void getRealMetrics(DisplayMetrics outMetrics) { outMetrics.setToDefaults(); }
    public long getAppVsyncOffsetNanos() { return 0L; }
    public long getPresentationDeadlineNanos() { return 16_666_666L; }
}
