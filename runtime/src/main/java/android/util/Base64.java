package android.util;

public class Base64 {
    public static final int DEFAULT = 0, NO_PADDING = 1, NO_WRAP = 2, CRLF = 4, URL_SAFE = 8, NO_CLOSE = 16;

    public static byte[] decode(String str, int flags) { return decode(str.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1), flags); }
    public static byte[] decode(byte[] input, int flags) {
        String s = new String(input, java.nio.charset.StandardCharsets.ISO_8859_1).replaceAll("\\s", "");
        // Android's decoder takes either alphabet's padding or none.
        while (s.endsWith("=")) s = s.substring(0, s.length() - 1);
        return ((flags & URL_SAFE) != 0 ? java.util.Base64.getUrlDecoder() : java.util.Base64.getDecoder()).decode(s);
    }
    public static byte[] decode(byte[] input, int offset, int len, int flags) { return decode(java.util.Arrays.copyOfRange(input, offset, offset + len), flags); }
    public static byte[] encode(byte[] input, int flags) {
        java.util.Base64.Encoder e = (flags & URL_SAFE) != 0 ? java.util.Base64.getUrlEncoder() : java.util.Base64.getEncoder();
        if ((flags & NO_PADDING) != 0) e = e.withoutPadding();
        byte[] out = e.encode(input);
        if ((flags & NO_WRAP) != 0) return out;
        // Android wraps at 76 characters and ends with a newline.
        String nl = (flags & CRLF) != 0 ? "\r\n" : "\n";
        StringBuilder sb = new StringBuilder();
        String s = new String(out, java.nio.charset.StandardCharsets.ISO_8859_1);
        for (int i = 0; i < s.length(); i += 76) sb.append(s, i, Math.min(s.length(), i + 76)).append(nl);
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }
    public static byte[] encode(byte[] input, int offset, int len, int flags) { return encode(java.util.Arrays.copyOfRange(input, offset, offset + len), flags); }
    public static String encodeToString(byte[] input, int flags) { return new String(encode(input, flags), java.nio.charset.StandardCharsets.ISO_8859_1); }
    public static String encodeToString(byte[] input, int offset, int len, int flags) { return encodeToString(java.util.Arrays.copyOfRange(input, offset, offset + len), flags); }
}
