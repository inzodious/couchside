package android.graphics;

public class CornerPathEffect extends PathEffect {
    public CornerPathEffect(float radius) { skia = org.jetbrains.skia.PathEffect.Companion.makeCorner(radius); }
}
