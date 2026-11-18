package android.app;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public abstract class Service extends ContextWrapper {
    public static final int START_STICKY_COMPATIBILITY = 0, START_STICKY = 1, START_NOT_STICKY = 2, START_REDELIVER_INTENT = 3, START_FLAG_REDELIVERY = 1, START_FLAG_RETRY = 2;
    public static final int STOP_FOREGROUND_REMOVE = 1, STOP_FOREGROUND_DETACH = 2, STOP_FOREGROUND_LEGACY = 0;

    public Service() { super(null); }
    public final Application getApplication() { return couchside.shim.Device.application(); }
    public void onCreate() {}
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    public void onDestroy() {}
    public void onConfigurationChanged(Configuration newConfig) {}
    public void onLowMemory() {}
    public void onTrimMemory(int level) {}
    public abstract IBinder onBind(Intent intent);
    public boolean onUnbind(Intent intent) { return false; }
    public void onRebind(Intent intent) {}
    public void onTaskRemoved(Intent rootIntent) {}
    public void onTimeout(int startId) {}
    public void onTimeout(int startId, int fgsType) {}
    public final void stopSelf() { couchside.shim.Device.stopService(this); }
    public final void stopSelf(int startId) { stopSelf(); }
    public final boolean stopSelfResult(int startId) { stopSelf(); return true; }
    public final void startForeground(int id, Notification notification) {}
    public final void startForeground(int id, Notification notification, int foregroundServiceType) {}
    public final void stopForeground(boolean removeNotification) {}
    public final void stopForeground(int notificationBehavior) {}
    public final int getForegroundServiceType() { return 0; }
}
