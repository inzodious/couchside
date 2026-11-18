package android.os;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Handler {
    public interface Callback { boolean handleMessage(Message msg); }

    private static final class Pending {
        final Runnable work; final Object token; final int what; final boolean message;
        ScheduledFuture<?> timer;
        volatile boolean cancelled;
        Pending(Runnable work, Object token, int what, boolean message) { this.work = work; this.token = token; this.what = what; this.message = message; }
    }

    private final Looper looper;
    private final Callback callback;
    private final List<Pending> pending = new ArrayList<>();

    public Handler() { this(Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper(), null); }
    public Handler(Callback callback) { this(Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper(), callback); }
    public Handler(Looper looper) { this(looper, null); }
    public Handler(Looper looper, Callback callback) { this.looper = looper; this.callback = callback; }

    public static Handler createAsync(Looper looper) { return new Handler(looper); }
    public final Looper getLooper() { return looper; }
    public void handleMessage(Message msg) {}
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) msg.callback.run();
        else if (callback == null || !callback.handleMessage(msg)) handleMessage(msg);
    }

    private boolean enqueue(Runnable work, Object token, int what, boolean message, long delayMillis) {
        Pending p = new Pending(work, token, what, message);
        Runnable run = () -> {
            synchronized (pending) { pending.remove(p); }
            if (!p.cancelled) work.run();
        };
        synchronized (pending) { pending.add(p); }
        if (delayMillis <= 0) looper.run(run);
        else p.timer = Looper.TIMER.schedule(() -> looper.run(run), delayMillis, TimeUnit.MILLISECONDS);
        return true;
    }

    private void cancel(java.util.function.Predicate<Pending> which) {
        synchronized (pending) {
            for (Iterator<Pending> it = pending.iterator(); it.hasNext();) {
                Pending p = it.next();
                if (!which.test(p)) continue;
                p.cancelled = true;
                if (p.timer != null) p.timer.cancel(false);
                it.remove();
            }
        }
    }

    public final boolean post(Runnable r) { return enqueue(r, null, 0, false, 0); }
    public final boolean postDelayed(Runnable r, long delayMillis) { return enqueue(r, null, 0, false, delayMillis); }
    public final boolean postDelayed(Runnable r, Object token, long delayMillis) { return enqueue(r, token, 0, false, delayMillis); }
    public final boolean postAtTime(Runnable r, long uptimeMillis) { return enqueue(r, null, 0, false, uptimeMillis - SystemClock.uptimeMillis()); }
    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) { return enqueue(r, token, 0, false, uptimeMillis - SystemClock.uptimeMillis()); }
    public final boolean postAtFrontOfQueue(Runnable r) { return enqueue(r, null, 0, false, 0); }
    public final void removeCallbacks(Runnable r) { cancel(p -> !p.message && p.work == r); }
    public final void removeCallbacks(Runnable r, Object token) { cancel(p -> !p.message && p.work == r && (token == null || p.token == token)); }
    public final void removeCallbacksAndMessages(Object token) { cancel(p -> token == null || p.token == token); }
    public final boolean hasCallbacks(Runnable r) { synchronized (pending) { return pending.stream().anyMatch(p -> !p.message && p.work == r); } }

    public final Message obtainMessage() { return Message.obtain(this); }
    public final Message obtainMessage(int what) { return Message.obtain(this, what); }
    public final Message obtainMessage(int what, Object obj) { return Message.obtain(this, what, obj); }
    public final Message obtainMessage(int what, int arg1, int arg2) { return Message.obtain(this, what, arg1, arg2); }
    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) { return Message.obtain(this, what, arg1, arg2, obj); }
    public final boolean sendMessage(Message msg) { return sendMessageDelayed(msg, 0); }
    public final boolean sendEmptyMessage(int what) { return sendEmptyMessageDelayed(what, 0); }
    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) { return sendMessageDelayed(Message.obtain(this, what), delayMillis); }
    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        msg.target = this;
        return enqueue(() -> dispatchMessage(msg), msg.obj, msg.what, true, delayMillis);
    }
    public final boolean sendMessageAtTime(Message msg, long uptimeMillis) { return sendMessageDelayed(msg, uptimeMillis - SystemClock.uptimeMillis()); }
    public final boolean sendMessageAtFrontOfQueue(Message msg) { return sendMessageDelayed(msg, 0); }
    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) { return sendMessageAtTime(Message.obtain(this, what), uptimeMillis); }
    public final boolean executeOrSendMessage(Message msg) {
        if (looper.isCurrentThread()) { dispatchMessage(msg); return true; }
        return sendMessage(msg);
    }
    public final boolean runWithScissors(Runnable r, long timeout) {
        if (looper.isCurrentThread()) { r.run(); return true; }
        java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        post(() -> { try { r.run(); } finally { done.countDown(); } });
        try { return timeout <= 0 ? waitFor(done) : done.await(timeout, java.util.concurrent.TimeUnit.MILLISECONDS); } catch (InterruptedException e) { return false; }
    }
    private static boolean waitFor(java.util.concurrent.CountDownLatch done) throws InterruptedException { done.await(); return true; }
    public static Handler createAsync(Looper looper, Callback callback) { return new Handler(looper, callback); }
    public String getMessageName(Message message) { return message.callback != null ? message.callback.getClass().getName() : "0x" + Integer.toHexString(message.what); }
    public final void removeMessages(int what) { cancel(p -> p.message && p.what == what); }
    public final void removeMessages(int what, Object object) { cancel(p -> p.message && p.what == what && (object == null || p.token == object)); }
    public final boolean hasMessages(int what) { synchronized (pending) { return pending.stream().anyMatch(p -> p.message && p.what == what); } }
}
