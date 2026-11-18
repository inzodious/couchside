package couchside.shim

import android.content.SharedPreferences
import java.io.File
import java.util.concurrent.CopyOnWriteArraySet
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** SharedPreferences kept as one JSON file; each value is stored with its type so it reads back as what was put. */
class Prefs(private val file: File) : SharedPreferences {
    private val values = HashMap<String, Any>()
    private val listeners = CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()

    init {
        runCatching {
            Json.parseToJsonElement(file.readText()).jsonObject.forEach { (key, entry) ->
                val o = entry.jsonObject
                val v = o.getValue("v")
                values[key] = when (o.getValue("t").jsonPrimitive.content) {
                    "b" -> v.jsonPrimitive.content.toBoolean()
                    "i" -> v.jsonPrimitive.content.toInt()
                    "l" -> v.jsonPrimitive.content.toLong()
                    "f" -> v.jsonPrimitive.content.toFloat()
                    "ss" -> v.jsonArray.map { it.jsonPrimitive.content }.toSet()
                    else -> v.jsonPrimitive.content
                }
            }
        }
    }

    private fun save() {
        val json = JsonObject(
            synchronized(values) { values.toSortedMap() }.mapValues { (_, v) ->
                val (type, value: JsonElement) = when (v) {
                    is Boolean -> "b" to JsonPrimitive(v)
                    is Int -> "i" to JsonPrimitive(v)
                    is Long -> "l" to JsonPrimitive(v)
                    is Float -> "f" to JsonPrimitive(v)
                    is Set<*> -> "ss" to JsonArray(v.map { JsonPrimitive(it.toString()) })
                    else -> "s" to JsonPrimitive(v.toString())
                }
                JsonObject(mapOf("t" to JsonPrimitive(type), "v" to value))
            },
        )
        runCatching {
            val tmp = File(file.parentFile, file.name + ".tmp")
            tmp.writeText(json.toString())
            java.nio.file.Files.move(tmp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun getAll(): MutableMap<String, *> = synchronized(values) { HashMap(values) }
    override fun contains(key: String): Boolean = synchronized(values) { key in values }
    private inline fun <reified T> get(key: String, def: T): T = synchronized(values) { values[key] as? T ?: def }
    override fun getString(key: String, defValue: String?): String? = get(key, defValue)
    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
        synchronized(values) { (values[key] as? Set<*>)?.map { it.toString() }?.toMutableSet() ?: defValues }
    override fun getInt(key: String, defValue: Int): Int = get(key, defValue)
    override fun getLong(key: String, defValue: Long): Long = get(key, defValue)
    override fun getFloat(key: String, defValue: Float): Float = get(key, defValue)
    override fun getBoolean(key: String, defValue: Boolean): Boolean = get(key, defValue)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners += listener }
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) { listeners -= listener }

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val puts = HashMap<String, Any?>()
        private var clear = false
        private fun put(key: String, value: Any?) = apply { puts[key] = value }
        override fun putString(key: String, value: String?) = put(key, value)
        override fun putStringSet(key: String, values: MutableSet<String>?) = put(key, values?.toSet())
        override fun putInt(key: String, value: Int) = put(key, value)
        override fun putLong(key: String, value: Long) = put(key, value)
        override fun putFloat(key: String, value: Float) = put(key, value)
        override fun putBoolean(key: String, value: Boolean) = put(key, value)
        override fun remove(key: String) = put(key, null)
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean {
            val changed = ArrayList<String>()
            synchronized(values) {
                if (clear) { changed += values.keys; values.clear() }
                puts.forEach { (k, v) ->
                    if (v == null) { if (values.remove(k) != null) changed += k } else if (values.put(k, v) != v) changed += k
                }
            }
            save()
            changed.forEach { key -> listeners.forEach { it.onSharedPreferenceChanged(this@Prefs, key) } }
            return true
        }
        override fun apply() { commit() }
    }
}
