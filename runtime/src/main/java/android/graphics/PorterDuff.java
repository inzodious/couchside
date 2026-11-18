package android.graphics;

public class PorterDuff {
    public enum Mode {
        CLEAR, SRC, DST, SRC_OVER, DST_OVER, SRC_IN, DST_IN, SRC_OUT, DST_OUT, SRC_ATOP, DST_ATOP, XOR, DARKEN, LIGHTEN, MULTIPLY, SCREEN, ADD, OVERLAY;

        org.jetbrains.skia.BlendMode skia() {
            switch (this) {
                case CLEAR: return org.jetbrains.skia.BlendMode.CLEAR;
                case SRC: return org.jetbrains.skia.BlendMode.SRC;
                case DST: return org.jetbrains.skia.BlendMode.DST;
                case DST_OVER: return org.jetbrains.skia.BlendMode.DST_OVER;
                case SRC_IN: return org.jetbrains.skia.BlendMode.SRC_IN;
                case DST_IN: return org.jetbrains.skia.BlendMode.DST_IN;
                case SRC_OUT: return org.jetbrains.skia.BlendMode.SRC_OUT;
                case DST_OUT: return org.jetbrains.skia.BlendMode.DST_OUT;
                case SRC_ATOP: return org.jetbrains.skia.BlendMode.SRC_ATOP;
                case DST_ATOP: return org.jetbrains.skia.BlendMode.DST_ATOP;
                case XOR: return org.jetbrains.skia.BlendMode.XOR;
                case DARKEN: return org.jetbrains.skia.BlendMode.DARKEN;
                case LIGHTEN: return org.jetbrains.skia.BlendMode.LIGHTEN;
                // Android's MULTIPLY is Skia's modulate.
                case MULTIPLY: return org.jetbrains.skia.BlendMode.MODULATE;
                case SCREEN: return org.jetbrains.skia.BlendMode.SCREEN;
                case ADD: return org.jetbrains.skia.BlendMode.PLUS;
                case OVERLAY: return org.jetbrains.skia.BlendMode.OVERLAY;
                default: return org.jetbrains.skia.BlendMode.SRC_OVER;
            }
        }
    }
}
