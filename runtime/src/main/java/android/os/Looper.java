package android.os;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** A looper is a thread that runs posted work in order. The main looper is the AWT event thread, where Compose for Desktop runs. */
public final class Looper {
    private static final Looper MAIN = new Looper(null);
    private static final ThreadLocal<Looper> MINE = new ThreadLocal<>();
    static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "looper-timer");
        t.setDaemon(true);
        return t;
    });

    /** Null for the main looper. */
    final java.util.concurrent.ExecutorService own;
    volatile Thread thread;

    Looper(String name) {
        // The thread knows its looper from the start; if work ever ends one, its replacement knows it too.
        own = name == null ? null : Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(() -> { MINE.set(this); r.run(); }, name);
            t.setDaemon(true);
            thread = t;
            return t;
        });
    }

    public static Looper getMainLooper() { return MAIN; }
    public static Looper myLooper() { return java.awt.EventQueue.isDispatchThread() ? MAIN : MINE.get(); }
    public static void prepare() { if (myLooper() == null) MINE.set(new Looper(Thread.currentThread().getName() + "-looper")); }
    public static void prepareMainLooper() {}
    public static void loop() {}

    void run(Runnable r) {
        if (own == null) java.awt.EventQueue.invokeLater(r);
        else if (!own.isShutdown()) own.execute(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                // Android would end the app here; the simulator says so and keeps the looper going.
                android.util.Log.e("Simulator", "Uncaught on " + Thread.currentThread().getName(), t);
            }
        });
    }

    public boolean isCurrentThread() { return own == null ? java.awt.EventQueue.isDispatchThread() : Thread.currentThread() == thread; }
    public Thread getThread() { return thread; }

    /** The desktop host calls this on the AWT event thread, which is the simulated TV's main thread. */
    public static void setMainThread(Thread t) { MAIN.thread = t; }
    public void quit() { if (own != null) own.shutdownNow(); }
    public void quitSafely() { if (own != null) own.shutdown(); }
}
