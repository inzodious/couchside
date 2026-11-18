package android.media;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import couchside.media.FfDecoder;
import couchside.media.VideoOutput;

/**
 * The TV's video and audio decoders, done by FFmpeg on the PC (couchside.media.FfDecoder). It behaves as Android's
 * MediaCodec does for a player such as ExoPlayer: input and output buffers by index, synchronous or with a callback,
 * flush, end of stream, and pictures released to a Surface at the time the player asks for. Decoding runs on a thread
 * of its own per codec. Encoders and encrypted (DRM) content aren't there.
 */
public final class MediaCodec {
    public static final int BUFFER_FLAG_SYNC_FRAME = 1, BUFFER_FLAG_KEY_FRAME = 1, BUFFER_FLAG_CODEC_CONFIG = 2, BUFFER_FLAG_END_OF_STREAM = 4,
        BUFFER_FLAG_PARTIAL_FRAME = 8, BUFFER_FLAG_MUXER_DATA = 16, BUFFER_FLAG_DECODE_ONLY = 32;
    public static final int CONFIGURE_FLAG_ENCODE = 1, CONFIGURE_FLAG_USE_BLOCK_MODEL = 2, CONFIGURE_FLAG_USE_CRYPTO_ASYNC = 4;
    public static final int CRYPTO_MODE_UNENCRYPTED = 0, CRYPTO_MODE_AES_CTR = 1, CRYPTO_MODE_AES_CBC = 2;
    public static final int INFO_OUTPUT_BUFFERS_CHANGED = -3, INFO_OUTPUT_FORMAT_CHANGED = -2, INFO_TRY_AGAIN_LATER = -1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1, VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    public static final String PARAMETER_KEY_VIDEO_BITRATE = "video-bitrate", PARAMETER_KEY_SUSPEND = "drop-input-frames",
        PARAMETER_KEY_REQUEST_SYNC_FRAME = "request-sync", PARAMETER_KEY_LOW_LATENCY = "low-latency", PARAMETER_KEY_TUNNEL_PEEK = "tunnel-peek",
        PARAMETER_KEY_OFFSET_TIME = "time-offset-us", PARAMETER_KEY_HDR10_PLUS_INFO = "hdr10-plus-info";

    public static final class BufferInfo {
        public int flags, offset, size;
        public long presentationTimeUs;
        public void set(int newOffset, int newSize, long newTimeUs, int newFlags) { offset = newOffset; size = newSize; presentationTimeUs = newTimeUs; flags = newFlags; }
        public BufferInfo dup() { BufferInfo b = new BufferInfo(); b.set(offset, size, presentationTimeUs, flags); return b; }
    }

    public static final class CryptoInfo {
        public int numSubSamples, mode;
        public int[] numBytesOfClearData, numBytesOfEncryptedData;
        public byte[] key, iv;
        private Pattern pattern = new Pattern(0, 0);
        public static final class Pattern {
            private int encrypt, skip;
            public Pattern(int blocksToEncrypt, int blocksToSkip) { set(blocksToEncrypt, blocksToSkip); }
            public void set(int blocksToEncrypt, int blocksToSkip) { encrypt = blocksToEncrypt; skip = blocksToSkip; }
            public int getEncryptBlocks() { return encrypt; }
            public int getSkipBlocks() { return skip; }
        }
        public void set(int newNumSubSamples, int[] newNumBytesOfClearData, int[] newNumBytesOfEncryptedData, byte[] newKey, byte[] newIV, int newMode) {
            numSubSamples = newNumSubSamples; numBytesOfClearData = newNumBytesOfClearData; numBytesOfEncryptedData = newNumBytesOfEncryptedData; key = newKey; iv = newIV; mode = newMode;
        }
        public void setPattern(Pattern newPattern) { pattern = newPattern == null ? new Pattern(0, 0) : newPattern; }
    }

    public static final class CodecException extends IllegalStateException {
        private final int code;
        public CodecException(String message, int code) { super(message); this.code = code; }
        public boolean isTransient() { return false; }
        public boolean isRecoverable() { return false; }
        public int getErrorCode() { return code; }
        public String getDiagnosticInfo() { return "android.media.MediaCodec.error_" + code; }
    }

