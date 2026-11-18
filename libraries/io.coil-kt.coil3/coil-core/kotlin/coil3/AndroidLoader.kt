// Coil's Android-only loader option: it limits Android's bitmap decoder, which the desktop doesn't use.
package coil3

fun ImageLoader.Builder.bitmapFactoryMaxParallelism(maxParallelism: Int): ImageLoader.Builder = this
