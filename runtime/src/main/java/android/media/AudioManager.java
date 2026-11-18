package android.media;

import android.os.Handler;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The TV's audio settings: one music volume (0-15, as a TV's), which every AudioTrack plays at, audio focus that's
 * always granted, and stereo PCM out with no HDMI devices to pass Dolby or DTS through to.
 */
public class AudioManager {
    public static final int STREAM_VOICE_CALL = 0, STREAM_SYSTEM = 1, STREAM_RING = 2, STREAM_MUSIC = 3, STREAM_ALARM = 4, STREAM_NOTIFICATION = 5;
    public static final int ADJUST_RAISE = 1, ADJUST_LOWER = -1, ADJUST_SAME = 0, ADJUST_MUTE = -100, ADJUST_UNMUTE = 100, ADJUST_TOGGLE_MUTE = 101;
    public static final int FLAG_SHOW_UI = 1, FLAG_PLAY_SOUND = 4;
    public static final int AUDIOFOCUS_NONE = 0, AUDIOFOCUS_GAIN = 1, AUDIOFOCUS_GAIN_TRANSIENT = 2, AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK = 3,
        AUDIOFOCUS_LOSS = -1, AUDIOFOCUS_LOSS_TRANSIENT = -2, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;
    public static final int AUDIOFOCUS_REQUEST_FAILED = 0, AUDIOFOCUS_REQUEST_GRANTED = 1, AUDIOFOCUS_REQUEST_DELAYED = 2;
    public static final int GET_DEVICES_INPUTS = 1, GET_DEVICES_OUTPUTS = 2, GET_DEVICES_ALL = 3;
    public static final int MODE_NORMAL = 0, RINGER_MODE_NORMAL = 2, AUDIO_SESSION_ID_GENERATE = 0, SUCCESS = 0, ERROR = -1;
    public static final int DIRECT_PLAYBACK_NOT_SUPPORTED = 0, PLAYBACK_OFFLOAD_NOT_SUPPORTED = 0;
    public static final String PROPERTY_OUTPUT_SAMPLE_RATE = "android.media.property.OUTPUT_SAMPLE_RATE",
        PROPERTY_OUTPUT_FRAMES_PER_BUFFER = "android.media.property.OUTPUT_FRAMES_PER_BUFFER";
    public static final String ACTION_AUDIO_BECOMING_NOISY = "android.media.AUDIO_BECOMING_NOISY",
        ACTION_HDMI_AUDIO_PLUG = "android.media.action.HDMI_AUDIO_PLUG";

    private static final int MAX = 15;
    private static volatile int volume = 10;
    private static volatile boolean muted = System.getenv("COUCHSIDE_MUTE") != null; // Tests run silent.
    private static final AtomicInteger sessions = new AtomicInteger(1000);
    static final Set<AudioTrack> tracks = new CopyOnWriteArraySet<>();
    private static final AudioManager INSTANCE = new AudioManager();

    public AudioManager() {}
    public AudioManager(android.content.Context context) {}

    /** The one the simulated TV hands out as its "audio" service. */
    public static AudioManager instance() { return INSTANCE; }

    /** What the volume makes of a full-scale sample, from 0 to 1 (a TV's steps are roughly logarithmic). */
    static float level() {
        if (muted || volume == 0) return 0f;
        return (float) Math.pow(10, (volume - MAX) * 2.5 / 20);
    }

    static int nextSession() { return sessions.incrementAndGet(); }

    public int generateAudioSessionId() { return nextSession(); }

    public int getStreamVolume(int stream) { return volume; }
    public int getStreamMaxVolume(int stream) { return MAX; }
    public int getStreamMinVolume(int stream) { return 0; }
    public float getStreamVolumeDb(int stream, int index, int device) { return (index - MAX) * 2.5f; }
    public boolean isStreamMute(int stream) { return muted; }
    public boolean isVolumeFixed() { return false; }
    public void setStreamVolume(int stream, int index, int flags) { volume = Math.max(0, Math.min(MAX, index)); }
    public void setStreamMute(int stream, boolean state) { muted = state; }
    public void adjustStreamVolume(int stream, int direction, int flags) {
        switch (direction) {
            case ADJUST_RAISE: muted = false; volume = Math.min(MAX, volume + 1); break;
            case ADJUST_LOWER: volume = Math.max(0, volume - 1); break;
            case ADJUST_MUTE: muted = true; break;
            case ADJUST_UNMUTE: muted = false; break;
            case ADJUST_TOGGLE_MUTE: muted = !muted; break;
            default: break;
        }
    }
    public void adjustVolume(int direction, int flags) { adjustStreamVolume(STREAM_MUSIC, direction, flags); }
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags) { adjustStreamVolume(STREAM_MUSIC, direction, flags); }

    public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint) { return AUDIOFOCUS_REQUEST_GRANTED; }
    public int requestAudioFocus(AudioFocusRequest request) { return AUDIOFOCUS_REQUEST_GRANTED; }
    public int abandonAudioFocus(OnAudioFocusChangeListener l) { return AUDIOFOCUS_REQUEST_GRANTED; }
    public int abandonAudioFocusRequest(AudioFocusRequest request) { return AUDIOFOCUS_REQUEST_GRANTED; }

    public AudioDeviceInfo[] getDevices(int flags) { return new AudioDeviceInfo[0]; }
    public List<AudioDeviceInfo> getAudioDevicesForAttributes(AudioAttributes attributes) { return Collections.emptyList(); }
    public List<AudioProfile> getDirectProfilesForAttributes(AudioAttributes attributes) { return Collections.emptyList(); }
    public static int getDirectPlaybackSupport(AudioFormat format, AudioAttributes attributes) { return DIRECT_PLAYBACK_NOT_SUPPORTED; }
    public static int getPlaybackOffloadSupport(AudioFormat format, AudioAttributes attributes) { return PLAYBACK_OFFLOAD_NOT_SUPPORTED; }
    public static boolean isOffloadedPlaybackSupported(AudioFormat format, AudioAttributes attributes) { return false; }
    public void registerAudioDeviceCallback(AudioDeviceCallback callback, Handler handler) {}
    public void unregisterAudioDeviceCallback(AudioDeviceCallback callback) {}
    public Spatializer getSpatializer() { return Spatializer.NONE; }

    public String getProperty(String key) {
        if (PROPERTY_OUTPUT_SAMPLE_RATE.equals(key)) return "48000";
        if (PROPERTY_OUTPUT_FRAMES_PER_BUFFER.equals(key)) return "256";
        return null;
    }
    public String getParameters(String keys) { return ""; }
    public void setParameters(String keyValuePairs) {}
    public int getMode() { return MODE_NORMAL; }
    public void setMode(int mode) {}
    public int getRingerMode() { return RINGER_MODE_NORMAL; }
    public boolean isMusicActive() { for (AudioTrack t : tracks) if (t.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) return true; return false; }
    public boolean isSpeakerphoneOn() { return false; }
    public boolean isMicrophoneMute() { return true; }
    public boolean isWiredHeadsetOn() { return false; }
    public boolean isBluetoothA2dpOn() { return false; }
    public void playSoundEffect(int effectType) {}
    public void playSoundEffect(int effectType, float volume) {}
    public void loadSoundEffects() {}
    public void unloadSoundEffects() {}
    public void registerMediaButtonEventReceiver(android.content.ComponentName receiver) {}
    public void unregisterMediaButtonEventReceiver(android.content.ComponentName receiver) {}

    public interface OnAudioFocusChangeListener { void onAudioFocusChange(int focusChange); }
}
