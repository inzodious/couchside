package androidx.activity.result

import androidx.activity.result.contract.ActivityResultContract

fun interface ActivityResultCallback<O> {
    fun onActivityResult(result: O)
}

abstract class ActivityResultLauncher<I> {
    abstract val contract: ActivityResultContract<I, *>
    open fun launch(input: I) = launch(input, null)
    abstract fun launch(input: I, options: Any?)
    abstract fun unregister()
}

class ActivityResult(val resultCode: Int, val data: android.content.Intent?)
