package androidx.core.content.res

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable

object ResourcesCompat {
    const val ID_NULL = 0
    @JvmStatic fun getFont(context: Context, id: Int): Typeface? = context.resources.getFont(id)
    @JvmStatic fun getDrawable(res: Resources, id: Int, theme: Resources.Theme?): Drawable? = res.getDrawable(id, theme)
    @JvmStatic fun getDrawableForDensity(res: Resources, id: Int, density: Int, theme: Resources.Theme?): Drawable? = res.getDrawable(id, theme)
    @JvmStatic fun getColor(res: Resources, id: Int, theme: Resources.Theme?): Int = res.getColor(id, theme)
    @JvmStatic fun getFloat(res: Resources, id: Int): Float = res.getDimension(id)
}
