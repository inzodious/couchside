package couchside.media;

import android.media.MediaFormat;
import java.nio.ByteBuffer;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodecContext;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avutil.AVBufferRef;
import org.bytedeco.ffmpeg.avutil.AVChannelLayout;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.ffmpeg.swresample.SwrContext;
import org.bytedeco.ffmpeg.swscale.SwsContext;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;

import static org.bytedeco.ffmpeg.global.avcodec.*;
import static org.bytedeco.ffmpeg.global.avutil.*;
import static org.bytedeco.ffmpeg.global.swresample.*;
import static org.bytedeco.ffmpeg.global.swscale.*;

/**
 * One of FFmpeg's decoders behind MediaCodec (android.media.MediaCodec): compressed samples in, as ExoPlayer hands them
 * to a TV's decoder (Annex B video, raw audio frames), and pictures or sound out. Pictures come out as BGRA, the
 * screen's own pixel layout, scaled to at most the size they're shown at; sound as 16-bit stereo.
 *
 * Video decodes on the graphics card (NVDEC, VA-API) when FFmpeg can use one, else on every CPU core.
 */
public final class FfDecoder implements AutoCloseable {
    /** A decoded picture: BGRA rows, ready for the screen. */
    public static final class Picture {
        public final int width, height;
        public final byte[] pixels;
        public long ptsUs;
        Picture(int width, int height) { this.width = width; this.height = height; this.pixels = new byte[width * height * 4]; }
    }

    /** Decoded sound: 16-bit little-endian stereo. */
    public static final class Sound {
        public final byte[] pcm;
        public final int size, sampleRate;
        public final long ptsUs;
        Sound(byte[] pcm, int size, int sampleRate, long ptsUs) { this.pcm = pcm; this.size = size; this.sampleRate = sampleRate; this.ptsUs = ptsUs; }
    }

    public final String mime;
    public final boolean video;
    private final AVCodecContext context;
    private final AVPacket packet = av_packet_alloc();
    private final AVFrame frame = av_frame_alloc();
    private final AVFrame transfer = av_frame_alloc();
    private final AVRational microseconds = av_make_q(1, 1_000_000);
    private BytePointer input = new BytePointer(av_malloc(1 << 20)).capacity(1 << 20);
    private long inputCapacity = 1 << 20;
    private SwsContext scaler;
    private int scalerFrom = -1, scalerW, scalerH, scalerOutW, scalerOutH;
    private SwrContext resampler;
    private int resamplerFormat = -1, resamplerRate, resamplerChannels;
    private BytePointer sound = new BytePointer(av_malloc(1 << 18)).capacity(1 << 18);
    private int soundCapacity = 1 << 18;
    private BytePointer picture;
    private long pictureCapacity;
    private boolean hardware;
    public volatile int maxWidth = 1920, maxHeight = 1080;

    private static final int[] HARDWARE = {AV_HWDEVICE_TYPE_CUDA, AV_HWDEVICE_TYPE_VAAPI};