    public static final class CryptoException extends RuntimeException {
        public static final int ERROR_NO_KEY = 1, ERROR_KEY_EXPIRED = 2, ERROR_RESOURCE_BUSY = 3, ERROR_INSUFFICIENT_OUTPUT_PROTECTION = 4,
            ERROR_SESSION_NOT_OPENED = 5, ERROR_UNSUPPORTED_OPERATION = 6, ERROR_INSUFFICIENT_SECURITY = 7, ERROR_FRAME_TOO_LARGE = 8, ERROR_LOST_STATE = 9;
        private final int code;
        public CryptoException(int errorCode, String detailMessage) { super(detailMessage); code = errorCode; }
        public int getErrorCode() { return code; }
    }

    public abstract static class Callback {
        public abstract void onInputBufferAvailable(MediaCodec codec, int index);
        public abstract void onOutputBufferAvailable(MediaCodec codec, int index, BufferInfo info);
        public abstract void onError(MediaCodec codec, CodecException e);
        public void onCryptoError(MediaCodec codec, CryptoException e) {}
        public abstract void onOutputFormatChanged(MediaCodec codec, MediaFormat format);
    }

    public interface OnFrameRenderedListener { void onFrameRendered(MediaCodec codec, long presentationTimeUs, long nanoTime); }

    // ---- Making one ----

    private final String name;
    private final String mime;

    private MediaCodec(String name, String mime) { this.name = name; this.mime = mime; }

    public static MediaCodec createByCodecName(String name) throws IOException {
        String mime = MediaCodecList.mimeOf(name);
        if (mime == null) throw new IllegalArgumentException("No codec named " + name + " on the simulated TV");
        return new MediaCodec(name, mime);
    }

    public static MediaCodec createDecoderByType(String type) throws IOException {
        String name = MediaCodecList.decoderFor(type);
        if (name == null) throw new IOException("No decoder for " + type + " on the simulated TV");
        return new MediaCodec(name, type);
    }

    public static MediaCodec createEncoderByType(String type) throws IOException { throw new IOException("The simulated TV has no encoders"); }

    public static MediaCodec createPersistentInputSurface() { throw new UnsupportedOperationException(); }

    public String getName() { return name; }
    public String getCanonicalName() { return name; }
    public MediaCodecInfo getCodecInfo() { return MediaCodecList.info(name); }

    // ---- State ----

    private static final int IN_SLOTS = 8;
    private static final Object FLUSH = new Object(), QUIT = new Object();

    private FfDecoder decoder;
    private boolean video;
    private MediaFormat inputFormat, outputFormat;
    private volatile Surface surface;
    private ByteBuffer[] inputs;
    private Callback callback;
    private Handler callbackHandler;
    private OnFrameRenderedListener rendered;
    private Handler renderedHandler;
    private Thread worker;
    private volatile boolean started, released;
    private boolean pendingFormat;

    private final Object lock = new Object();
    private final ArrayDeque<Integer> freeInputs = new ArrayDeque<>();
    private final ArrayDeque<Output> ready = new ArrayDeque<>();
    private final Map<Integer, Output> held = new HashMap<>();
    private final LinkedBlockingQueue<Object> work = new LinkedBlockingQueue<>();
    private volatile int generation;
    private int nextOutput;
    private int maxOutputs;

    private static final class Input {
        final int index, offset, size, flags, generation;
        final long ptsUs;
        Input(int index, int offset, int size, long ptsUs, int flags, int generation) { this.index = index; this.offset = offset; this.size = size; this.ptsUs = ptsUs; this.flags = flags; this.generation = generation; }
    }

    private static final class Output {
        final int index;
        final BufferInfo info = new BufferInfo();
        MediaFormat format;
        FfDecoder.Picture picture;
        ByteBuffer pcm;
        Output(int index) { this.index = index; }
    }

