package android.media;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * The TV's sound output, played through the PC's (Java Sound, so PipeWire or PulseAudio on Linux). PCM in any of
 * Android's encodings comes in; what goes out is 16-bit, mixed down to stereo, at the track's own sample rate, scaled
 * by the TV's volume (AudioManager). The playback position is what the PC's audio has actually played, which is what
 * a player such as ExoPlayer keeps video in step with. Passthrough (sending Dolby to a receiver) isn't there.
 */
public class AudioTrack {
    public static final int PLAYSTATE_STOPPED = 1, PLAYSTATE_PAUSED = 2, PLAYSTATE_PLAYING = 3;
    public static final int STATE_UNINITIALIZED = 0, STATE_INITIALIZED = 1, STATE_NO_STATIC_DATA = 2;
    public static final int MODE_STATIC = 0, MODE_STREAM = 1;
    public static final int WRITE_BLOCKING = 0, WRITE_NON_BLOCKING = 1;
    public static final int SUCCESS = 0, ERROR = -1, ERROR_BAD_VALUE = -2, ERROR_INVALID_OPERATION = -3, ERROR_DEAD_OBJECT = -6;
    public static final int PERFORMANCE_MODE_NONE = 0, PERFORMANCE_MODE_LOW_LATENCY = 1, PERFORMANCE_MODE_POWER_SAVING = 2;
    public static final int ENCAPSULATION_MODE_NONE = 0;

    public abstract static class StreamEventCallback {
        public StreamEventCallback() {}
        public void onTearDown(AudioTrack track) {}
        public void onPresentationEnded(AudioTrack track) {}
        public void onDataRequest(AudioTrack track, int sizeInFrames) {}
    }

    public static class Builder {
        private AudioAttributes attributes;
        private AudioFormat format;
        private int bufferSize, mode = MODE_STREAM, session;
        public Builder() {}
        public Builder setAudioAttributes(AudioAttributes a) { attributes = a; return this; }
        public Builder setAudioFormat(AudioFormat f) { format = f; return this; }
        public Builder setBufferSizeInBytes(int size) { bufferSize = size; return this; }
        public Builder setTransferMode(int m) { mode = m; return this; }
        public Builder setSessionId(int id) { session = id; return this; }
        public Builder setOffloadedPlayback(boolean offload) { return this; }
        public Builder setPerformanceMode(int m) { return this; }
        public Builder setContext(android.content.Context c) { return this; }
        public Builder setEncapsulationMode(int m) { return this; }
        public Builder setTunerConfiguration(Object c) { return this; }
        public AudioTrack build() {
            AudioFormat f = format != null ? format : new AudioFormat.Builder().setSampleRate(48000).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build();
            return new AudioTrack(attributes, f, bufferSize, mode, session);
        }
    }

