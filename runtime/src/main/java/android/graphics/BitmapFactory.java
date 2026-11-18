package android.graphics;

import java.io.InputStream;

public class BitmapFactory {
    public static class Options {
        public boolean inJustDecodeBounds, inMutable, inScaled = true, inDither, inPremultiplied = true, inPreferQualityOverSpeed;
        public int inSampleSize, inDensity, inTargetDensity, inScreenDensity, outWidth, outHeight;
        public Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888, outConfig;
        public Bitmap inBitmap;
        public String outMimeType;
        public byte[] inTempStorage;
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts) {
        if (offset != 0 || length != data.length) data = java.util.Arrays.copyOfRange(data, offset, offset + length);
        return Sk.decode(data, opts);
    }
    public static Bitmap decodeByteArray(byte[] data, int offset, int length) { return decodeByteArray(data, offset, length, null); }
    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        try { return Sk.decode(is.readAllBytes(), opts); } catch (java.io.IOException e) { return null; }
    }
    public static Bitmap decodeStream(InputStream is) { return decodeStream(is, null, null); }
    public static Bitmap decodeFile(String pathName, Options opts) {
        try { return Sk.decode(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(pathName)), opts); } catch (java.io.IOException e) { return null; }
    }
    public static Bitmap decodeFile(String pathName) { return decodeFile(pathName, null); }
    public static Bitmap decodeResource(android.content.res.Resources res, int id, Options opts) {
        try (InputStream in = res.openRawResource(id)) { return Sk.decode(in.readAllBytes(), opts); } catch (java.io.IOException e) { return null; }
    }
    public static Bitmap decodeResource(android.content.res.Resources res, int id) { return decodeResource(res, id, null); }
}
