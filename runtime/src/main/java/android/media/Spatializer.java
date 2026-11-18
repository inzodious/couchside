package android.media;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/** The TV has no spatial audio (Dolby Atmos virtualisation and the like): nothing is spatialized. */
public class Spatializer {
    public static final int SPATIALIZER_IMMERSIVE_LEVEL_NONE = 0, SPATIALIZER_IMMERSIVE_LEVEL_OTHER = -1, SPATIALIZER_IMMERSIVE_LEVEL_MULTICHANNEL = 1;
    static final Spatializer NONE = new Spatializer();

    public interface OnSpatializerStateChangedListener {
        void onSpatializerEnabledChanged(Spatializer spat, boolean enabled);
        void onSpatializerAvailableChanged(Spatializer spat, boolean available);
    }

    public int getImmersiveAudioLevel() { return SPATIALIZER_IMMERSIVE_LEVEL_NONE; }
    public boolean isEnabled() { return false; }
    public boolean isAvailable() { return false; }
    public boolean isHeadTrackerAvailable() { return false; }
    public boolean canBeSpatialized(AudioAttributes attributes, AudioFormat format) { return false; }
    public List<Integer> getSpatializedChannelMasks() { return Collections.emptyList(); }
    public void addOnSpatializerStateChangedListener(Executor executor, OnSpatializerStateChangedListener listener) {}
    public void removeOnSpatializerStateChangedListener(OnSpatializerStateChangedListener listener) {}
}
