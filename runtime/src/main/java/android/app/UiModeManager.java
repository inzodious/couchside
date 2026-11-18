package android.app;

public class UiModeManager {
    public static final int MODE_NIGHT_AUTO = 0, MODE_NIGHT_NO = 1, MODE_NIGHT_YES = 2;
    public int getCurrentModeType() { return android.content.res.Configuration.UI_MODE_TYPE_TELEVISION; }
    public int getNightMode() { return MODE_NIGHT_YES; }
    public void setNightMode(int mode) {}
}
