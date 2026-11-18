// androidx.activity for the desktop simulator: the same names a Compose app's activities use, over the shim's android.app.Activity.
package androidx.activity

import android.app.Application
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import couchside.shim.Device
import couchside.shim.LifecycleHost
import kotlin.reflect.KClass

/** Makes a view model the way Android's default factory does: with the Application when it asks for one. */
object SimulatorViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val type = modelClass.java
        val made = if (AndroidViewModel::class.java.isAssignableFrom(type)) {
            type.getConstructor(Application::class.java).newInstance(Device.application())
        } else {
            type.getDeclaredConstructor().newInstance()
        }
        return made
    }
}

class OnBackPressedCallbackHandle(var enabled: Boolean, val onBack: () -> Unit)

/** The newest enabled BackHandler gets Back, like Android's dispatcher. */
class OnBackPressedDispatcher {
    private val callbacks = ArrayList<OnBackPressedCallbackHandle>()
    fun add(callback: OnBackPressedCallbackHandle) { callbacks += callback }
    fun remove(callback: OnBackPressedCallbackHandle) { callbacks -= callback }
    fun hasEnabledCallbacks(): Boolean = callbacks.any { it.enabled }
    /** True when a handler took it. */
    fun dispatch(): Boolean {
        val callback = callbacks.lastOrNull { it.enabled } ?: return false
        callback.onBack()
        return true
    }
}

open class ComponentActivity : android.app.Activity(), LifecycleOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory, LifecycleHost {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore = ViewModelStore()
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory get() = SimulatorViewModelFactory
    val onBackPressedDispatcher = OnBackPressedDispatcher()

    /** What `setContent` was given; the desktop host draws it while this activity is on top. */
    var content: (@Composable () -> Unit)? by mutableStateOf(null)

    override fun handle(event: Lifecycle.Event) {
        registry.handleLifecycleEvent(event)
        if (event == Lifecycle.Event.ON_DESTROY) viewModelStore.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState) }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (!onBackPressedDispatcher.dispatch()) super.onBackPressed()
    }

    fun <I, O> registerForActivityResult(contract: ActivityResultContract<I, O>, callback: ActivityResultCallback<O>): ActivityResultLauncher<I> =
        object : ActivityResultLauncher<I>() {
            override val contract: ActivityResultContract<I, *> get() = contract
            override fun launch(input: I, options: Any?) {
                // The simulated TV answers a permission request at once; anything else comes back cancelled.
                @Suppress("UNCHECKED_CAST")
                val result = when (contract) {
                    is ActivityResultContracts.RequestPermission -> (Device.permission(input as String) == android.content.pm.PackageManager.PERMISSION_GRANTED) as O
                    else -> contract.parseResult(RESULT_CANCELED, null)
                }
                runOnUiThread { callback.onActivityResult(result) }
            }
            override fun unregister() {}
        }
}

inline fun <reified VM : ViewModel> ComponentActivity.viewModels(): Lazy<VM> =
    lazy(LazyThreadSafetyMode.NONE) { ViewModelProvider.create(viewModelStore, SimulatorViewModelFactory)[VM::class] }
