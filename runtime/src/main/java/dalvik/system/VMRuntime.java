package dalvik.system;

/**
 * ART's runtime, for the framework classes from android-all that ask it for arrays (SpannableStringBuilder, ArrayMap's
 * helpers): plain JVM arrays, and nothing for the garbage collector hints.
 */
public final class VMRuntime {
    public static final int SDK_VERSION_CUR_DEVELOPMENT = 10000;
    private static final VMRuntime RUNTIME = new VMRuntime();
    private int targetSdk = android.os.Build.VERSION.SDK_INT;

    private VMRuntime() {}

    public static VMRuntime getRuntime() { return RUNTIME; }
    public static int getSdkVersion() { return android.os.Build.VERSION.SDK_INT; }
    public synchronized int getTargetSdkVersion() { return targetSdk; }
    public synchronized void setTargetSdkVersion(int v) { targetSdk = v; }
    public synchronized void setDisabledCompatChanges(long[] changes) {}

    public Object newUnpaddedArray(Class<?> type, int length) { return java.lang.reflect.Array.newInstance(type, length); }
    public Object newNonMovableArray(Class<?> type, int length) { return java.lang.reflect.Array.newInstance(type, length); }
    public long addressOf(Object array) { return 0; }

    public String[] properties() { return new String[0]; }
    public String bootClassPath() { return ""; }
    public String classPath() { return System.getProperty("java.class.path", ""); }
    public String vmVersion() { return "2.1.0"; }
    public String vmLibrary() { return "libart.so"; }
    public String vmInstructionSet() { return "x86_64"; }
    public boolean is64Bit() { return true; }
    public boolean isCheckJniEnabled() { return false; }
    public boolean isNativeDebuggable() { return false; }
    public boolean isJavaDebuggable() { return false; }
    public float getTargetHeapUtilization() { return 0.75f; }
    public float setTargetHeapUtilization(float v) { return 0.75f; }
    public long getFinalizerTimeoutMs() { return 10_000; }
    public long getMinimumHeapSize() { return 0; }
    public long setMinimumHeapSize(long v) { return 0; }
    public void gcSoftReferences() {}
    public void runFinalizationSync() {}
    public boolean trackExternalAllocation(long size) { return true; }
    public void trackExternalFree(long size) {}
    public long getExternalBytesAllocated() { return 0; }
    public void setHiddenApiExemptions(String[] exemptions) {}
    public void setHiddenApiAccessLogSamplingRate(int rate) {}
    public void clearGrowthLimit() {}
    public void clampGrowthLimit() {}
    public void registerNativeAllocation(long bytes) {}
    public void registerNativeAllocation(int bytes) {}
    public void registerNativeFree(long bytes) {}
    public void registerNativeFree(int bytes) {}
    public void notifyNativeAllocation() {}
    public void notifyNativeAllocationsInternal() {}
    public static void runFinalization(long timeout) {}
    public void requestConcurrentGC() {}
    public void requestHeapTrim() {}
    public void trimHeap() {}
    public void updateProcessState(int state) {}
    public void notifyStartupCompleted() {}
    public void preloadDexCaches() {}
    public static String getInstructionSet(String abi) { return "x86_64"; }
    public static boolean is64BitInstructionSet(String set) { return true; }
    public static boolean is64BitAbi(String abi) { return true; }
    public static String getCurrentInstructionSet() { return "x86_64"; }
    public static void registerSensitiveThread() {}
    public static void setSystemDaemonThreadPriority() {}
    public static void setNonSdkApiUsageConsumer(java.util.function.Consumer<String> consumer) {}
    public static void setDedupeHiddenApiWarnings(boolean dedupe) {}
    public static void setProcessPackageName(String name) {}
    public static void setProcessDataDirectory(String dir) {}
    public static void bootCompleted() {}
    public static void resetJitCounters() {}
}
