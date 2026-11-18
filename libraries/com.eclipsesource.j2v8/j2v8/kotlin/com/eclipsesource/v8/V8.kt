package com.eclipsesource.v8

import com.caoccao.javet.exceptions.JavetCompilationException
import com.caoccao.javet.exceptions.JavetException
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.callback.IJavetDirectCallable
import com.caoccao.javet.interop.callback.JavetCallbackContext
import com.caoccao.javet.interop.callback.JavetCallbackType
import com.caoccao.javet.values.V8Value as JavetValue
import com.caoccao.javet.values.primitive.V8ValueBoolean
import com.caoccao.javet.values.primitive.V8ValueDouble
import com.caoccao.javet.values.primitive.V8ValueInteger
import com.caoccao.javet.values.primitive.V8ValueLong
import com.caoccao.javet.values.primitive.V8ValueNull
import com.caoccao.javet.values.primitive.V8ValueString
import com.caoccao.javet.values.primitive.V8ValueUndefined
import java.util.concurrent.locks.ReentrantLock

/*
 * J2V8's API on a PC. J2V8 ships V8 for Android only (and an old Linux build that can't parse today's JavaScript), so
 * on the desktop its classes are this: the same calls, run by Javet's current V8. Only what apps commonly use is here:
 * running scripts for a string or nothing, Java callbacks with string and number arguments, and the locker.
 */

open class V8Value internal constructor() {
    @Volatile protected var released = false
    open fun release() { released = true }
    fun isReleased(): Boolean = released
    fun close() = release()
    open fun isUndefined(): Boolean = false
}

/** A JavaScript object handed to Java; on the desktop it carries only what a callback receives. */
open class V8Object internal constructor(internal val values: List<Any?> = emptyList()) : V8Value() {
    open fun getString(key: String): String? = null
    open fun getInteger(key: String): Int = 0
    open fun getObject(key: String): V8Object? = null
    open fun getArray(key: String): V8Array? = null
}

/** Arguments of a Java callback, already converted to Java values. */
class V8Array internal constructor(values: List<Any?>) : V8Object(values) {
    fun length(): Int = values.size
    fun getString(index: Int): String = values.getOrNull(index)?.toString() ?: throw V8ResultUndefined()
    fun getInteger(index: Int): Int = (values.getOrNull(index) as? Number)?.toInt() ?: values.getOrNull(index)?.toString()?.toIntOrNull() ?: 0
    fun getDouble(index: Int): Double = (values.getOrNull(index) as? Number)?.toDouble() ?: 0.0
    fun getBoolean(index: Int): Boolean = values.getOrNull(index) == true
    fun get(index: Int): Any? = values.getOrNull(index)
}

fun interface JavaVoidCallback {
    fun invoke(receiver: V8Object?, parameters: V8Array)
}

fun interface JavaCallback {
    fun invoke(receiver: V8Object?, parameters: V8Array): Any?
}

open class V8RuntimeException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
open class V8ScriptException(message: String?, cause: Throwable? = null) : V8RuntimeException(message, cause) {
    val jsMessage: String? get() = message
    val jsStackTrace: String? get() = null
    val lineNumber: Int get() = 0
    val fileName: String get() = "undefined"
}
class V8ScriptExecutionException(message: String?, cause: Throwable? = null) : V8ScriptException(message, cause)
class V8ScriptCompilationException(message: String?, cause: Throwable? = null) : V8ScriptException(message, cause)
class V8ResultUndefined(message: String? = "Result is undefined") : V8RuntimeException(message)

/** J2V8's thread lock: one thread at a time in a runtime. */
class V8Locker internal constructor() {
    private val lock = ReentrantLock()
    fun acquire() = lock.lock()
    fun tryAcquire(): Boolean = lock.tryLock()
    fun release() { if (lock.isHeldByCurrentThread) lock.unlock() }
    fun hasLock(): Boolean = lock.isHeldByCurrentThread
    fun checkThread() {}
    fun getThread(): Thread? = null
}

