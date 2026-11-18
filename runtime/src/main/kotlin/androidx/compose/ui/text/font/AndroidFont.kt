// Android's `Font(R.font.x, ...)`: the same font file, loaded from the app's res folder on the classpath.
package androidx.compose.ui.text.font

import couchside.shim.Device

fun Font(resId: Int, weight: FontWeight = FontWeight.Normal, style: FontStyle = FontStyle.Normal, loadingStrategy: FontLoadingStrategy = FontLoadingStrategy.Blocking): Font {
    val table = Device.res()
    return androidx.compose.ui.text.platform.Font(identity = table.name(resId, true), getData = { table.open(resId).use { it.readBytes() } }, weight = weight, style = style)
}
