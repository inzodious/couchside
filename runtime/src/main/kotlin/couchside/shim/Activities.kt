package couchside.shim

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.Lifecycle

/** An activity whose lifecycle other code watches (ComponentActivity): the host tells it each step. */
interface LifecycleHost {
    fun handle(event: Lifecycle.Event)
}

/**
 * The simulated TV's activities. The last one in [stack] is on screen and resumed; the ones under it are stopped, as on
 * Android. An activity that's already here is brought forward, as `singleTask` activities (a TV app's usual kind) are.
 */
object Activities {
    /** Read by the desktop host's composition, which draws the top activity. */
    val stack = mutableStateListOf<Activity>()

    val top: Activity? get() = stack.lastOrNull()

    /** Set by the desktop host: passes a key into what the activity shows, and says whether it was used. */
    @Volatile var sendToContent: (KeyEvent) -> Boolean = { false }

    /** Set by the desktop host: draws the screen into a canvas, for the app's own screenshots. */
    @Volatile var draw: (android.graphics.Canvas) -> Unit = {}

    private var since = System.currentTimeMillis()

    private fun classFor(component: ComponentName): Pair<String, Class<*>>? {
        val name = when {
            component.packageName != Device.PACKAGE -> Apps.STAND_IN
            component.className == Apps.HOME_ALIAS && Apps.HOME != null -> Apps.HOME
            component.className.startsWith(".") -> Device.PACKAGE + component.className
            else -> component.className
        }
        return runCatching { name to Class.forName(name) }.getOrNull()
    }

    private fun onMain(work: () -> Unit) {
        if (java.awt.EventQueue.isDispatchThread()) work() else java.awt.EventQueue.invokeLater(work)
    }

    private fun event(activity: Activity, event: Lifecycle.Event) = (activity as? LifecycleHost)?.handle(event)

    private fun countUse() {
        val now = System.currentTimeMillis()
        top?.let { Apps.addUse(pkgOf(it), now - since) }
        since = now
    }

    private fun pkgOf(activity: Activity) = activity.componentName?.packageName ?: Device.PACKAGE

    private fun pause(activity: Activity, leaving: Boolean) {
        countUse()
        if (leaving) activity.performUserLeaving()
        event(activity, Lifecycle.Event.ON_PAUSE)
        activity.performPause()
    }

    private fun stop(activity: Activity) {
        event(activity, Lifecycle.Event.ON_STOP)
        activity.performStop()
    }

    private fun resume(activity: Activity, restart: Boolean) {
        if (restart) activity.performRestart()
        activity.performStart()
        event(activity, Lifecycle.Event.ON_START)
        activity.performResume()
        event(activity, Lifecycle.Event.ON_RESUME)
        since = System.currentTimeMillis()
    }

    private fun destroy(activity: Activity) {
        event(activity, Lifecycle.Event.ON_DESTROY)
        activity.performDestroy()
    }

    /** Opens what the intent names: one of the app's activities, or the stand-in screen for another app. */
    fun start(from: Context, intent: Intent) {
        val component = intent.component
            ?: Apps.query(intent).firstOrNull()?.activityInfo?.let { ComponentName(it.packageName, it.name) }
            ?: throw android.content.ActivityNotFoundException("No Activity found to handle $intent")
        val (name, type) = classFor(component) ?: throw android.content.ActivityNotFoundException("Unable to find explicit activity class $component")
        onMain {
            val was = top
            val existing = stack.firstOrNull { it.javaClass.name == name && (name != Apps.STAND_IN || pkgOf(it) == component.packageName) }
            when {
                existing != null && existing === was -> {
                    // Android pauses a single-task activity around a new intent.
                    event(existing, Lifecycle.Event.ON_PAUSE)
                    existing.performPause()
                    existing.setIntentQuietly(intent)
                    existing.performNewIntent(intent)
                    existing.performResume()
                    event(existing, Lifecycle.Event.ON_RESUME)
                }
                existing != null -> {
                    was?.let { pause(it, leaving = true) }
                    stack.remove(existing)
                    stack.add(existing)
                    existing.performNewIntent(intent)
                    resume(existing, restart = true)
                    was?.let(::stop)
                }
                else -> {
                    was?.let { pause(it, leaving = true) }
                    val activity = type.getDeclaredConstructor().newInstance() as Activity
                    activity.attach(intent, component)
                    stack.add(activity)
                    Device.application().lifecycleCallbacks.forEach { it.onActivityPreCreated(activity, null) }
                    activity.performCreate()
                    event(activity, Lifecycle.Event.ON_CREATE)
                    Device.application().lifecycleCallbacks.forEach { it.onActivityCreated(activity, null) }
                    if (!activity.isFinishing) {
                        resume(activity, restart = false)
                        Device.application().lifecycleCallbacks.forEach { it.onActivityResumed(activity) }
                    }
                    was?.let(::stop)
                }
            }
        }
    }

    private fun Activity.setIntentQuietly(intent: Intent) {}

    fun finish(activity: Activity) = onMain {
        if (activity !in stack) return@onMain
        val wasTop = activity === top
        if (wasTop) pause(activity, leaving = false)
        stack.remove(activity)
        if (wasTop) top?.let { resume(it, restart = true) }
        stop(activity)
        destroy(activity)
        Device.application().lifecycleCallbacks.forEach { it.onActivityDestroyed(activity) }
        if (stack.isEmpty()) home()
    }

    fun toBack(activity: Activity): Boolean {
        onMain {
            if (activity !== top || stack.size < 2) return@onMain
            pause(activity, leaving = true)
            stack.remove(activity)
            stack.add(0, activity)
            top?.let { resume(it, restart = true) }
            stop(activity)
        }
        return true
    }

    fun isRoot(activity: Activity): Boolean = stack.firstOrNull() === activity

    fun recreate(activity: Activity) {
        val intent = activity.intent
        finish(activity)
        onMain { start(Device.application(), intent) }
    }

    /** The remote's Home button: the app's home screen when it is a launcher app, else its launcher activity. */
    fun home() = start(
        Device.application(),
        Intent(Intent.ACTION_MAIN).addCategory(if (Apps.HOME_ALIAS != null) Intent.CATEGORY_HOME else Intent.CATEGORY_LEANBACK_LAUNCHER)
            .setComponent(ComponentName(Device.PACKAGE, Apps.HOME_ALIAS ?: Apps.MAIN ?: error("The app has no launcher activity")))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    /** A key from the remote, to the activity on screen. */
    fun key(event: KeyEvent): Boolean = top?.dispatchKeyEvent(event) ?: false

    fun toContent(activity: Activity, event: KeyEvent): Boolean = activity === top && sendToContent(event)

    fun drawScreen(canvas: android.graphics.Canvas) = draw(canvas)

    /** The host is closing: stop counting time and let the activities finish cleanly. */
    fun shutdown() {
        countUse()
        stack.toList().asReversed().forEach { a ->
            runCatching {
                if (a === top) { event(a, Lifecycle.Event.ON_PAUSE); a.performPause() }
                stop(a)
                destroy(a)
            }
        }
        stack.clear()
    }
}