    private final int sampleRate, channels, encoding, inFrame, bufferBytes, session;
    private final AudioFormat format;
    private SourceDataLine line;
    private int state = STATE_UNINITIALIZED, playState = PLAYSTATE_STOPPED;
    private long base;
    private float volume = 1f;
    private byte[] out = new byte[16384];
    private PlaybackParams params = new PlaybackParams();

    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode) {
        this(null, new AudioFormat.Builder().setSampleRate(sampleRateInHz).setEncoding(audioFormat).setChannelMask(channelConfig).build(), bufferSizeInBytes, mode, 0);
    }

    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int sessionId) {
        this(null, new AudioFormat.Builder().setSampleRate(sampleRateInHz).setEncoding(audioFormat).setChannelMask(channelConfig).build(), bufferSizeInBytes, mode, sessionId);
    }

    public AudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) {
        this.format = format;
        sampleRate = format.getSampleRate() > 0 ? format.getSampleRate() : 48000;
        channels = Math.max(1, format.getChannelCount());
        encoding = format.getEncoding() == AudioFormat.ENCODING_DEFAULT ? AudioFormat.ENCODING_PCM_16BIT : format.getEncoding();
        inFrame = channels * bytesPerSample(encoding);
        session = sessionId != 0 ? sessionId : AudioManager.nextSession();
        int frames = inFrame > 0 ? Math.max(bufferSizeInBytes / inFrame, sampleRate / 10) : sampleRate / 4;
        bufferBytes = frames * 4;
        if (inFrame <= 0) return; // Compressed (passthrough) encodings: not on a PC.
        try {
            javax.sound.sampled.AudioFormat pcm = new javax.sound.sampled.AudioFormat(sampleRate, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(pcm);
            line.open(pcm, bufferBytes);
            state = STATE_INITIALIZED;
            AudioManager.tracks.add(this);
        } catch (Exception | LinkageError e) {
            android.util.Log.e("CouchsideAudio", "The PC's sound output wouldn't open", e);
        }
    }

    private static int bytesPerSample(int encoding) {
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_8BIT: return 1;
            case AudioFormat.ENCODING_PCM_16BIT: return 2;
            case AudioFormat.ENCODING_PCM_24BIT_PACKED: return 3;
            case AudioFormat.ENCODING_PCM_32BIT: case AudioFormat.ENCODING_PCM_FLOAT: return 4;
            default: return 0;
        }
    }

    public static int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int bytes = bytesPerSample(audioFormat);
        if (bytes == 0) return ERROR_BAD_VALUE;
        return sampleRateInHz / 20 * Integer.bitCount(channelConfig) * bytes;
    }

    public static boolean isDirectPlaybackSupported(AudioFormat format, AudioAttributes attributes) { return false; }

    public static float getMinVolume() { return 0f; }
    public static float getMaxVolume() { return 1f; }

    public int getState() { return state; }
    public int getPlayState() { return playState; }
    public int getSampleRate() { return sampleRate; }
    public int getPlaybackRate() { return sampleRate; }
    public int getChannelCount() { return channels; }
    public int getAudioFormat() { return encoding; }
    public AudioFormat getFormat() { return format; }
    public int getChannelConfiguration() { return format.getChannelMask(); }
    public int getAudioSessionId() { return session; }
    public int getBufferSizeInFrames() { return bufferBytes / 4; }
    public int getBufferCapacityInFrames() { return bufferBytes / 4; }
    public int setBufferSizeInFrames(int frames) { return bufferBytes / 4; }
    public int getUnderrunCount() { return 0; }
    public boolean isOffloadedPlayback() { return false; }
    public PlaybackParams getPlaybackParams() { return params; }
    public void setPlaybackParams(PlaybackParams p) { params = p; }

    public void play() {
        if (line == null) return;
        line.start();
        playState = PLAYSTATE_PLAYING;
    }

    public void pause() {
        if (line == null) return;
        line.stop();
        playState = PLAYSTATE_PAUSED;
    }

    /** Stopping a stream lets what's written play out, as Android does. */
    public void stop() { playState = PLAYSTATE_STOPPED; }

    public void flush() {
        if (line == null || playState == PLAYSTATE_PLAYING) return;
        line.flush();
        base = line.getLongFramePosition();
    }

    public void release() {
        if (line != null) {
            line.stop();
            line.flush();
            line.close();
        }
        line = null;
        state = STATE_UNINITIALIZED;
        AudioManager.tracks.remove(this);
    }

    public int getPlaybackHeadPosition() {
        if (line == null) return 0;
        return (int) (line.getLongFramePosition() - base);
    }

    public boolean getTimestamp(AudioTimestamp timestamp) {
        if (line == null || playState != PLAYSTATE_PLAYING) return false;
        timestamp.framePosition = line.getLongFramePosition() - base;
        timestamp.nanoTime = System.nanoTime();
        return true;
    }

    public int setVolume(float gain) { volume = Math.max(0f, Math.min(1f, gain)); return SUCCESS; }
    public int setStereoVolume(float left, float right) { return setVolume(Math.max(left, right)); }
    public int attachAuxEffect(int effectId) { return SUCCESS; }
    public int setAuxEffectSendLevel(float level) { return SUCCESS; }
    public boolean setPreferredDevice(AudioDeviceInfo device) { return true; }
    public AudioDeviceInfo getRoutedDevice() { return null; }
    public void addOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener, android.os.Handler handler) {}
    public void removeOnRoutingChangedListener(AudioRouting.OnRoutingChangedListener listener) {}
    public void registerStreamEventCallback(Executor executor, StreamEventCallback callback) {}
    public void unregisterStreamEventCallback(StreamEventCallback callback) {}
    public void setOffloadDelayPadding(int delayInFrames, int paddingInFrames) {}
    public void setOffloadEndOfStream() {}
    public void setLogSessionId(android.media.metrics.LogSessionId id) {}

    public int write(byte[] data, int offset, int size) { return write(ByteBuffer.wrap(data, offset, size), size, WRITE_BLOCKING); }
    public int write(byte[] data, int offset, int size, int mode) { return write(ByteBuffer.wrap(data, offset, size), size, mode); }

    public int write(short[] data, int offset, int size) { return write(data, offset, size, WRITE_BLOCKING); }
    public int write(short[] data, int offset, int size, int mode) {
        ByteBuffer b = ByteBuffer.allocate(size * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.asShortBuffer().put(data, offset, size);
        return write(b, size * 2, mode) / 2;
    }

    public int write(float[] data, int offset, int size, int mode) {
        ByteBuffer b = ByteBuffer.allocate(size * 4).order(ByteOrder.LITTLE_ENDIAN);
        b.asFloatBuffer().put(data, offset, size);
        return write(b, size * 4, mode) / 4;
    }

    public int write(ByteBuffer data, int size, int mode, long timestamp) { return write(data, size, mode); }

    /** Takes as much as the PC's buffer has room for (all of it when blocking), and says how many bytes it took. */
    public int write(ByteBuffer data, int size, int mode) {
        if (line == null) return ERROR_INVALID_OPERATION;
        ByteBuffer in = data.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int frames = Math.min(size, in.remaining()) / inFrame;
        if (mode == WRITE_NON_BLOCKING) frames = Math.min(frames, line.available() / 4);
        if (frames <= 0) return 0;
        if (out.length < frames * 4) out = new byte[frames * 4];
        float gain = volume * AudioManager.level();
        int at = in.position();
        for (int f = 0; f < frames; f++) {
            float l, r;
            if (channels == 1) {
                l = r = sample(in, at);
            } else {
                l = sample(in, at);
                r = sample(in, at + bytesPerSample(encoding));
                if (channels > 2) { // Down to stereo: the centre into both sides, the surrounds onto theirs, no LFE.
                    float c = sample(in, at + 2 * bytesPerSample(encoding));
                    float left = 0f, right = 0f;
                    for (int ch = 4; ch < channels; ch++) {
                        float v = sample(in, at + ch * bytesPerSample(encoding));
                        if (ch % 2 == 0) left += v; else right += v;
                    }
                    l = (l + 0.707f * c + 0.5f * left) * 0.6f;
                    r = (r + 0.707f * c + 0.5f * right) * 0.6f;
                }
            }
            short sl = clip(l * gain), sr = clip(r * gain);
            int o = f * 4;
            out[o] = (byte) sl; out[o + 1] = (byte) (sl >> 8); out[o + 2] = (byte) sr; out[o + 3] = (byte) (sr >> 8);
            at += inFrame;
        }
        line.write(out, 0, frames * 4);
        int taken = frames * inFrame;
        data.position(data.position() + taken);
        return taken;
    }

    private float sample(ByteBuffer b, int at) {
        switch (encoding) {
            case AudioFormat.ENCODING_PCM_16BIT: return b.getShort(at) / 32768f;
            case AudioFormat.ENCODING_PCM_FLOAT: return b.getFloat(at);
            case AudioFormat.ENCODING_PCM_8BIT: return ((b.get(at) & 0xff) - 128) / 128f;
            case AudioFormat.ENCODING_PCM_24BIT_PACKED: return ((b.get(at) & 0xff) | ((b.get(at + 1) & 0xff) << 8) | (b.get(at + 2) << 16)) / 8388608f;
            case AudioFormat.ENCODING_PCM_32BIT: return b.getInt(at) / 2147483648f;
            default: return 0f;
        }
    }

    private static short clip(float v) { return (short) Math.max(-32768, Math.min(32767, Math.round(v * 32767f))); }
}
