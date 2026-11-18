// Android's resource accessors for Compose, reading the app's res folders through the shim (ResTable).
package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Density
import couchside.shim.Device
import org.xml.sax.InputSource

@Composable
fun painterResource(id: Int): Painter {
    val table = Device.res()
    if (table.file(id).endsWith(".xml")) {
        val vector = remember(id) { table.open(id).use { loadXmlImageVector(InputSource(it), Density(2f)) } }
        return rememberVectorPainter(vector)
    }
    return remember(id) { BitmapPainter(requireNotNull(table.bitmap(id)) { "Not a picture: ${table.name(id, true)}" }.asImageBitmap()) }
}

fun ImageBitmap.Companion.imageResource(id: Int): ImageBitmap = requireNotNull(Device.res().bitmap(id)).asImageBitmap()

@Composable
fun ImageVector.Companion.vectorResource(id: Int): ImageVector = remember(id) { Device.res().open(id).use { loadXmlImageVector(InputSource(it), Density(2f)) } }

@Composable @ReadOnlyComposable
fun stringResource(id: Int): String = Device.resources().getString(id)

@Composable @ReadOnlyComposable
fun stringResource(id: Int, vararg formatArgs: Any): String = Device.resources().getString(id, *formatArgs)

@Composable @ReadOnlyComposable
fun stringArrayResource(id: Int): Array<String> = Device.resources().getStringArray(id)

@Composable @ReadOnlyComposable
fun colorResource(id: Int): androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(Device.resources().getColor(id, null))
