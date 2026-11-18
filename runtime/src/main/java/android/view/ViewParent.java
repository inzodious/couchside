package android.view;

public interface ViewParent {
    ViewParent getParent();
    void bringChildToFront(View child);
    default void requestLayout() {}
    default boolean isLayoutRequested() { return false; }
    default void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
}
