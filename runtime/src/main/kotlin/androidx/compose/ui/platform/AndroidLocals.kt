// The Android-only composition locals of androidx.compose.ui.platform. The desktop host provides them (Host.kt).
package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf

val LocalContext = staticCompositionLocalOf<android.content.Context> { error("No Android context: the desktop host provides one") }

val LocalConfiguration = staticCompositionLocalOf<android.content.res.Configuration> { error("No configuration: the desktop host provides one") }

val LocalView = staticCompositionLocalOf<android.view.View> { error("No view: the desktop host provides one") }
