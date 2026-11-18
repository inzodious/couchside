package android.media;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import couchside.media.FfDecoder;

/**
 * The simulated TV's decoders: one for each format FFmpeg on the PC can decode, described the way a TV's own decoders
 * describe themselves (profiles, levels, sizes up to 4K at 60, up to 8 audio channels), so a player picks them as it
 * would on a TV. The MediaCodecInfo objects are Android's own classes, built through their hidden constructors.
 */
public final class MediaCodecList {
    public static final int REGULAR_CODECS = 0, ALL_CODECS = 1;

    private static final class Spec {
        final String mime, shortName;
        final int[][] profileLevels;
        Spec(String mime, String shortName, int[]... profileLevels) { this.mime = mime; this.shortName = shortName; this.profileLevels = profileLevels; }
    }

    private static final Spec[] SPECS = {
        new Spec("video/avc", "avc", new int[] {1, 65536}, new int[] {2, 65536}, new int[] {8, 65536}, new int[] {16, 65536}, new int[] {65536, 65536}, new int[] {524288, 65536}),
        new Spec("video/hevc", "hevc", new int[] {1, 33554432}, new int[] {2, 33554432}, new int[] {4096, 33554432}, new int[] {8192, 33554432}),
        new Spec("video/x-vnd.on2.vp9", "vp9", new int[] {1, 8192}, new int[] {2, 8192}, new int[] {4, 8192}, new int[] {4096, 8192}, new int[] {8192, 8192}, new int[] {16384, 8192}),
        new Spec("video/av01", "av1", new int[] {1, 8388608}, new int[] {2, 8388608}, new int[] {4096, 8388608}, new int[] {8192, 8388608}),
        new Spec("video/x-vnd.on2.vp8", "vp8", new int[] {1, 8}),
        new Spec("video/mpeg2", "mpeg2", new int[] {4, 3}, new int[] {1, 3}),
        new Spec("video/mp4v-es", "mpeg4", new int[] {1, 128}, new int[] {32768, 128}),
        new Spec("video/3gpp", "h263", new int[] {1, 128}),
        new Spec("audio/mp4a-latm", "aac", new int[] {2, 0}, new int[] {5, 0}, new int[] {29, 0}, new int[] {23, 0}, new int[] {39, 0}, new int[] {42, 0}),
        new Spec("audio/ac3", "ac3"),
        new Spec("audio/eac3", "eac3"),
        new Spec("audio/eac3-joc", "eac3joc"),
        new Spec("audio/true-hd", "truehd"),
        new Spec("audio/vnd.dts", "dts"),
        new Spec("audio/vnd.dts.hd", "dtshd"),
        new Spec("audio/opus", "opus"),
        new Spec("audio/vorbis", "vorbis"),
        new Spec("audio/flac", "flac"),
        new Spec("audio/mpeg", "mp3"),
        new Spec("audio/mpeg-L2", "mp2"),
        new Spec("audio/alac", "alac"),
        new Spec("audio/3gpp", "amrnb"),
        new Spec("audio/amr-wb", "amrwb"),
        new Spec("audio/g711-alaw", "g711.alaw"),
        new Spec("audio/g711-mlaw", "g711.mlaw"),
    };

    private static Map<String, MediaCodecInfo> infos;
    private static final Map<String, String> mimes = new HashMap<>();

    private static synchronized Map<String, MediaCodecInfo> infos() {
        if (infos != null) return infos;
        Map<String, MediaCodecInfo> made = new LinkedHashMap<>();
        for (Spec spec : SPECS) {
            try {
                if (FfDecoder.codec(spec.mime) == null) continue;
            } catch (Throwable t) {
                android.util.Log.e("CouchsideVideo", "FFmpeg didn't load, so the simulated TV has no decoders", t);
                break;
            }
            String name = "c2.couchside." + spec.shortName + ".decoder";
            try {
                made.put(name, build(name, spec));
                mimes.put(name, spec.mime);
            } catch (ReflectiveOperationException e) {
                android.util.Log.e("CouchsideVideo", "Couldn't describe " + name, e);
            }
        }
        infos = made;
        return infos;
    }

    private static MediaCodecInfo build(String name, Spec spec) throws ReflectiveOperationException {
        boolean video = spec.mime.startsWith("video/");
        MediaCodecInfo.CodecProfileLevel[] levels = new MediaCodecInfo.CodecProfileLevel[spec.profileLevels.length];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = new MediaCodecInfo.CodecProfileLevel();
            levels[i].profile = spec.profileLevels[i][0];
            levels[i].level = spec.profileLevels[i][1];
        }
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(MediaFormat.KEY_MIME, spec.mime);
        Map<String, Object> caps = new HashMap<>();
        caps.put("max-concurrent-instances", "16");
        if (video) {
            caps.put("alignment", "2x2");
            caps.put("block-size", "16x16");
            caps.put("block-count-range", "1-32640");
            caps.put("blocks-per-second-range", "1-1958400");
            caps.put("size-range", "2x2-3840x2176");
            caps.put("frame-rate-range", "1-120");
            caps.put("bitrate-range", "1-240000000");
            caps.put("feature-adaptive-playback", 1);
        } else {
            caps.put("bitrate-range", "1-24500000");
            caps.put("max-channel-count", "8");
            caps.put("sample-rate-ranges", "8000-192000");
        }
        int[] colors = video ? new int[] {MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible} : new int[0];
        Constructor<MediaCodecInfo.CodecCapabilities> capabilities = MediaCodecInfo.CodecCapabilities.class.getDeclaredConstructor(
            MediaCodecInfo.CodecProfileLevel[].class, int[].class, boolean.class, Map.class, Map.class);
        capabilities.setAccessible(true);
        MediaCodecInfo.CodecCapabilities made = capabilities.newInstance(levels, colors, false, defaults, caps);
        Constructor<MediaCodecInfo> info = MediaCodecInfo.class.getDeclaredConstructor(String.class, String.class, int.class, MediaCodecInfo.CodecCapabilities[].class);
        info.setAccessible(true);
        // Vendor and hardware-accelerated, as a TV's own decoders are.
        return info.newInstance(name, name, 2 | 8, new MediaCodecInfo.CodecCapabilities[] {made});
    }

    static String mimeOf(String name) { infos(); return mimes.get(name); }

    static MediaCodecInfo info(String name) { return infos().get(name); }

    static String decoderFor(String mime) {
        for (Map.Entry<String, MediaCodecInfo> e : infos().entrySet()) if (mime.equalsIgnoreCase(mimes.get(e.getKey()))) return e.getKey();
        return null;
    }

    static final Map<String, Object> getGlobalSettings() { return Collections.emptyMap(); }

    public MediaCodecList(int kind) {}

    public final MediaCodecInfo[] getCodecInfos() { return infos().values().toArray(new MediaCodecInfo[0]); }

    public static final int getCodecCount() { return infos().size(); }

    public static final MediaCodecInfo getCodecInfoAt(int index) { return new ArrayList<>(infos().values()).get(index); }

    public static MediaCodecInfo getInfoFor(String codec) { return info(codec); }

    public final String findDecoderForFormat(MediaFormat format) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        return mime == null ? null : decoderFor(mime);
    }

    public final String findEncoderForFormat(MediaFormat format) { return null; }

    static List<String> names() { return new ArrayList<>(infos().keySet()); }
}