    public void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags) {
        if ((flags & CONFIGURE_FLAG_ENCODE) != 0) throw new IllegalArgumentException("The simulated TV has no encoders");
        inputFormat = format;
        this.surface = surface;
        video = mime.startsWith("video/");
        try {
            decoder = FfDecoder.open(mime, format);
        } catch (RuntimeException e) {
            throw new CodecException(e.getMessage(), -1);
        }
        if (decoder == null) throw new CodecException("No decoder for " + mime, -1);
        int max = format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE) ? format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE) : 0;
        int size = Math.max(max, video ? 4 << 20 : 256 << 10);
        inputs = new ByteBuffer[IN_SLOTS];
        for (int i = 0; i < IN_SLOTS; i++) inputs[i] = ByteBuffer.allocateDirect(size);
        maxOutputs = video ? 5 : 16;
        outputFormat = new MediaFormat();
        outputFormat.setString(MediaFormat.KEY_MIME, video ? "video/raw" : "audio/raw");
    }

    public void configure(MediaFormat format, Surface surface, int flags, android.media.MediaDescrambler descrambler) { configure(format, surface, null, flags); }

    public void setCallback(Callback cb, Handler handler) {
        callback = cb;
        callbackHandler = handler != null ? handler : new Handler(Looper.myLooper() != null ? Looper.myLooper() : Looper.getMainLooper());
    }

    public void setCallback(Callback cb) { setCallback(cb, null); }

    public void start() {
        if (decoder == null) throw new IllegalStateException("start() before configure()");
        if (worker == null) {
            worker = new Thread(this::run, "MediaCodec " + name);
            worker.setDaemon(true);
            worker.start();
        }
        synchronized (lock) {
            if (freeInputs.isEmpty() && !started) for (int i = 0; i < IN_SLOTS; i++) freeInputs.add(i);
            started = true;
            lock.notifyAll();
        }
        if (callback != null) {
            List<Integer> free;
            synchronized (lock) { free = new ArrayList<>(freeInputs); freeInputs.clear(); }
            for (int index : free) post(() -> callback.onInputBufferAvailable(this, index));
        }
    }

    public void flush() {
        int gen;
        synchronized (lock) {
            gen = ++generation;
            work.clear();
            for (Output o : ready) recycle(o);
            for (Output o : held.values()) recycle(o);
            ready.clear();
            held.clear();
            lock.notifyAll();
        }
        CountDownLatch done = new CountDownLatch(1);
        work.add(new Object[] {FLUSH, done});
        try { done.await(2, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        synchronized (lock) {
            freeInputs.clear();
            if (callback != null) started = false; // A codec with a callback waits for start() after a flush.
            else for (int i = 0; i < IN_SLOTS; i++) freeInputs.add(i);
            lock.notifyAll();
        }
    }

    public void stop() {
        flush();
        synchronized (lock) { started = false; freeInputs.clear(); }
    }

    public void reset() {
        stop();
        shutdown();
        decoder = null;
    }

    public void release() {
        if (released) return;
        released = true;
        synchronized (lock) {
            generation++;
            work.clear();
            for (Output o : ready) recycle(o);
            for (Output o : held.values()) recycle(o);
            ready.clear();
            held.clear();
            lock.notifyAll();
        }
        shutdown();
    }

    private void shutdown() {
        Thread t = worker;
        worker = null;
        FfDecoder d = decoder;
        if (t != null) {
            work.add(QUIT);
            try { t.join(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (d != null) d.close();
    }

    // ---- Input ----

    public int dequeueInputBuffer(long timeoutUs) {
        synchronized (lock) {
            long until = System.nanoTime() + timeoutUs * 1000L;
            while (freeInputs.isEmpty() || !started) {
                if (timeoutUs == 0 || released) return INFO_TRY_AGAIN_LATER;
                long wait = timeoutUs < 0 ? 100 : (until - System.nanoTime()) / 1_000_000L;
                if (timeoutUs > 0 && wait <= 0) return INFO_TRY_AGAIN_LATER;
                try { lock.wait(Math.max(1, wait)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return INFO_TRY_AGAIN_LATER; }
            }
            return freeInputs.poll();
        }
    }

    public ByteBuffer getInputBuffer(int index) {
        ByteBuffer b = inputs[index];
        b.clear();
        return b;
    }

    public ByteBuffer[] getInputBuffers() { return inputs; }

    public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {
        work.add(new Input(index, offset, size, presentationTimeUs, flags, generation));
    }

    public void queueSecureInputBuffer(int index, int offset, CryptoInfo info, long presentationTimeUs, int flags) {
        throw new CryptoException(CryptoException.ERROR_UNSUPPORTED_OPERATION, "The simulated TV can't play protected (DRM) video");
    }

    // ---- Output ----

    public int dequeueOutputBuffer(BufferInfo info, long timeoutUs) {
        synchronized (lock) {
            long until = System.nanoTime() + timeoutUs * 1000L;
            while (ready.isEmpty()) {
                if (timeoutUs == 0 || released) return INFO_TRY_AGAIN_LATER;
                long wait = timeoutUs < 0 ? 100 : (until - System.nanoTime()) / 1_000_000L;
                if (timeoutUs > 0 && wait <= 0) return INFO_TRY_AGAIN_LATER;
                try { lock.wait(Math.max(1, wait)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return INFO_TRY_AGAIN_LATER; }
            }
            Output o = ready.poll();
            if (o.format != null) {
                outputFormat = o.format;
                return INFO_OUTPUT_FORMAT_CHANGED;
            }
            held.put(o.index, o);
            info.set(o.info.offset, o.info.size, o.info.presentationTimeUs, o.info.flags);
            return o.index;
        }
    }

    public ByteBuffer getOutputBuffer(int index) {
        synchronized (lock) {
            Output o = held.get(index);
            return o == null ? null : o.pcm; // The same buffer each time: a player keeps its place in it.
        }
    }

    public ByteBuffer[] getOutputBuffers() { return new ByteBuffer[0]; }

    public Image getOutputImage(int index) { return null; }

    public MediaFormat getOutputFormat() { return outputFormat; }
    public MediaFormat getOutputFormat(int index) { return outputFormat; }
    public MediaFormat getInputFormat() { return inputFormat; }

    public void releaseOutputBuffer(int index, boolean render) { release(index, render, 0); }

    public void releaseOutputBuffer(int index, long renderTimestampNs) { release(index, true, renderTimestampNs); }

    private void release(int index, boolean render, long atNs) {
        Output o;
        synchronized (lock) {
            o = held.remove(index);
            lock.notifyAll();
        }
        if (o == null || o.picture == null) return;
        Surface s = surface;
        if (render && s != null && s.isValid()) {
            s.output().show(o.picture, atNs);
            OnFrameRenderedListener l = rendered;
            if (l != null) {
                long pts = o.info.presentationTimeUs;
                Runnable r = () -> l.onFrameRendered(this, pts, Math.max(atNs, System.nanoTime()));
                if (renderedHandler != null) renderedHandler.post(r); else r.run();
            }
        } else {
            recycle(o);
        }
    }

    private void recycle(Output o) {
        Surface s = surface;
        if (o.picture != null && s != null) s.output().drop(o.picture);
        o.picture = null;
    }

    // ---- Settings ----

    public void setOutputSurface(Surface surface) { this.surface = surface; }
    public void detachOutputSurface() { surface = null; }
    public void setVideoScalingMode(int mode) {}
    public void setParameters(Bundle params) {}
    public void setAudioPresentation(AudioPresentation presentation) {}
    public void setOnFrameRenderedListener(OnFrameRenderedListener listener, Handler handler) { rendered = listener; renderedHandler = handler; }
    public void setOnFirstTunnelFrameReadyListener(Handler handler, Object listener) {}
    public PersistableBundle getMetrics() { return new PersistableBundle(); }
    public List<String> getSupportedVendorParameters() { return Collections.emptyList(); }
    public void subscribeToVendorParameters(List<String> names) {}
    public void unsubscribeFromVendorParameters(List<String> names) {}
    public void signalEndOfInputStream() {}

    // ---- The decoding thread ----

    private void run() {
        try {
            for (;;) {
                Object job = work.take();
                if (job == QUIT) return;
                if (job instanceof Object[] flush) {
                    decoder.flush();
                    ((CountDownLatch) flush[1]).countDown();
                    continue;
                }
                Input in = (Input) job;
                try {
                    decode(in);
                } catch (Throwable t) {
                    android.util.Log.e("CouchsideVideo", name + " failed", t);
                    if (callback != null) post(() -> callback.onError(this, new CodecException(String.valueOf(t), -1)));
                } finally {
                    giveBack(in);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void decode(Input in) {
        boolean end = (in.flags & BUFFER_FLAG_END_OF_STREAM) != 0;
        boolean config = (in.flags & BUFFER_FLAG_CODEC_CONFIG) != 0;
        if (in.size > 0 && !(config && !video)) {
            while (!decoder.send(inputs[in.index], in.offset, in.size, in.ptsUs)) {
                if (!drain(in.generation)) return;
            }
        }
        if (!drain(in.generation)) return;
        if (end) {
            decoder.end();
            drain(in.generation);
            Output eos = new Output(nextIndex());
            eos.info.set(0, 0, in.ptsUs, BUFFER_FLAG_END_OF_STREAM);
            deliver(eos, in.generation);
            decoder.flush();
        }
    }

    /** Takes out what FFmpeg has decoded. False when a flush or release came meanwhile. */
    private boolean drain(int gen) {
        while (decoder.receive()) {
            if (!roomFor(gen)) return false;
            Output o = new Output(nextIndex());
            if (video) {
                Surface s = surface;
                VideoOutput out = s != null ? s.output() : null;
                if (out != null) { decoder.maxWidth = out.getViewWidth(); decoder.maxHeight = out.getViewHeight(); }
                o.picture = decoder.picture(out != null ? out.spare() : null);
                if (o.picture == null) continue;
                checkVideoFormat(gen);
                o.info.set(0, 1, o.picture.ptsUs, 0);
            } else {
                FfDecoder.Sound sound = decoder.sound();
                if (sound == null) continue;
                checkAudioFormat(sound.sampleRate, gen);
                o.pcm = ByteBuffer.wrap(sound.pcm, 0, sound.size).order(java.nio.ByteOrder.nativeOrder());
                o.info.set(0, sound.size, sound.ptsUs, 0);
            }
            deliver(o, gen);
        }
        return gen == generation;
    }

    private int lastWidth, lastHeight, lastRate;

    private void checkVideoFormat(int gen) {
        int w = decoder.frameWidth(), h = decoder.frameHeight();
        if (w == lastWidth && h == lastHeight) return;
        lastWidth = w;
        lastHeight = h;
        MediaFormat f = new MediaFormat();
        f.setString(MediaFormat.KEY_MIME, "video/raw");
        f.setInteger(MediaFormat.KEY_WIDTH, w);
        f.setInteger(MediaFormat.KEY_HEIGHT, h);
        f.setInteger("crop-left", 0);
        f.setInteger("crop-top", 0);
        f.setInteger("crop-right", w - 1);
        f.setInteger("crop-bottom", h - 1);
        f.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        Output o = new Output(-1);
        o.format = f;
        deliver(o, gen);
    }

    private void checkAudioFormat(int rate, int gen) {
        if (rate == lastRate) return;
        lastRate = rate;
        MediaFormat f = new MediaFormat();
        f.setString(MediaFormat.KEY_MIME, "audio/raw");
        f.setInteger(MediaFormat.KEY_SAMPLE_RATE, rate);
        f.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
        f.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT);
        Output o = new Output(-1);
        o.format = f;
        deliver(o, gen);
    }

    private int nextIndex() {
        synchronized (lock) {
            nextOutput = (nextOutput + 1) & 0x3fffffff;
            return nextOutput;
        }
    }

    /** Waits until the player has taken enough of what's out (a TV's decoder has only so many output buffers). */
    private boolean roomFor(int gen) {
        synchronized (lock) {
            while (gen == generation && !released && ready.size() + held.size() >= maxOutputs) {
                try { lock.wait(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
            }
            return gen == generation && !released;
        }
    }

    private void deliver(Output o, int gen) {
        if (callback == null) {
            synchronized (lock) {
                if (gen != generation) { recycle(o); return; }
                ready.add(o);
                lock.notifyAll();
            }
            return;
        }
        if (o.format != null) {
            MediaFormat f = o.format;
            post(() -> { if (gen == generation) { outputFormat = f; callback.onOutputFormatChanged(this, f); } });
            return;
        }
        synchronized (lock) {
            if (gen != generation) { recycle(o); return; }
            held.put(o.index, o);
        }
        BufferInfo info = o.info.dup();
        post(() -> { if (gen == generation) callback.onOutputBufferAvailable(this, o.index, info); });
    }

    private void giveBack(Input in) {
        synchronized (lock) {
            if (in.generation != generation) return;
            if (callback == null) {
                freeInputs.add(in.index);
                lock.notifyAll();
                return;
            }
        }
        int gen = in.generation;
        post(() -> { if (gen == generation && started) callback.onInputBufferAvailable(this, in.index); });
    }

    private void post(Runnable r) {
        Handler h = callbackHandler;
        if (h != null) h.post(r); else r.run();
    }
}
