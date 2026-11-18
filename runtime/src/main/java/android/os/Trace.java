package android.os;

/** Systrace sections: nothing records them on the simulated TV. */
public final class Trace {
    public static final long TRACE_TAG_NEVER = 0, TRACE_TAG_ALWAYS = 1, TRACE_TAG_GRAPHICS = 2, TRACE_TAG_INPUT = 4, TRACE_TAG_VIEW = 8,
        TRACE_TAG_WINDOW_MANAGER = 32, TRACE_TAG_ACTIVITY_MANAGER = 64, TRACE_TAG_AUDIO = 256, TRACE_TAG_VIDEO = 512, TRACE_TAG_APP = 4096,
        TRACE_TAG_RESOURCES = 8192, TRACE_TAG_DALVIK = 16384, TRACE_TAG_SYSTEM_SERVER = 524288, TRACE_TAG_DATABASE = 1048576, TRACE_TAG_NETWORK = 2097152;

    private Trace() {}

    public static boolean isEnabled() { return false; }
    public static boolean isTagEnabled(long traceTag) { return false; }
    public static void beginSection(String sectionName) {}
    public static void endSection() {}
    public static void beginAsyncSection(String methodName, int cookie) {}
    public static void endAsyncSection(String methodName, int cookie) {}
    public static void setCounter(String counterName, long counterValue) {}
    public static void traceBegin(long traceTag, String methodName) {}
    public static void traceEnd(long traceTag) {}
    public static void traceCounter(long traceTag, String counterName, int counterValue) {}
    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {}
    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {}
    public static void asyncTraceForTrackBegin(long traceTag, String trackName, String methodName, int cookie) {}
    public static void asyncTraceForTrackEnd(long traceTag, String trackName, int cookie) {}
    public static void instant(long traceTag, String methodName) {}
    public static void instantForTrack(long traceTag, String trackName, String methodName) {}
    public static void setAppTracingAllowed(boolean allowed) {}
    public static void setTracingEnabled(boolean enabled, int debugFlags) {}
}
