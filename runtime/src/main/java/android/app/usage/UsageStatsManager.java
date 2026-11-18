package android.app.usage;

import couchside.shim.Apps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** How long each app has been used on the simulated TV, as the desktop host counts it (Apps.kt). */
public final class UsageStatsManager {
    public static final int INTERVAL_DAILY = 0, INTERVAL_WEEKLY = 1, INTERVAL_MONTHLY = 2, INTERVAL_YEARLY = 3, INTERVAL_BEST = 4;
    public static final int STANDBY_BUCKET_ACTIVE = 10, STANDBY_BUCKET_WORKING_SET = 20, STANDBY_BUCKET_FREQUENT = 30, STANDBY_BUCKET_RARE = 40, STANDBY_BUCKET_RESTRICTED = 45;

    public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) { return Apps.usage(beginTime, endTime); }
    public Map<String, UsageStats> queryAndAggregateUsageStats(long beginTime, long endTime) {
        Map<String, UsageStats> out = new HashMap<>();
        for (UsageStats s : Apps.usage(beginTime, endTime)) out.put(s.packageName, s);
        return out;
    }
    public UsageEvents queryEvents(long beginTime, long endTime) { return null; }
    public boolean isAppInactive(String packageName) { return false; }
    public int getAppStandbyBucket() { return STANDBY_BUCKET_ACTIVE; }
}
