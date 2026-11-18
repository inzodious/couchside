package android.util;

/** Logcat, on the terminal. COUCHSIDE_LOG=v|d|i|w|e sets how much (default i). */
public final class Log {
    public static final int VERBOSE = 2, DEBUG = 3, INFO = 4, WARN = 5, ERROR = 6, ASSERT = 7;
    private static final int LEVEL = level(System.getenv("COUCHSIDE_LOG"));
    private Log() {}

    private static int level(String s) {
        if (s == null || s.isEmpty()) return INFO;
        switch (Character.toLowerCase(s.charAt(0))) { case 'v': return VERBOSE; case 'd': return DEBUG; case 'w': return WARN; case 'e': return ERROR; default: return INFO; }
    }
    private static int out(int priority, String tag, String msg, Throwable tr) {
        if (priority < LEVEL) return 0;
        String line = "VDIWEA".charAt(Math.min(priority, ASSERT) - 2) + "/" + tag + ": " + msg;
        synchronized (System.err) {
            System.err.println(line);
            if (tr != null) tr.printStackTrace(System.err);
        }
        return line.length();
    }
    public static int v(String tag, String msg) { return out(VERBOSE, tag, msg, null); }
    public static int v(String tag, String msg, Throwable tr) { return out(VERBOSE, tag, msg, tr); }
    public static int d(String tag, String msg) { return out(DEBUG, tag, msg, null); }
    public static int d(String tag, String msg, Throwable tr) { return out(DEBUG, tag, msg, tr); }
    public static int i(String tag, String msg) { return out(INFO, tag, msg, null); }
    public static int i(String tag, String msg, Throwable tr) { return out(INFO, tag, msg, tr); }
    public static int w(String tag, String msg) { return out(WARN, tag, msg, null); }
    public static int w(String tag, String msg, Throwable tr) { return out(WARN, tag, msg, tr); }
    public static int w(String tag, Throwable tr) { return out(WARN, tag, "", tr); }
    public static int e(String tag, String msg) { return out(ERROR, tag, msg, null); }
    public static int e(String tag, String msg, Throwable tr) { return out(ERROR, tag, msg, tr); }
    public static int wtf(String tag, String msg) { return out(ASSERT, tag, msg, null); }
    public static int wtf(String tag, Throwable tr) { return out(ASSERT, tag, "", tr); }
    public static int wtf(String tag, String msg, Throwable tr) { return out(ASSERT, tag, msg, tr); }
    public static int println(int priority, String tag, String msg) { return out(priority, tag, msg, null); }
    public static boolean isLoggable(String tag, int level) { return level >= LEVEL; }
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) return "";
        java.io.StringWriter sw = new java.io.StringWriter();
        tr.printStackTrace(new java.io.PrintWriter(sw, true));
        return sw.toString();
    }
}
