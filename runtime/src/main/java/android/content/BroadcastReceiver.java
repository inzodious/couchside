package android.content;

import android.os.Bundle;

public abstract class BroadcastReceiver {
    public static class PendingResult {
        public final void finish() {}
        public final void setResultCode(int code) {}
        public final void setResultData(String data) {}
    }

    private int resultCode;
    private String resultData;
    private Bundle resultExtras;
    private boolean ordered, aborted;

    public BroadcastReceiver() {}
    public abstract void onReceive(Context context, Intent intent);
    public final PendingResult goAsync() { return new PendingResult(); }
    public final void setResultCode(int code) { resultCode = code; }
    public final int getResultCode() { return resultCode; }
    public final void setResultData(String data) { resultData = data; }
    public final String getResultData() { return resultData; }
    public final void setResultExtras(Bundle extras) { resultExtras = extras; }
    public final Bundle getResultExtras(boolean makeMap) { if (resultExtras == null && makeMap) resultExtras = new Bundle(); return resultExtras; }
    public final void setResult(int code, String data, Bundle extras) { resultCode = code; resultData = data; resultExtras = extras; }
    public final boolean isOrderedBroadcast() { return ordered; }
    public final void setOrderedHint(boolean isOrdered) { ordered = isOrdered; }
    public final boolean isInitialStickyBroadcast() { return false; }
    public final void abortBroadcast() { aborted = true; }
    public final boolean getAbortBroadcast() { return aborted; }
    public final void clearAbortBroadcast() { aborted = false; }
}
