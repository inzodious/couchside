package couchside.media

import java.util.Collections
import java.util.WeakHashMap

/** A SurfaceTexture's pictures (a TextureView's video), kept with the texture so the view can draw them. */
object Textures {
    private val outputs = Collections.synchronizedMap(WeakHashMap<Any, VideoOutput>())

    @JvmStatic fun output(texture: Any?): VideoOutput = if (texture == null) VideoOutput() else outputs.getOrPut(texture) { VideoOutput() }
}

/** Surfaces for video views; apps compile against Android's Surface, which doesn't show these. */
object Surfaces {
    @JvmStatic fun create(): android.view.Surface = android.view.Surface()
    @JvmStatic fun output(surface: android.view.Surface): VideoOutput = surface.output()
}
