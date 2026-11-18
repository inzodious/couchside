package android.os;

public final class PowerManager {
    public static final int PARTIAL_WAKE_LOCK = 1, SCREEN_DIM_WAKE_LOCK = 6, SCREEN_BRIGHT_WAKE_LOCK = 10, FULL_WAKE_LOCK = 26, ACQUIRE_CAUSES_WAKEUP = 0x10000000, ON_AFTER_RELEASE = 0x20000000;
    public static final String ACTION_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED", ACTION_DEVICE_IDLE_MODE_CHANGED = "android.os.action.DEVICE_IDLE_MODE_CHANGED";

    public final class WakeLock {
        private int held;
        public void acquire() { held++; }
        public void acquire(long timeout) { held++; }
        public void release() { if (held > 0) held--; }
        public void release(int flags) { release(); }
        public boolean isHeld() { return held > 0; }
        public void setReferenceCounted(boolean value) {}
        public void setWorkSource(WorkSource ws) {}
    }

    public interface OnThermalStatusChangedListener { void onThermalStatusChanged(int status); }
    public void addThermalStatusListener(OnThermalStatusChangedListener listener) {}
    public void addThermalStatusListener(java.util.concurrent.Executor executor, OnThermalStatusChangedListener listener) {}
    public void removeThermalStatusListener(OnThermalStatusChangedListener listener) {}
    public static final int THERMAL_STATUS_NONE = 0, THERMAL_STATUS_LIGHT = 1, THERMAL_STATUS_MODERATE = 2, THERMAL_STATUS_SEVERE = 3, THERMAL_STATUS_CRITICAL = 4;
    public WakeLock newWakeLock(int levelAndFlags, String tag) { return new WakeLock(); }
    public boolean isInteractive() { return true; }
    public boolean isScreenOn() { return true; }
    public boolean isPowerSaveMode() { return false; }
    public boolean isDeviceIdleMode() { return false; }
    public boolean isIgnoringBatteryOptimizations(String packageName) { return true; }
    public boolean isSustainedPerformanceModeSupported() { return false; }
    public int getCurrentThermalStatus() { return 0; }
    public void reboot(String reason) {}
}
