package androidx.core.view

import android.view.View

/** The ViewCompat calls a Compose TV app makes. There's no screen reader on the simulated TV, so no view has a delegate. */
object ViewCompat {
    @JvmStatic fun getAccessibilityDelegate(view: View): AccessibilityDelegateCompat? = null
    @JvmStatic fun setAccessibilityDelegate(view: View, delegate: AccessibilityDelegateCompat?) {}
    @JvmStatic fun isAttachedToWindow(view: View): Boolean = true
    @JvmStatic fun isLaidOut(view: View): Boolean = true
}
