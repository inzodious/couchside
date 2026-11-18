package android.content;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class Intent implements android.os.Parcelable, Cloneable {
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(android.os.Parcel dest, int flags) {}
    public static final String ACTION_MAIN = "android.intent.action.MAIN", ACTION_VIEW = "android.intent.action.VIEW", ACTION_SEND = "android.intent.action.SEND",
        ACTION_SEARCH = "android.intent.action.SEARCH", ACTION_ASSIST = "android.intent.action.ASSIST", ACTION_VOICE_COMMAND = "android.intent.action.VOICE_COMMAND",
        ACTION_DELETE = "android.intent.action.DELETE", ACTION_UNINSTALL_PACKAGE = "android.intent.action.UNINSTALL_PACKAGE", ACTION_INSTALL_PACKAGE = "android.intent.action.INSTALL_PACKAGE",
        ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED", ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED",
        ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON", ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF", ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT",
        ACTION_DREAMING_STARTED = "android.intent.action.DREAMING_STARTED", ACTION_DREAMING_STOPPED = "android.intent.action.DREAMING_STOPPED",
        ACTION_PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED", ACTION_PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED", ACTION_PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED",
        ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED", ACTION_MY_PACKAGE_REPLACED = "android.intent.action.MY_PACKAGE_REPLACED",
        ACTION_PACKAGE_FULLY_REMOVED = "android.intent.action.PACKAGE_FULLY_REMOVED", ACTION_TIME_TICK = "android.intent.action.TIME_TICK", ACTION_TIME_CHANGED = "android.intent.action.TIME_SET",
        ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED", ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS",
        ACTION_ALL_APPS = "android.intent.action.ALL_APPS", ACTION_APPLICATION_PREFERENCES = "android.intent.action.APPLICATION_PREFERENCES",
        ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN", ACTION_HDMI_AUDIO_PLUG = "android.media.action.HDMI_AUDIO_PLUG";
    public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT", CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER", CATEGORY_HOME = "android.intent.category.HOME",
        CATEGORY_LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER", CATEGORY_LEANBACK_SETTINGS = "android.intent.category.LEANBACK_SETTINGS",
        CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE", CATEGORY_INFO = "android.intent.category.INFO";
    public static final String EXTRA_TEXT = "android.intent.extra.TEXT", EXTRA_TITLE = "android.intent.extra.TITLE", EXTRA_INTENT = "android.intent.extra.INTENT",
        EXTRA_STREAM = "android.intent.extra.STREAM", EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME", EXTRA_REPLACING = "android.intent.extra.REPLACING",
        EXTRA_RETURN_RESULT = "android.intent.extra.RETURN_RESULT", EXTRA_UID = "android.intent.extra.UID", EXTRA_KEY_EVENT = "android.intent.extra.KEY_EVENT",
        EXTRA_NOT_UNKNOWN_SOURCE = "android.intent.extra.NOT_UNKNOWN_SOURCE", EXTRA_DATA_REMOVED = "android.intent.extra.DATA_REMOVED";
    public static final int FLAG_ACTIVITY_NEW_TASK = 0x10000000, FLAG_ACTIVITY_CLEAR_TOP = 0x04000000, FLAG_ACTIVITY_SINGLE_TOP = 0x20000000, FLAG_ACTIVITY_NO_ANIMATION = 0x00010000,
        FLAG_ACTIVITY_CLEAR_TASK = 0x00008000, FLAG_ACTIVITY_REORDER_TO_FRONT = 0x00020000, FLAG_ACTIVITY_NO_HISTORY = 0x40000000, FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 0x00800000,
        FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 0x00200000, FLAG_ACTIVITY_BROUGHT_TO_FRONT = 0x00400000, FLAG_ACTIVITY_MULTIPLE_TASK = 0x08000000, FLAG_ACTIVITY_TASK_ON_HOME = 0x00004000,
        FLAG_ACTIVITY_PREVIOUS_IS_TOP = 0x01000000, FLAG_ACTIVITY_NO_USER_ACTION = 0x00040000, FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY = 0x00100000,
        FLAG_GRANT_READ_URI_PERMISSION = 1, FLAG_GRANT_WRITE_URI_PERMISSION = 2, FLAG_INCLUDE_STOPPED_PACKAGES = 0x20, FLAG_RECEIVER_FOREGROUND = 0x10000000,
        FLAG_RECEIVER_REGISTERED_ONLY = 0x40000000, FLAG_FROM_BACKGROUND = 4;
    public static final int URI_INTENT_SCHEME = 1;

    private String action, type, pkg;
    private Uri data;
    private ComponentName component;
    private int flags;
    private Set<String> categories;
    private Bundle extras;
    private Intent selector;

    public Intent() {}
    public Intent(Intent o) {
        action = o.action; type = o.type; pkg = o.pkg; data = o.data; component = o.component; flags = o.flags;
        if (o.categories != null) categories = new LinkedHashSet<>(o.categories);
        if (o.extras != null) extras = new Bundle(o.extras);
    }
    public Intent(String action) { this.action = action; }
    public Intent(String action, Uri uri) { this.action = action; this.data = uri; }
    public Intent(Context packageContext, Class<?> cls) { component = new ComponentName(packageContext, cls); }
    public Intent(String action, Uri uri, Context packageContext, Class<?> cls) { this(packageContext, cls); this.action = action; this.data = uri; }

    public static Intent makeMainActivity(ComponentName mainActivity) { return new Intent(ACTION_MAIN).addCategory(CATEGORY_LAUNCHER).setComponent(mainActivity); }
    public static Intent createChooser(Intent target, CharSequence title) { return target; }
    @Override public Object clone() { return new Intent(this); }

    public String getAction() { return action; }
    public Intent setAction(String action) { this.action = action; return this; }
    public Uri getData() { return data; }
    public String getDataString() { return data == null ? null : data.toString(); }
    public String getScheme() { return data == null ? null : data.getScheme(); }
    public Intent setData(Uri data) { this.data = data; type = null; return this; }
    public String getType() { return type; }
    public Intent setType(String type) { this.type = type; data = null; return this; }
    public Intent setDataAndType(Uri data, String type) { this.data = data; this.type = type; return this; }
    public String getPackage() { return pkg; }
    public Intent setPackage(String packageName) { pkg = packageName; return this; }
    public ComponentName getComponent() { return component; }
    public Intent setComponent(ComponentName component) { this.component = component; return this; }
    public Intent setClassName(Context packageContext, String className) { component = new ComponentName(packageContext, className); return this; }
    public Intent setClassName(String packageName, String className) { component = new ComponentName(packageName, className); return this; }
    public Intent setClass(Context packageContext, Class<?> cls) { component = new ComponentName(packageContext, cls); return this; }
    public int getFlags() { return flags; }
    public Intent setFlags(int flags) { this.flags = flags; return this; }
    public Intent addFlags(int flags) { this.flags |= flags; return this; }
    public void removeFlags(int flags) { this.flags &= ~flags; }
    public Intent addCategory(String category) { if (categories == null) categories = new LinkedHashSet<>(); categories.add(category); return this; }
    public void removeCategory(String category) { if (categories != null) categories.remove(category); }
    public boolean hasCategory(String category) { return categories != null && categories.contains(category); }
    public Set<String> getCategories() { return categories; }
    public Intent getSelector() { return selector; }
    public void setSelector(Intent selector) { this.selector = selector; }
    public ComponentName resolveActivity(android.content.pm.PackageManager pm) {
        android.content.pm.ResolveInfo info = pm.resolveActivity(this, 0);
        return info == null || info.activityInfo == null ? null : new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
    }
    public android.content.pm.ActivityInfo resolveActivityInfo(android.content.pm.PackageManager pm, int flags) {
        android.content.pm.ResolveInfo info = pm.resolveActivity(this, flags);
        return info == null ? null : info.activityInfo;
    }
    public String toUri(int flags) { return "intent:" + (data == null ? "" : data) + "#Intent;" + (action == null ? "" : "action=" + action + ";") + "end"; }

    private Bundle x() { if (extras == null) extras = new Bundle(); return extras; }
    public Bundle getExtras() { return extras == null ? null : new Bundle(extras); }
    public boolean hasExtra(String name) { return extras != null && extras.containsKey(name); }
    public void removeExtra(String name) { if (extras != null) extras.remove(name); }
    public Intent replaceExtras(Bundle extras) { this.extras = extras == null ? null : new Bundle(extras); return this; }
    public Intent putExtras(Bundle extras) { x().putAll(extras); return this; }
    public Intent putExtras(Intent src) { if (src.extras != null) x().putAll(src.extras); return this; }
    public Intent putExtra(String name, boolean value) { x().putBoolean(name, value); return this; }
    public Intent putExtra(String name, byte value) { x().putByte(name, value); return this; }
    public Intent putExtra(String name, char value) { x().putChar(name, value); return this; }
    public Intent putExtra(String name, short value) { x().putShort(name, value); return this; }
    public Intent putExtra(String name, int value) { x().putInt(name, value); return this; }
    public Intent putExtra(String name, long value) { x().putLong(name, value); return this; }
    public Intent putExtra(String name, float value) { x().putFloat(name, value); return this; }
    public Intent putExtra(String name, double value) { x().putDouble(name, value); return this; }
    public Intent putExtra(String name, String value) { x().putString(name, value); return this; }
    public Intent putExtra(String name, CharSequence value) { x().putCharSequence(name, value); return this; }
    public Intent putExtra(String name, Parcelable value) { x().putParcelable(name, value); return this; }
    public Intent putExtra(String name, Parcelable[] value) { x().putParcelableArray(name, value); return this; }
    public Intent putExtra(String name, Serializable value) { x().putSerializable(name, value); return this; }
    public Intent putExtra(String name, boolean[] value) { x().putBooleanArray(name, value); return this; }
    public Intent putExtra(String name, byte[] value) { x().putByteArray(name, value); return this; }
    public Intent putExtra(String name, int[] value) { x().putIntArray(name, value); return this; }
    public Intent putExtra(String name, long[] value) { x().putLongArray(name, value); return this; }
    public Intent putExtra(String name, float[] value) { x().putFloatArray(name, value); return this; }
    public Intent putExtra(String name, String[] value) { x().putStringArray(name, value); return this; }
    public Intent putExtra(String name, Bundle value) { x().putBundle(name, value); return this; }
    public Intent putStringArrayListExtra(String name, ArrayList<String> value) { x().putStringArrayList(name, value); return this; }
    public Intent putIntegerArrayListExtra(String name, ArrayList<Integer> value) { x().putIntegerArrayList(name, value); return this; }
    public Intent putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) { x().putParcelableArrayList(name, value); return this; }
    public boolean getBooleanExtra(String name, boolean defaultValue) { return extras == null ? defaultValue : extras.getBoolean(name, defaultValue); }
    public int getIntExtra(String name, int defaultValue) { return extras == null ? defaultValue : extras.getInt(name, defaultValue); }
    public long getLongExtra(String name, long defaultValue) { return extras == null ? defaultValue : extras.getLong(name, defaultValue); }
    public float getFloatExtra(String name, float defaultValue) { return extras == null ? defaultValue : extras.getFloat(name, defaultValue); }
    public double getDoubleExtra(String name, double defaultValue) { return extras == null ? defaultValue : extras.getDouble(name, defaultValue); }
    public String getStringExtra(String name) { return extras == null ? null : extras.getString(name); }
    public CharSequence getCharSequenceExtra(String name) { return extras == null ? null : extras.getCharSequence(name); }
    public <T extends Parcelable> T getParcelableExtra(String name) { return extras == null ? null : extras.getParcelable(name); }
    public <T> T getParcelableExtra(String name, Class<T> clazz) { return extras == null ? null : extras.getParcelable(name, clazz); }
    public Parcelable[] getParcelableArrayExtra(String name) { return extras == null ? null : extras.getParcelableArray(name); }
    public <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(String name) { return extras == null ? null : extras.getParcelableArrayList(name); }
    public Serializable getSerializableExtra(String name) { return extras == null ? null : extras.getSerializable(name); }
    public <T extends Serializable> T getSerializableExtra(String name, Class<T> clazz) { return extras == null ? null : extras.getSerializable(name, clazz); }
    public String[] getStringArrayExtra(String name) { return extras == null ? null : extras.getStringArray(name); }
    public ArrayList<String> getStringArrayListExtra(String name) { return extras == null ? null : extras.getStringArrayList(name); }
    public ArrayList<Integer> getIntegerArrayListExtra(String name) { return extras == null ? null : extras.getIntegerArrayList(name); }
    public int[] getIntArrayExtra(String name) { return extras == null ? null : extras.getIntArray(name); }
    public long[] getLongArrayExtra(String name) { return extras == null ? null : extras.getLongArray(name); }
    public byte[] getByteArrayExtra(String name) { return extras == null ? null : extras.getByteArray(name); }
    public Bundle getBundleExtra(String name) { return extras == null ? null : extras.getBundle(name); }

    @Override public String toString() {
        return "Intent { " + (action == null ? "" : "act=" + action + " ") + (categories == null ? "" : "cat=" + categories + " ") + (data == null ? "" : "dat=" + data + " ")
            + (pkg == null ? "" : "pkg=" + pkg + " ") + (component == null ? "" : "cmp=" + component.flattenToShortString() + " ") + (extras == null ? "" : "(has extras) ") + "}";
    }
}
