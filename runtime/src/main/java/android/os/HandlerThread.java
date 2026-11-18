package android.os;

public class HandlerThread extends Thread {
    private Looper looper;
    public HandlerThread(String name) { super(name); }
    public HandlerThread(String name, int priority) { super(name); }
    @Override public synchronized void start() { if (looper == null) looper = new Looper(getName()); }
    @Override public void run() {}
    public Looper getLooper() { if (looper == null) start(); return looper; }
    public boolean quit() { if (looper != null) looper.quit(); return true; }
    public boolean quitSafely() { if (looper != null) looper.quitSafely(); return true; }
    public int getThreadId() { return 0; }
}