    private FfDecoder(String mime, AVCodec codec, MediaFormat format, boolean video) {
        this.mime = mime;
        this.video = video;
        context = avcodec_alloc_context3(codec);
        context.pkt_timebase(microseconds);
        byte[] extra = extradata(mime, format);
        if (extra.length > 0) {
            BytePointer data = new BytePointer(av_mallocz(extra.length + AV_INPUT_BUFFER_PADDING_SIZE));
            data.put(extra);
            context.extradata(data);
            context.extradata_size(extra.length);
        }
        if (video) {
            if (format.containsKey(MediaFormat.KEY_WIDTH)) context.width(format.getInteger(MediaFormat.KEY_WIDTH));
            if (format.containsKey(MediaFormat.KEY_HEIGHT)) context.height(format.getInteger(MediaFormat.KEY_HEIGHT));
            hardware = useHardware(codec);
            if (!hardware) {
                context.thread_count(0);
                context.thread_type(3); // Frame and slice threads.
            }
        } else {
            if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) context.sample_rate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
            if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) av_channel_layout_default(context.ch_layout(), format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
            context.request_sample_fmt(AV_SAMPLE_FMT_S16);
        }
        int result = avcodec_open2(context, codec, (org.bytedeco.ffmpeg.avutil.AVDictionary) null);
        if (result < 0) throw new IllegalStateException("FFmpeg couldn't open " + codec.name().getString() + ": " + error(result));
    }

    /** FFmpeg's decoder for a MediaCodec MIME type, or null when it has none. */
    public static FfDecoder open(String mime, MediaFormat format) {
        AVCodec codec = codec(mime);
        if (codec == null) return null;
        return new FfDecoder(mime, codec, format, mime.startsWith("video/"));
    }

    public static AVCodec codec(String mime) {
        int id = codecId(mime);
        if (id == AV_CODEC_ID_AV1) {
            AVCodec dav1d = avcodec_find_decoder_by_name("libdav1d");
            if (dav1d != null) return dav1d;
        }
        return id == AV_CODEC_ID_NONE ? null : avcodec_find_decoder(id);
    }

    public static int codecId(String mime) {
        switch (mime) {
            case "video/avc": return AV_CODEC_ID_H264;
            case "video/hevc": case "video/dolby-vision": return AV_CODEC_ID_HEVC;
            case "video/x-vnd.on2.vp9": return AV_CODEC_ID_VP9;
            case "video/x-vnd.on2.vp8": return AV_CODEC_ID_VP8;
            case "video/av01": return AV_CODEC_ID_AV1;
            case "video/mpeg2": return AV_CODEC_ID_MPEG2VIDEO;
            case "video/mp4v-es": return AV_CODEC_ID_MPEG4;
            case "video/3gpp": return AV_CODEC_ID_H263;
            case "video/wvc1": case "video/x-ms-wmv": return AV_CODEC_ID_VC1;
            case "audio/mp4a-latm": return AV_CODEC_ID_AAC;
            case "audio/ac3": return AV_CODEC_ID_AC3;
            case "audio/eac3": case "audio/eac3-joc": return AV_CODEC_ID_EAC3;
            case "audio/true-hd": return AV_CODEC_ID_TRUEHD;
            case "audio/vnd.dts": case "audio/vnd.dts.hd": case "audio/vnd.dts.hd;profile=lbr": return AV_CODEC_ID_DTS;
            case "audio/opus": return AV_CODEC_ID_OPUS;
            case "audio/vorbis": return AV_CODEC_ID_VORBIS;
            case "audio/flac": return AV_CODEC_ID_FLAC;
            case "audio/mpeg": return AV_CODEC_ID_MP3;
            case "audio/mpeg-L2": return AV_CODEC_ID_MP2;
            case "audio/alac": return AV_CODEC_ID_ALAC;
            case "audio/3gpp": return AV_CODEC_ID_AMR_NB;
            case "audio/amr-wb": return AV_CODEC_ID_AMR_WB;
            case "audio/g711-alaw": return AV_CODEC_ID_PCM_ALAW;
            case "audio/g711-mlaw": return AV_CODEC_ID_PCM_MULAW;
            default: return AV_CODEC_ID_NONE;
        }
    }

    /** What FFmpeg wants up front, from MediaCodec's csd-0/1/2 buffers. */
    private static byte[] extradata(String mime, MediaFormat format) {
        byte[][] csd = new byte[3][];
        int total = 0;
        for (int i = 0; i < 3; i++) {
            ByteBuffer b = format.containsKey("csd-" + i) ? format.getByteBuffer("csd-" + i) : null;
            if (b == null) continue;
            b = b.duplicate();
            b.rewind();
            csd[i] = new byte[b.remaining()];
            b.get(csd[i]);
            total += csd[i].length;
        }
        if (total == 0) return new byte[0];
        switch (mime) {
            case "audio/vorbis": {
                // Xiph lacing: 2, the two header lengths, then the identification, comment-less and setup headers.
                byte[] id = csd[0], setup = csd[1] != null ? csd[1] : new byte[0];
                byte[] comment = {3, 'v', 'o', 'r', 'b', 'i', 's', 0, 0, 0, 0, 0, 0, 0, 0, 1};
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                out.write(2);
                lace(out, id.length);
                lace(out, comment.length);
                out.writeBytes(id);
                out.writeBytes(comment);
                out.writeBytes(setup);
                return out.toByteArray();
            }
            case "audio/opus":
                return csd[0];
            default: {
                byte[] all = new byte[total];
                int at = 0;
                for (byte[] c : csd) if (c != null) { System.arraycopy(c, 0, all, at, c.length); at += c.length; }
                return all;
            }
        }
    }

    private static void lace(java.io.ByteArrayOutputStream out, int n) {
        while (n >= 255) { out.write(255); n -= 255; }
        out.write(n);
    }

    private boolean useHardware(AVCodec codec) {
        if (System.getenv("COUCHSIDE_SOFTWARE_VIDEO") != null) return false;
        for (int type : HARDWARE) {
            for (int i = 0; ; i++) {
                org.bytedeco.ffmpeg.avcodec.AVCodecHWConfig config = avcodec_get_hw_config(codec, i);
                if (config == null) break;
                if (config.device_type() != type || (config.methods() & AV_CODEC_HW_CONFIG_METHOD_HW_DEVICE_CTX) == 0) continue;
                AVBufferRef device = new AVBufferRef((Pointer) null);
                if (av_hwdevice_ctx_create(device, type, (String) null, null, 0) < 0) break;
                context.hw_device_ctx(av_buffer_ref(device));
                av_buffer_unref(device);
                android.util.Log.i("CouchsideVideo", codec.name().getString() + " decodes on " + av_hwdevice_get_type_name(type).getString());
                return true;
            }
        }
        return false;
    }

    /** Hands FFmpeg one compressed sample. False when it's full: take its output first, then send again. */
    public boolean send(ByteBuffer data, int offset, int size, long ptsUs) {
        if (size > inputCapacity) {
            av_free(input);
            inputCapacity = size + AV_INPUT_BUFFER_PADDING_SIZE;
            input = new BytePointer(av_malloc(inputCapacity)).capacity(inputCapacity);
        }
        ByteBuffer source = data.duplicate();
        source.limit(offset + size).position(offset);
        input.position(0).limit(size);
        input.asBuffer().put(source);
        BytePointer.memset(input.position(size), 0, AV_INPUT_BUFFER_PADDING_SIZE);
        input.position(0);
        packet.data(input);
        packet.size(size);
        packet.pts(ptsUs);
        packet.dts(AV_NOPTS_VALUE);
        int result = avcodec_send_packet(context, packet);
        if (result == AVERROR_EAGAIN()) return false;
        if (result < 0 && result != AVERROR_INVALIDDATA) android.util.Log.w("CouchsideVideo", mime + ": " + error(result));
        return true;
    }

    /** No more input: what's left inside comes out. */
    public void end() { avcodec_send_packet(context, null); }

    /** True when a decoded frame is ready to take with [picture] or [sound]; false when FFmpeg needs more input or has ended. */
    public boolean receive() {
        last = avcodec_receive_frame(context, frame);
        return last >= 0;
    }

    /** Whether the last [receive] found the end of the stream (after [end]). */
    public boolean ended() { return last == AVERROR_EOF; }

    private int last;

    public long framePtsUs() {
        long pts = frame.best_effort_timestamp();
        return pts == AV_NOPTS_VALUE ? frame.pts() : pts;
    }

    public int frameWidth() { return frame.width(); }
    public int frameHeight() { return frame.height(); }

    /** The frame just received, as BGRA scaled to fit maxWidth x maxHeight, into [into] when it's the right size. */
    public Picture picture(Picture into) {
        AVFrame source = frame;
        if (frame.hw_frames_ctx() != null) {
            av_frame_unref(transfer);
            if (av_hwframe_transfer_data(transfer, frame, 0) < 0) return null;
            source = transfer;
        }
        int w = source.width(), h = source.height();
        double scale = Math.min(1.0, Math.min(maxWidth / (double) w, maxHeight / (double) h));
        int outW = Math.max(2, (int) Math.round(w * scale) & ~1), outH = Math.max(2, (int) Math.round(h * scale) & ~1);
        if (scaler == null || scalerFrom != source.format() || scalerW != w || scalerH != h || scalerOutW != outW || scalerOutH != outH) {
            if (scaler != null) sws_freeContext(scaler);
            scaler = sws_getContext(w, h, source.format(), outW, outH, AV_PIX_FMT_BGRA, SWS_BILINEAR, null, null, (DoublePointer) null);
            scalerFrom = source.format(); scalerW = w; scalerH = h; scalerOutW = outW; scalerOutH = outH;
        }
        long bytes = (long) outW * outH * 4;
        if (picture == null || pictureCapacity < bytes) {
            if (picture != null) av_free(picture);
            picture = new BytePointer(av_malloc(bytes)).capacity(bytes);
            pictureCapacity = bytes;
        }
        PointerPointer<BytePointer> out = new PointerPointer<BytePointer>(1);
        out.put(0, picture);
        org.bytedeco.javacpp.IntPointer stride = new org.bytedeco.javacpp.IntPointer(1).put(0, outW * 4);
        sws_scale(scaler, source.data(), source.linesize(), 0, h, out, stride);
        out.close();
        stride.close();
        Picture result = into != null && into.width == outW && into.height == outH ? into : new Picture(outW, outH);
        picture.position(0).get(result.pixels, 0, (int) bytes);
        result.ptsUs = framePtsUs();
        return result;
    }

    /** The frame just received, as 16-bit stereo at its own sample rate. */
    public Sound sound() {
        int channels = frame.ch_layout().nb_channels(), rate = frame.sample_rate(), format = frame.format();
        if (resampler == null || resamplerFormat != format || resamplerRate != rate || resamplerChannels != channels) {
            if (resampler != null) swr_free(resampler);
            AVChannelLayout stereo = new AVChannelLayout();
            av_channel_layout_default(stereo, 2);
            PointerPointer<SwrContext> holder = new PointerPointer<SwrContext>(1);
            holder.put(0, (Pointer) null);
            swr_alloc_set_opts2(holder, stereo, AV_SAMPLE_FMT_S16, rate, frame.ch_layout(), format, rate, 0, null);
            resampler = holder.get(SwrContext.class, 0);
            swr_init(resampler);
            resamplerFormat = format; resamplerRate = rate; resamplerChannels = channels;
        }
        int samples = frame.nb_samples();
        int bytes = samples * 2 * 2 + 4096;
        if (soundCapacity < bytes) {
            av_free(sound);
            soundCapacity = bytes;
            sound = new BytePointer(av_malloc(bytes)).capacity(bytes);
        }
        PointerPointer<BytePointer> out = new PointerPointer<BytePointer>(1);
        out.put(0, sound);
        int made = swr_convert(resampler, out, samples + 256, frame.extended_data(), samples);
        out.close();
        if (made <= 0) return null;
        byte[] pcm = new byte[made * 4];
        sound.position(0).get(pcm);
        return new Sound(pcm, pcm.length, rate, framePtsUs());
    }

    public int sampleRate() { return context.sample_rate(); }

    public void flush() { avcodec_flush_buffers(context); }

    @Override public void close() {
        avcodec_free_context(context);
        av_packet_free(packet);
        av_frame_free(frame);
        av_frame_free(transfer);
        if (scaler != null) sws_freeContext(scaler);
        if (resampler != null) swr_free(resampler);
        av_free(input);
        av_free(sound);
        if (picture != null) av_free(picture);
    }

    static String error(int code) {
        byte[] buffer = new byte[128];
        av_strerror(code, buffer, buffer.length);
        return new String(buffer).trim().replace("\0", "");
    }
}
