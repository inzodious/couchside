package android.view;

public abstract class InputEvent {
    public abstract int getDeviceId();
    public abstract long getEventTime();
    public abstract int getSource();
    public final InputDevice getDevice() { return null; }
    public boolean isFromSource(int source) { return (getSource() & source) == source; }
}
