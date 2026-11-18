package android.os;

public final class Message {
    public int what, arg1, arg2;
    public Object obj;
    Handler target;
    Runnable callback;
    Bundle data;
    public long when;

    public static Message obtain() { return new Message(); }
    public static Message obtain(Handler h) { Message m = new Message(); m.target = h; return m; }
    public static Message obtain(Handler h, Runnable callback) { Message m = obtain(h); m.callback = callback; return m; }
    public static Message obtain(Message o) { Message m = new Message(); m.what = o.what; m.arg1 = o.arg1; m.arg2 = o.arg2; m.obj = o.obj; m.target = o.target; m.callback = o.callback; m.data = o.data; return m; }
    public static Message obtain(Handler h, int what) { Message m = obtain(h); m.what = what; return m; }
    public static Message obtain(Handler h, int what, Object obj) { Message m = obtain(h, what); m.obj = obj; return m; }
    public static Message obtain(Handler h, int what, int arg1, int arg2) { Message m = obtain(h, what); m.arg1 = arg1; m.arg2 = arg2; return m; }
    public static Message obtain(Handler h, int what, int arg1, int arg2, Object obj) { Message m = obtain(h, what, arg1, arg2); m.obj = obj; return m; }
    public Handler getTarget() { return target; }
    public Runnable getCallback() { return callback; }
    public void sendToTarget() { target.sendMessage(this); }
    public void setTarget(Handler h) { target = h; }
    public Bundle getData() { if (data == null) data = new Bundle(); return data; }
    public Bundle peekData() { return data; }
    public void setData(Bundle b) { data = b; }
    public long getWhen() { return when; }
    public boolean isAsynchronous() { return false; }
    public void setAsynchronous(boolean async) {}
    public void copyFrom(Message o) { what = o.what; arg1 = o.arg1; arg2 = o.arg2; obj = o.obj; data = o.data; }
    public void recycle() {}
}
