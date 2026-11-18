package android.app.usage;

public final class UsageStats {
    public String packageName;
    public long firstTimeStamp, lastTimeStamp, lastTimeUsed, totalTimeInForeground, lastTimeVisible, totalTimeVisible;
    public UsageStats() {}
    public UsageStats(UsageStats o) { packageName = o.packageName; firstTimeStamp = o.firstTimeStamp; lastTimeStamp = o.lastTimeStamp; lastTimeUsed = o.lastTimeUsed; totalTimeInForeground = o.totalTimeInForeground; }
    public String getPackageName() { return packageName; }
    public long getFirstTimeStamp() { return firstTimeStamp; }
    public long getLastTimeStamp() { return lastTimeStamp; }
    public long getLastTimeUsed() { return lastTimeUsed; }
    public long getLastTimeVisible() { return lastTimeVisible == 0 ? lastTimeUsed : lastTimeVisible; }
    public long getTotalTimeInForeground() { return totalTimeInForeground; }
    public long getTotalTimeVisible() { return totalTimeVisible == 0 ? totalTimeInForeground : totalTimeVisible; }
    public long getLastTimeForegroundServiceUsed() { return 0; }
    public long getTotalTimeForegroundServiceUsed() { return 0; }
    public void add(UsageStats right) { totalTimeInForeground += right.totalTimeInForeground; lastTimeUsed = Math.max(lastTimeUsed, right.lastTimeUsed); }
}
