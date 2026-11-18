package androidx.core.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import java.io.File
import java.util.concurrent.Executor

object ContextCompat {
    const val RECEIVER_VISIBLE_TO_INSTANT_APPS = 1
    const val RECEIVER_EXPORTED = 2
    const val RECEIVER_NOT_EXPORTED = 4
    @JvmStatic fun getDrawable(context: Context, id: Int): Drawable? = context.resources.getDrawable(id, null)
    @JvmStatic fun getColor(context: Context, id: Int): Int = context.resources.getColor(id, null)
    @JvmStatic fun checkSelfPermission(context: Context, permission: String): Int = context.checkSelfPermission(permission)
    @JvmStatic fun getMainExecutor(context: Context): Executor = context.mainExecutor
    @JvmStatic fun startForegroundService(context: Context, intent: Intent) { context.startForegroundService(intent) }
    @JvmStatic fun startActivity(context: Context, intent: Intent, options: android.os.Bundle?) { context.startActivity(intent, options) }
    @JvmStatic fun registerReceiver(context: Context, receiver: BroadcastReceiver?, filter: IntentFilter, flags: Int): Intent? = context.registerReceiver(receiver, filter, flags)
    @JvmStatic fun <T> getSystemService(context: Context, serviceClass: Class<T>): T? = context.getSystemService(serviceClass)
    @JvmStatic fun getSystemServiceName(context: Context, serviceClass: Class<*>): String? = context.getSystemServiceName(serviceClass)
    @JvmStatic fun getNoBackupFilesDir(context: Context): File? = context.noBackupFilesDir
    @JvmStatic fun getCodeCacheDir(context: Context): File = context.codeCacheDir
    @JvmStatic fun getExternalFilesDirs(context: Context, type: String?): Array<File> = context.getExternalFilesDirs(type)
    @JvmStatic fun getExternalCacheDirs(context: Context): Array<File> = context.externalCacheDirs
    @JvmStatic fun createDeviceProtectedStorageContext(context: Context): Context = context
    @JvmStatic fun getAttributionTag(context: Context): String? = null
}