class V8 private constructor(private val runtime: V8Runtime) : V8Object() {
    val locker = V8Locker()
    private val callbacks = ArrayList<Any>()

    fun executeStringScript(script: String): String = executeStringScript(script, null, 0)

    fun executeStringScript(script: String, scriptName: String?, lineNumber: Int): String =
        when (val result = execute(script)) {
            null -> throw V8ResultUndefined()
            else -> result.toString()
        }

    fun executeVoidScript(script: String) { execute(script) }
    fun executeVoidScript(script: String, scriptName: String?, lineNumber: Int) { execute(script) }

    fun executeScript(script: String): Any? = execute(script)
    fun executeIntegerScript(script: String): Int = (execute(script) as? Number)?.toInt() ?: throw V8ResultUndefined()
    fun executeDoubleScript(script: String): Double = (execute(script) as? Number)?.toDouble() ?: throw V8ResultUndefined()
    fun executeBooleanScript(script: String): Boolean = (execute(script) as? Boolean) ?: throw V8ResultUndefined()

    private fun execute(script: String): Any? = guarded {
        runtime.getExecutor(script).execute<JavetValue>().use { toJava(it) }
    }

    /** Calls a global function with [parameters] and gives back what it returned, as a Java value. */
    fun executeJSFunction(name: String, vararg parameters: Any?): Any? = guarded {
        runtime.globalObject.use { global -> global.invoke<JavetValue>(name, *parameters).use { toJava(it) } }
    }

    fun registerJavaMethod(callback: JavaVoidCallback, jsFunctionName: String): V8Object {
        bind(jsFunctionName) { args -> callback.invoke(null, V8Array(args)); null }
        return this
    }

    fun registerJavaMethod(callback: JavaCallback, jsFunctionName: String): V8Object {
        bind(jsFunctionName) { args -> callback.invoke(null, V8Array(args)) }
        return this
    }

    private fun bind(name: String, call: (List<Any?>) -> Any?) = guarded {
        val callable = IJavetDirectCallable.NoThisAndResult<Exception> { values ->
            val result = call(values.map { toJava(it) })
            runtime.converter.toV8Value(runtime, result)
        }
        callbacks += callable
        runtime.globalObject.use { it.bindFunction(JavetCallbackContext(name, JavetCallbackType.DirectCallNoThisAndResult, callable)) }
    }

    override fun release() = release(true)

    fun release(reportMemoryLeaks: Boolean) {
        if (released) return
        released = true
        runCatching { runtime.close() }
    }

    fun lowMemoryNotification() { runCatching { runtime.lowMemoryNotification() } }
    fun terminateExecution() { runCatching { runtime.terminateExecution() } }

    private fun <T> guarded(block: () -> T): T = try {
        block()
    } catch (e: JavetCompilationException) {
        throw V8ScriptCompilationException(e.message, e)
    } catch (e: JavetException) {
        throw V8ScriptExecutionException(e.message, e)
    }

    companion object {
        @JvmStatic fun createV8Runtime(): V8 = V8(V8Host.getV8Instance().createV8Runtime())
        @JvmStatic fun createV8Runtime(globalAlias: String?): V8 = createV8Runtime().also { v8 ->
            if (globalAlias != null) v8.executeVoidScript("var $globalAlias = globalThis;")
        }
        @JvmStatic fun isLoaded(): Boolean = true
        @JvmStatic fun getActiveRuntimes(): Int = 0

        private fun toJava(value: JavetValue?): Any? = when (value) {
            null, is V8ValueUndefined, is V8ValueNull -> null
            is V8ValueString -> value.value
            is V8ValueInteger -> value.value
            is V8ValueLong -> value.value
            is V8ValueDouble -> value.value
            is V8ValueBoolean -> value.value
            else -> value.toString()
        }
    }
}
