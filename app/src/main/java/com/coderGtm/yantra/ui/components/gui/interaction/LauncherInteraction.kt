package com.coderGtm.yantra.ui.components.gui.interaction

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.withTimeoutOrNull

enum class DragSource {
    HOME,
    DRAWER,
    DOCK
}

data class DragSession(
    val itemId: String,
    val source: DragSource,
    val startPosition: Offset = Offset.Zero,
    val currentPosition: Offset = Offset.Zero,
    val hasMoved: Boolean = false
)

suspend fun PointerInputScope.detectLauncherItemGestures(
    onTap: () -> Unit,
    onLongPressWithoutMove: () -> Unit,
    onDragStart: (startOffset: Offset) -> Unit,
    onDrag: (dragAmount: Offset) -> Unit,
    onDrop: () -> Unit,
    onCancel: () -> Unit = {}
) {
    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val downPosition = down.position
        val pointerId = down.id

        var isLongPress = false
        var isDragging = false
        var isReleasedBeforeLongPress = false

        val timedOut = withTimeoutOrNull(longPressTimeout) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                if (!change.pressed) {
                    change.consume()
                    isReleasedBeforeLongPress = true
                    break
                }
                val distance = (change.position - downPosition).getDistance()
                if (distance > touchSlop) {
                    break
                }
            }
        }

        if (isReleasedBeforeLongPress) {
            onTap()
            return@awaitEachGesture
        }

        if (timedOut == null) {
            isLongPress = true
        }

        if (isLongPress) {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                if (!change.pressed) {
                    change.consume()
                    if (isDragging) {
                        onDrop()
                    } else {
                        onLongPressWithoutMove()
                    }
                    return@awaitEachGesture
                }

                val dragDelta = change.positionChange()
                val totalDistance = (change.position - downPosition).getDistance()

                if (!isDragging && totalDistance > touchSlop) {
                    isDragging = true
                    onDragStart(downPosition)
                }

                if (isDragging) {
                    change.consume()
                    onDrag(dragDelta)
                }
            }
        } else {
            onCancel()
        }
    }
}
