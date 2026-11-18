package androidx.activity.result.contract

import android.content.Context
import android.content.Intent

abstract class ActivityResultContract<I, O> {
    abstract fun createIntent(context: Context, input: I): Intent
    abstract fun parseResult(resultCode: Int, intent: Intent?): O
}

class ActivityResultContracts private constructor() {
    class RequestPermission : ActivityResultContract<String, Boolean>() {
        override fun createIntent(context: Context, input: String): Intent = Intent("androidx.activity.result.contract.action.REQUEST_PERMISSIONS")
        override fun parseResult(resultCode: Int, intent: Intent?): Boolean = false
    }

    class StartActivityForResult : ActivityResultContract<Intent, androidx.activity.result.ActivityResult>() {
        override fun createIntent(context: Context, input: Intent): Intent = input
        override fun parseResult(resultCode: Int, intent: Intent?) = androidx.activity.result.ActivityResult(resultCode, intent)
    }
}
