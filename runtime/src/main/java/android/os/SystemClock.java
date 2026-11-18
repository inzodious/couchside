package android.os;

public final class SystemClock {
    private static final long START = System.nanoTime();
    private SystemClock() {}
    public static long uptimeMillis() { return (System.nanoTime() - START) / 1_000_000L; }
    public static long elapsedRealtime() { return uptimeMillis(); }
    public static long elapsedRealtimeNanos() { return System.nanoTime() - START; }
    public static long uptimeNanos() { return System.nanoTime() - START; }
    public static long currentThreadTimeMillis() { return uptimeMillis(); }
    public static long currentThreadTimeMicro() { return uptimeNanos() / 1000L; }
    public static long currentTimeMicro() { return System.currentTimeMillis() * 1000L; }
    public static boolean setCurrentTimeMillis(long millis) { return false; }
    public static java.time.Clock currentNetworkTimeClock() { return java.time.Clock.systemUTC(); }
    public static java.time.Clock currentGnssTimeClock() { return java.time.Clock.systemUTC(); }
    public static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }
}
