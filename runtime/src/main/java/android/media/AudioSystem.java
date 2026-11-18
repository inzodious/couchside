package android.media;

/** Android's audio system, as far as its framework classes ask it (AudioFormat's limits): the PC's audio has no natives here. */
public class AudioSystem {
    public static final int SAMPLE_RATE_HZ_MIN = 4000, SAMPLE_RATE_HZ_MAX = 192000, OUT_CHANNEL_COUNT_MAX = 8, FCC_24 = 24, FCC_LIMIT = 8;
    public static final int SUCCESS = 0, ERROR = -1, BAD_VALUE = -2, INVALID_OPERATION = -3, DEAD_OBJECT = -6;
    public static final int STREAM_DEFAULT = -1, STREAM_MUSIC = 3, NUM_STREAM_TYPES = 12;
    public static final int AUDIO_SESSION_ALLOCATE = 0;
    public static final int DEVICE_OUT_SPEAKER = 0x2, DEVICE_OUT_HDMI = 0x400, DEVICE_NONE = 0;

    private AudioSystem() {}

    public static int getMaxChannelCount() { return OUT_CHANNEL_COUNT_MAX; }
    public static int getMaxSampleRate() { return SAMPLE_RATE_HZ_MAX; }
    public static int getMinSampleRate() { return SAMPLE_RATE_HZ_MIN; }
    public static int getPrimaryOutputSamplingRate() { return 48000; }
    public static int getPrimaryOutputFrameCount() { return 256; }
    public static int getOutputLatency(int stream) { return 40; }
    public static boolean isStreamActive(int stream, int inPastMs) { return false; }
    public static boolean isOffloadSupported(AudioFormat format, AudioAttributes attributes) { return false; }
    public static int getOffloadSupport(AudioFormat format, AudioAttributes attributes) { return 0; }
    public static int getDirectPlaybackSupport(AudioFormat format, AudioAttributes attributes) { return 0; }
    public static int newAudioSessionId() { return AudioManager.nextSession(); }
    public static String streamToString(int stream) { return "STREAM_" + stream; }
}
