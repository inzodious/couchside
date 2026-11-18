package android.app;

import couchside.shim.Device;
import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    public static class MemoryInfo { public long availMem, totalMem, threshold; public boolean lowMemory; }
    public static class RunningAppProcessInfo {
        public static final int IMPORTANCE_FOREGROUND = 100, IMPORTANCE_VISIBLE = 200, IMPORTANCE_SERVICE = 300, IMPORTANCE_CACHED = 400, IMPORTANCE_GONE = 1000;
        public String processName = Device.PACKAGE;
        public int pid = android.os.Process.myPid(), uid = android.os.Process.myUid(), importance = IMPORTANCE_FOREGROUND, lastTrimLevel;
        public String[] pkgList = {Device.PACKAGE};
    }
    public static class RunningServiceInfo { public android.content.ComponentName service; public boolean foreground, started; }
    public static class RunningTaskInfo extends TaskInfo {}
    public static class TaskDescription { public TaskDescription() {} public TaskDescription(String label) {} }

    public List<ApplicationExitInfo> getHistoricalProcessExitReasons(String packageName, int pid, int maxNum) { return new ArrayList<>(); }
    public boolean isLowRamDevice() { return Device.lowRam(); }
    public void getMemoryInfo(MemoryInfo outInfo) {
        outInfo.totalMem = Device.ramBytes();
        outInfo.availMem = outInfo.totalMem / 2;
        outInfo.threshold = 150L * 1024 * 1024;
        outInfo.lowMemory = false;
    }
    public int getMemoryClass() { return 256; }
    public int getLargeMemoryClass() { return 512; }
    public static void getMyMemoryState(RunningAppProcessInfo outState) {}
    public List<RunningAppProcessInfo> getRunningAppProcesses() { List<RunningAppProcessInfo> l = new ArrayList<>(); l.add(new RunningAppProcessInfo()); return l; }
    public List<RunningServiceInfo> getRunningServices(int maxNum) { return new ArrayList<>(); }
    public List<RunningTaskInfo> getRunningTasks(int maxNum) { return new ArrayList<>(); }
    public List<ActivityManager.AppTask> getAppTasks() { return new ArrayList<>(); }
    public static class AppTask {}
    public void killBackgroundProcesses(String packageName) {}
    public boolean isBackgroundRestricted() { return false; }
    public static boolean isUserAMonkey() { return false; }
    public static boolean isRunningInTestHarness() { return false; }
    public int getLockTaskModeState() { return 0; }
    public boolean clearApplicationUserData() { return false; }
    public void moveTaskToFront(int taskId, int flags) {}
}
