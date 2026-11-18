package couchside

import android.view.KeyEvent
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The mouse, for a TV app that only knows the remote: a left click on something that can take focus focuses it and
 * presses OK on it, the way pointing and pressing would on a TV with a pointer remote; a right click is Back, and the
 * wheel is the D-pad's up and down. A press on nothing that takes focus is left to the app.
 *
 * What's under the pointer is found in the semantics tree (what accessibility sees): the innermost node that can take
 * focus. Compose keeps that tree on the window's owner, which it doesn't expose, so it's reached by reflection.
 */
/** The test tag on the top activity's layer, where the pointer looks. */
const val TOP_LAYER = "couchside:top"

fun Modifier.tvPointer(enabled: () -> Boolean): Modifier = this then PointerElement(enabled)

private class PointerElement(val enabled: () -> Boolean) : ModifierNodeElement<PointerNode>() {
    override fun create() = PointerNode(enabled)
    override fun update(node: PointerNode) { node.enabled = enabled }
    override fun equals(other: Any?) = other is PointerElement && other.enabled === enabled
    override fun hashCode() = enabled.hashCode()
}

private class PointerNode(var enabled: () -> Boolean) : Modifier.Node(), PointerInputModifierNode {
    private var pressed: SemanticsNode? = null

    override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
        if (pass != PointerEventPass.Initial || !enabled()) return
        val change = pointerEvent.changes.firstOrNull() ?: return
        when (pointerEvent.type) {
            PointerEventType.Press -> when {
                pointerEvent.buttons.isSecondaryPressed -> { RemoteKeys.tap(KeyEvent.KEYCODE_BACK); change.consume() }
                pointerEvent.buttons.isPrimaryPressed -> {
                    val root = requireLayoutCoordinates().positionInRoot()
                    pressed = focusableAt(change.position + root)?.also { change.consume() }
                }
            }
            PointerEventType.Release -> pressed?.let { node ->
                pressed = null
                change.consume()
                activate(node)
            }
            PointerEventType.Move -> if (pressed != null) change.consume()
            // A TV's lists follow focus, so the wheel moves focus a step per notch (with Shift, sideways).
            PointerEventType.Scroll -> {
                val delta = change.scrollDelta
                val sideways = pointerEvent.keyboardModifiers.isShiftPressed || delta.x != 0f
                val amount = if (delta.x != 0f) delta.x else delta.y
                if (amount != 0f) {
                    RemoteKeys.tap(when {
                        sideways && amount > 0 -> KeyEvent.KEYCODE_DPAD_RIGHT
                        sideways -> KeyEvent.KEYCODE_DPAD_LEFT
                        amount > 0 -> KeyEvent.KEYCODE_DPAD_DOWN
                        else -> KeyEvent.KEYCODE_DPAD_UP
                    }, holdMs = 0)
                    change.consume()
                }
            }
        }
    }

    override fun onCancelPointerInput() { pressed = null }

    /** Focus it, then OK on it: its click action when it has one (a button), else the OK key (a card that reads keys). */
    private fun activate(node: SemanticsNode) {
        val focused = node.config.getOrNull(SemanticsProperties.Focused) == true
        if (!focused) node.config.getOrNull(SemanticsActions.RequestFocus)?.action?.invoke()
        coroutineScope.launch {
            if (!focused) delay(50)
            val click = node.config.getOrNull(SemanticsActions.OnClick)?.action
            if (click != null) click() else RemoteKeys.tap(KeyEvent.KEYCODE_DPAD_CENTER)
        }
    }

    private fun focusableAt(point: Offset): SemanticsNode? {
        val owner = semanticsOwner(this) ?: return null
        var best: SemanticsNode? = null
        fun visit(node: SemanticsNode) {
            if (!node.boundsInRoot.contains(point)) return
            val config = node.config
            if (SemanticsActions.RequestFocus in config || SemanticsProperties.Focused in config) best = node
            node.children.forEach(::visit)
        }
        // The window's tree holds every activity, drawn or not: only the one on top (tagged by its layer) counts.
        fun top(node: SemanticsNode): SemanticsNode? =
            if (node.config.getOrNull(SemanticsProperties.TestTag) == TOP_LAYER) node else node.children.firstNotNullOfOrNull(::top)
        visit(top(owner.unmergedRootSemanticsNode) ?: return null)
        return best
    }
}

private val requireOwner by lazy {
    Class.forName("androidx.compose.ui.node.DelegatableNodeKt").getMethod("requireOwner", DelegatableNode::class.java)
}

private fun semanticsOwner(node: DelegatableNode): SemanticsOwner? = runCatching {
    val owner = requireOwner.invoke(null, node)
    owner.javaClass.getMethod("getSemanticsOwner").apply { isAccessible = true }.invoke(owner) as SemanticsOwner
}.getOrNull()
