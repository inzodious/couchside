package android.view;

import android.content.Context;

public abstract class ViewGroup extends View implements ViewManager, ViewParent {
    public interface OnHierarchyChangeListener {
        void onChildViewAdded(View parent, View child);
        void onChildViewRemoved(View parent, View child);
    }
    public static final int FOCUS_BEFORE_DESCENDANTS = 0x20000, FOCUS_AFTER_DESCENDANTS = 0x40000, FOCUS_BLOCK_DESCENDANTS = 0x60000;
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {}
    @Override public void bringChildToFront(View child) { if (children.remove(child)) children.add(child); }
    public static class LayoutParams {
        public static final int FILL_PARENT = -1, MATCH_PARENT = -1, WRAP_CONTENT = -2;
        public int width, height;
        public LayoutParams(int width, int height) { this.width = width; this.height = height; }
        public LayoutParams(LayoutParams source) { width = source.width; height = source.height; }
    }
    public static class MarginLayoutParams extends LayoutParams {
        public int leftMargin, topMargin, rightMargin, bottomMargin;
        public MarginLayoutParams(int width, int height) { super(width, height); }
        public MarginLayoutParams(LayoutParams source) { super(source); }
        public void setMargins(int left, int top, int right, int bottom) { leftMargin = left; topMargin = top; rightMargin = right; bottomMargin = bottom; }
    }

    private final java.util.List<View> children = new java.util.ArrayList<>();

    public ViewGroup(Context context) { super(context); }
    public ViewGroup(Context context, android.util.AttributeSet attrs) { super(context); }
    public ViewGroup(Context context, android.util.AttributeSet attrs, int defStyleAttr) { super(context); }

    private void adopt(View child) { child.parent = this; }
    public void addView(View child) { children.add(child); adopt(child); }
    public void addView(View child, int index) { children.add(index < 0 ? children.size() : index, child); adopt(child); }
    public void addView(View child, int width, int height) { children.add(child); adopt(child); }
    @Override public void addView(View child, LayoutParams params) { children.add(child); adopt(child); }
    public void addView(View child, int index, LayoutParams params) { addView(child, index); }
    @Override public void updateViewLayout(View view, LayoutParams params) {}
    @Override public void removeView(View view) { if (children.remove(view)) view.parent = null; }
    public void removeAllViews() { children.clear(); }
    public void removeViewAt(int index) { children.remove(index); }
    public int getChildCount() { return children.size(); }
    public View getChildAt(int index) { return index >= 0 && index < children.size() ? children.get(index) : null; }
    public int indexOfChild(View child) { return children.indexOf(child); }
    public void setClipChildren(boolean clipChildren) {}
    public void setClipToPadding(boolean clipToPadding) {}
    public void setDescendantFocusability(int focusability) {}
}
