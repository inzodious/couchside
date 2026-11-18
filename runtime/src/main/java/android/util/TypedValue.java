package android.util;

public class TypedValue {
    public static final int COMPLEX_UNIT_PX = 0, COMPLEX_UNIT_DIP = 1, COMPLEX_UNIT_SP = 2, COMPLEX_UNIT_PT = 3, COMPLEX_UNIT_IN = 4, COMPLEX_UNIT_MM = 5;
    public static final int TYPE_NULL = 0, TYPE_REFERENCE = 1, TYPE_STRING = 3, TYPE_FLOAT = 4, TYPE_DIMENSION = 5, TYPE_INT_DEC = 16, TYPE_INT_BOOLEAN = 18, TYPE_INT_COLOR_ARGB8 = 28;
    public int type, data, resourceId, density;
    public CharSequence string;
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_DIP: return value * metrics.density;
            case COMPLEX_UNIT_SP: return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT: return value * metrics.xdpi / 72f;
            case COMPLEX_UNIT_IN: return value * metrics.xdpi;
            case COMPLEX_UNIT_MM: return value * metrics.xdpi / 25.4f;
            default: return value;
        }
    }
    public float getFloat() { return Float.intBitsToFloat(data); }
}
