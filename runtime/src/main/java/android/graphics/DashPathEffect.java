package android.graphics;

public class DashPathEffect extends PathEffect {
    public DashPathEffect(float[] intervals, float phase) { skia = org.jetbrains.skia.PathEffect.Companion.makeDash(intervals, phase); }
}
