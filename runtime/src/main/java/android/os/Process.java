package android.os;

public class Process {
    public static final int THREAD_PRIORITY_DEFAULT = 0, THREAD_PRIORITY_BACKGROUND = 10, THREAD_PRIORITY_LOWEST = 19, THREAD_PRIORITY_FOREGROUND = -2,
        THREAD_PRIORITY_DISPLAY = -4, THREAD_PRIORITY_URGENT_DISPLAY = -8, THREAD_PRIORITY_AUDIO = -16, THREAD_PRIORITY_URGENT_AUDIO = -19,
        THREAD_PRIORITY_MORE_FAVORABLE = -1, THREAD_PRIORITY_LESS_FAVORABLE = 1, THREAD_PRIORITY_VIDEO = -10;
    public static final int FIRST_APPLICATION_UID = 10000, SYSTEM_UID = 1000, INVALID_UID = -1;
    public static int myPid() { return (int) ProcessHandle.current().pid(); }
    public static int myTid() { return (int) Thread.currentThread().getId(); }
    public static int myUid() { return 10001; }
    public static UserHandle myUserHandle() { return null; }
    public static void setThreadPriority(int priority) {}
    public static void setThreadPriority(int tid, int priority) {}
    public static int getThreadPriority(int tid) { return 0; }
    public static void killProcess(int pid) { if (pid == myPid()) System.exit(0); }
    public static long getElapsedCpuTime() { return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime(); }
    public static long getStartElapsedRealtime() { return 0L; }
    public static long getStartUptimeMillis() { return 0L; }
    public static boolean is64Bit() { return true; }
}
