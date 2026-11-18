package android.content;

public final class ComponentName implements Comparable<ComponentName> {
    private final String pkg, cls;
    public ComponentName(String pkg, String cls) { this.pkg = pkg; this.cls = cls; }
    public ComponentName(Context pkg, String cls) { this(pkg.getPackageName(), cls); }
    public ComponentName(Context pkg, Class<?> cls) { this(pkg.getPackageName(), cls.getName()); }
    public static ComponentName createRelative(String pkg, String cls) { return new ComponentName(pkg, cls.startsWith(".") ? pkg + cls : cls); }
    public static ComponentName unflattenFromString(String str) {
        int sep = str.indexOf('/');
        if (sep < 0 || sep + 1 >= str.length()) return null;
        return createRelative(str.substring(0, sep), str.substring(sep + 1));
    }
    public String getPackageName() { return pkg; }
    public String getClassName() { return cls; }
    public String getShortClassName() { return cls.startsWith(pkg) && cls.length() > pkg.length() && cls.charAt(pkg.length()) == '.' ? cls.substring(pkg.length()) : cls; }
    public String flattenToString() { return pkg + "/" + cls; }
    public String flattenToShortString() { return pkg + "/" + getShortClassName(); }
    @Override public boolean equals(Object o) { return o instanceof ComponentName c && c.pkg.equals(pkg) && c.cls.equals(cls); }
    @Override public int hashCode() { return pkg.hashCode() * 31 + cls.hashCode(); }
    @Override public int compareTo(ComponentName o) { int v = pkg.compareTo(o.pkg); return v != 0 ? v : cls.compareTo(o.cls); }
    @Override public String toString() { return "ComponentInfo{" + pkg + "/" + cls + "}"; }
}
