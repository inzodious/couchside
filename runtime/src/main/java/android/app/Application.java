package android.app;

import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class Application extends ContextWrapper {
    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);
        void onActivityStarted(Activity activity);
        void onActivityResumed(Activity activity);
        void onActivityPaused(Activity activity);
        void onActivityStopped(Activity activity);
        void onActivitySaveInstanceState(Activity activity, Bundle outState);
        void onActivityDestroyed(Activity activity);
        default void onActivityPreCreated(Activity activity, Bundle savedInstanceState) {}
        default void onActivityPostCreated(Activity activity, Bundle savedInstanceState) {}
        default void onActivityPreStarted(Activity activity) {}
        default void onActivityPostStarted(Activity activity) {}
        default void onActivityPreResumed(Activity activity) {}
        default void onActivityPostResumed(Activity activity) {}
        default void onActivityPrePaused(Activity activity) {}
        default void onActivityPostPaused(Activity activity) {}
        default void onActivityPreStopped(Activity activity) {}
        default void onActivityPostStopped(Activity activity) {}
        default void onActivityPreDestroyed(Activity activity) {}
        default void onActivityPostDestroyed(Activity activity) {}
    }

    public final List<ActivityLifecycleCallbacks> lifecycleCallbacks = new ArrayList<>();

    public Application() { super(null); }
    public void onCreate() {}
    public void onTerminate() {}
    public void onConfigurationChanged(Configuration newConfig) {}
    public void onLowMemory() {}
    public void onTrimMemory(int level) {}
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) { lifecycleCallbacks.add(callback); }
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) { lifecycleCallbacks.remove(callback); }
    public static String getProcessName() { return couchside.shim.Device.PACKAGE; }
    @Override public android.content.Context getApplicationContext() { return this; }
}
