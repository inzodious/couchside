package android.view.accessibility;

import java.util.ArrayList;
import java.util.List;

/** No screen reader on the simulated TV. */
public final class AccessibilityManager {
    public interface AccessibilityStateChangeListener { void onAccessibilityStateChanged(boolean enabled); }
    public interface TouchExplorationStateChangeListener { void onTouchExplorationStateChanged(boolean enabled); }
    public interface AccessibilityServicesStateChangeListener { void onAccessibilityServicesStateChanged(AccessibilityManager manager); }
    public static final int FLAG_CONTENT_ICONS = 1, FLAG_CONTENT_TEXT = 2, FLAG_CONTENT_CONTROLS = 4;

    public boolean isEnabled() { return false; }
    public boolean isTouchExplorationEnabled() { return false; }
    public boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) { return true; }
    public void addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener, android.os.Handler handler) {}
    public boolean removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) { return true; }
    public boolean addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) { return true; }
    public void addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener, android.os.Handler handler) {}
    public boolean removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) { return true; }
    public void addAccessibilityServicesStateChangeListener(AccessibilityServicesStateChangeListener listener) {}
    public boolean removeAccessibilityServicesStateChangeListener(AccessibilityServicesStateChangeListener listener) { return true; }
    public List<android.accessibilityservice.AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) { return new ArrayList<>(); }
    public List<android.accessibilityservice.AccessibilityServiceInfo> getInstalledAccessibilityServiceList() { return new ArrayList<>(); }
    public void sendAccessibilityEvent(AccessibilityEvent event) {}
    public void interrupt() {}
    public int getRecommendedTimeoutMillis(int originalTimeout, int uiContentFlags) { return originalTimeout; }
    public boolean isRequestFromAccessibilityTool() { return false; }
}
