// Coil's Android-only request options. They tune Android's bitmap decoder, so on desktop they change nothing.
package coil3.request

fun ImageRequest.Builder.allowRgb565(enable: Boolean): ImageRequest.Builder = this

fun ImageRequest.Builder.allowHardware(enable: Boolean): ImageRequest.Builder = this

fun coil3.ImageLoader.Builder.allowRgb565(enable: Boolean): coil3.ImageLoader.Builder = this

fun coil3.ImageLoader.Builder.allowHardware(enable: Boolean): coil3.ImageLoader.Builder = this
