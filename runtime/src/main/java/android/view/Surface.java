package android.view;

import android.os.Parcel;
import android.os.Parcelable;
import couchside.media.VideoOutput;

/**
 * A surface a decoder draws into (MediaCodec's output). On the simulated TV it's a [VideoOutput], which a video view
 * draws in Compose. Canvas drawing (lockCanvas) isn't there: apps that draw on a SurfaceView's canvas get nothing shown.
 */
public class Surface implements Parcelable {
    public static final int SCALING_MODE_FREEZE = 0, SCALING_MODE_SCALE_TO_WINDOW = 1, SCALING_MODE_SCALE_CROP = 2, SCALING_MODE_NO_SCALE_CROP = 3;
    public static final int ROTATION_0 = 0, ROTATION_90 = 1, ROTATION_180 = 2, ROTATION_270 = 3;
    public static final int FRAME_RATE_COMPATIBILITY_DEFAULT = 0, FRAME_RATE_COMPATIBILITY_FIXED_SOURCE = 1;
    public static final int CHANGE_FRAME_RATE_ONLY_IF_SEAMLESS = 0, CHANGE_FRAME_RATE_ALWAYS = 1;

    public static final Creator<Surface> CREATOR = new Creator<Surface>() {
        @Override public Surface createFromParcel(Parcel in) { return new Surface(); }
        @Override public Surface[] newArray(int size) { return new Surface[size]; }
    };

    public static class OutOfResourcesException extends RuntimeException {
        public OutOfResourcesException() {}
        public OutOfResourcesException(String name) { super(name); }
    }

    private final VideoOutput output;
    private volatile boolean valid = true;

    public Surface() { this(new VideoOutput()); }
    public Surface(VideoOutput output) { this.output = output; }
    public Surface(android.graphics.SurfaceTexture texture) { this(couchside.media.Textures.output(texture)); }

    /** Where this surface's pictures go. */
    public VideoOutput output() { return output; }

    public void release() { valid = false; }
    public void destroy() { release(); }
    public boolean isValid() { return valid; }
    public int getGenerationId() { return 1; }
    public long getNextFrameNumber() { return 0; }
    public boolean isConsumerRunningBehind() { return false; }
    public void setFrameRate(float frameRate, int compatibility) {}
    public void setFrameRate(float frameRate, int compatibility, int changeFrameRateStrategy) {}
    public void clearFrameRate() {}
    public android.graphics.Canvas lockCanvas(android.graphics.Rect dirty) { throw new OutOfResourcesException("No canvas surfaces on the simulated TV"); }
    public android.graphics.Canvas lockHardwareCanvas() { throw new OutOfResourcesException("No canvas surfaces on the simulated TV"); }
    public void unlockCanvasAndPost(android.graphics.Canvas canvas) {}
    public void unlockCanvas(android.graphics.Canvas canvas) {}
    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {}
    public void readFromParcel(Parcel source) {}
    @Override public String toString() { return "Surface(" + output + ")"; }
}
