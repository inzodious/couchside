package couchside.shim

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

/**
 * The app's resources by the ids in R. The build copies each module's `res` folder to `couchside-res/<namespace>/` on the
 * classpath and writes `couchside-res/index.tsv` (id, namespace, type, name, file) next to the R classes it generates.
 */
class ResTable {
    private class Entry(val namespace: String, val type: String, val name: String, val file: String)

    private val byId = HashMap<Int, Entry>()
    private val byName = HashMap<String, Int>()
    private val values = ConcurrentHashMap<String, Map<String, Element>>()
    private val fonts = ConcurrentHashMap<Int, Typeface>()
    private val bitmaps = ConcurrentHashMap<Int, Bitmap>()

    init {
        stream("couchside-res/index.tsv")?.bufferedReader()?.forEachLine { line ->
            val (id, namespace, type, name, file) = line.split('\t')
            byId[id.toInt()] = Entry(namespace, type, name, file)
            byName["$namespace:$type/$name"] = id.toInt()
        }
    }

    private fun stream(path: String): InputStream? = ResTable::class.java.classLoader.getResourceAsStream(path)
    private fun entry(id: Int) = byId[id] ?: throw android.content.res.Resources.NotFoundException("Resource ID #0x${Integer.toHexString(id)}")

    fun id(name: String, type: String?, pkg: String?): Int {
        val bare = name.substringAfter('/').substringAfter(':')
        val t = type ?: name.substringBefore('/', "").substringAfter(':').ifEmpty { return 0 }
        (listOfNotNull(pkg ?: Device.PACKAGE) + Target.namespaces).forEach { ns -> byName["$ns:$t/$bare"]?.let { return it } }
        return 0
    }

    fun name(id: Int, full: Boolean): String = entry(id).let { if (full) "${it.namespace}:${it.type}/${it.name}" else it.name }
    fun type(id: Int): String = entry(id).type
    fun pkg(id: Int): String = entry(id).namespace
    fun file(id: Int): String = entry(id).file

    /** The file behind a file resource (a picture, a font, raw data). */
    fun open(id: Int): InputStream {
        val e = entry(id)
        return stream("couchside-res/${e.namespace}/${e.file}") ?: throw android.content.res.Resources.NotFoundException("${e.type}/${e.name}")
    }

    private fun element(id: Int): Element? {
        val e = byId[id] ?: return null
        val all = values.getOrPut("${e.namespace}/${e.file}") {
            val doc = stream("couchside-res/${e.namespace}/${e.file}")?.use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) } ?: return@getOrPut emptyMap()
            val out = HashMap<String, Element>()
            val nodes = doc.documentElement.childNodes
            for (i in 0 until nodes.length) (nodes.item(i) as? Element)?.let { el ->
                val type = if (el.tagName == "item") el.getAttribute("type") else if (el.tagName == "string-array") "array" else el.tagName
                out["$type/${el.getAttribute("name").replace('.', '_')}"] = el
            }
            out
        }
        return all["${e.type}/${e.name}"]
    }

    // Android's own unescaping of a string resource: \' \" \n \t \@ \? and \\, with outer quotes dropped.
    private fun unescape(raw: String): String {
        var s = raw.trim()
        if (s.length >= 2 && s.startsWith('"') && s.endsWith('"')) s = s.substring(1, s.length - 1)
        val out = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (val n = s[i + 1]) { 'n' -> out.append('\n'); 't' -> out.append('\t'); else -> out.append(n) }
                i += 2
            } else { out.append(c); i++ }
        }
        return out.toString()
    }

    fun value(id: Int): String? = element(id)?.textContent?.let(::unescape)

    fun string(id: Int): String? {
        val text = value(id) ?: return null
        return if (text.startsWith("@string/")) string(id(text, null, pkg(id))) else text
    }

    fun strings(id: Int): Array<String> {
        val el = element(id) ?: return emptyArray()
        val items = el.getElementsByTagName("item")
        return Array(items.length) { unescape(items.item(it).textContent) }
    }

    fun color(id: Int): Int {
        val text = value(id) ?: throw android.content.res.Resources.NotFoundException("color #0x${Integer.toHexString(id)}")
        if (text.startsWith("@color/")) return color(id(text, null, pkg(id)))
        val hex = text.removePrefix("#")
        return when (hex.length) {
            3 -> ("FF" + hex.map { "$it$it" }.joinToString("")).toLong(16).toInt()
            4 -> hex.map { "$it$it" }.joinToString("").toLong(16).toInt()
            6 -> ("FF$hex").toLong(16).toInt()
            else -> hex.toLong(16).toInt()
        }
    }

    fun dimension(id: Int, metrics: DisplayMetrics): Float {
        val text = value(id) ?: return 0f
        val number = text.takeWhile { it.isDigit() || it == '.' || it == '-' }.toFloatOrNull() ?: return 0f
        return when (text.drop(number.toString().length.coerceAtMost(text.length)).ifEmpty { text.dropWhile { it.isDigit() || it == '.' || it == '-' } }) {
            "dp", "dip" -> number * metrics.density
            "sp" -> number * metrics.scaledDensity
            else -> number
        }
    }

    fun font(id: Int): Typeface = fonts.getOrPut(id) { open(id).use { Typeface.createFromBytes(it.readBytes()) } }

    /** A picture resource as a bitmap, decoded once. */
    fun bitmap(id: Int): Bitmap? = bitmaps[id] ?: runCatching { open(id).use { BitmapFactory.decodeStream(it) } }.getOrNull()?.also { bitmaps[id] = it }

    fun drawable(id: Int): Drawable? {
        val e = byId[id] ?: return null
        if (e.type == "color") return android.graphics.drawable.ColorDrawable(color(id))
        if (e.file.endsWith(".xml")) return runCatching { VectorArt.drawable(open(id).use { it.readBytes() }) }.getOrNull()
        return bitmap(id)?.let { BitmapDrawable(Device.resources(), it) }
    }
}
