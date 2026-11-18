package android.graphics;

public class Color {
    public static final int BLACK = 0xFF000000, DKGRAY = 0xFF444444, GRAY = 0xFF888888, LTGRAY = 0xFFCCCCCC, WHITE = 0xFFFFFFFF;
    public static final int RED = 0xFFFF0000, GREEN = 0xFF00FF00, BLUE = 0xFF0000FF, YELLOW = 0xFFFFFF00, CYAN = 0xFF00FFFF, MAGENTA = 0xFFFF00FF;
    public static final int TRANSPARENT = 0;

    public static int alpha(int color) { return color >>> 24; }
    public static int red(int color) { return (color >> 16) & 0xFF; }
    public static int green(int color) { return (color >> 8) & 0xFF; }
    public static int blue(int color) { return color & 0xFF; }
    public static int rgb(int red, int green, int blue) { return 0xFF000000 | (red << 16) | (green << 8) | blue; }
    public static int argb(int alpha, int red, int green, int blue) { return (alpha << 24) | (red << 16) | (green << 8) | blue; }
    public static int rgb(float red, float green, float blue) { return argb(1f, red, green, blue); }
    public static int argb(float alpha, float red, float green, float blue) {
        return ((int) (alpha * 255f + 0.5f) << 24) | ((int) (red * 255f + 0.5f) << 16) | ((int) (green * 255f + 0.5f) << 8) | (int) (blue * 255f + 0.5f);
    }
    public static float luminance(int color) {
        double r = lin(red(color) / 255.0), g = lin(green(color) / 255.0), b = lin(blue(color) / 255.0);
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }
    private static double lin(double c) { return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4); }

    public static int parseColor(String s) {
        if (s.charAt(0) == '#') {
            long color = Long.parseLong(s.substring(1), 16);
            if (s.length() == 7) color |= 0xFF000000L;
            else if (s.length() != 9) throw new IllegalArgumentException("Unknown color");
            return (int) color;
        }
        switch (s.toLowerCase(java.util.Locale.ROOT)) {
            case "black": return BLACK; case "white": return WHITE; case "red": return RED; case "green": return GREEN;
            case "blue": return BLUE; case "yellow": return YELLOW; case "cyan": case "aqua": return CYAN;
            case "magenta": case "fuchsia": return MAGENTA; case "gray": case "grey": return GRAY;
            case "darkgray": case "darkgrey": return DKGRAY; case "lightgray": case "lightgrey": return LTGRAY;
            default: throw new IllegalArgumentException("Unknown color");
        }
    }

    public static void RGBToHSV(int red, int green, int blue, float[] hsv) {
        float r = red / 255f, g = green / 255f, b = blue / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b)), delta = max - min;
        float h;
        if (delta == 0f) h = 0f;
        else if (max == r) h = 60f * (((g - b) / delta) % 6f);
        else if (max == g) h = 60f * (((b - r) / delta) + 2f);
        else h = 60f * (((r - g) / delta) + 4f);
        if (h < 0f) h += 360f;
        hsv[0] = h; hsv[1] = max == 0f ? 0f : delta / max; hsv[2] = max;
    }
    public static void colorToHSV(int color, float[] hsv) { RGBToHSV(red(color), green(color), blue(color), hsv); }
    public static int HSVToColor(float[] hsv) { return HSVToColor(0xFF, hsv); }
    public static int HSVToColor(int alpha, float[] hsv) {
        float h = ((hsv[0] % 360f) + 360f) % 360f, s = Math.max(0f, Math.min(1f, hsv[1])), v = Math.max(0f, Math.min(1f, hsv[2]));
        float c = v * s, x = c * (1f - Math.abs((h / 60f) % 2f - 1f)), m = v - c;
        float r, g, b;
        if (h < 60f) { r = c; g = x; b = 0; } else if (h < 120f) { r = x; g = c; b = 0; } else if (h < 180f) { r = 0; g = c; b = x; }
        else if (h < 240f) { r = 0; g = x; b = c; } else if (h < 300f) { r = x; g = 0; b = c; } else { r = c; g = 0; b = x; }
        return argb(alpha, Math.round((r + m) * 255f), Math.round((g + m) * 255f), Math.round((b + m) * 255f));
    }
}
