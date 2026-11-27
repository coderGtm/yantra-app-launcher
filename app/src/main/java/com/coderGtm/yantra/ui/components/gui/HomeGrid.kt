package com.coderGtm.yantra.ui.components.gui

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import com.coderGtm.yantra.gui.desktop.model.AppInfo
import com.coderGtm.yantra.gui.desktop.model.HomeItem
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.components.gui.interaction.DragSession
import com.coderGtm.yantra.ui.components.gui.interaction.DragSource
import com.coderGtm.yantra.ui.components.gui.interaction.detectLauncherItemGestures
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState

@Composable
fun HomeGrid(
    columns: Int,
    rows: Int,
    items: List<HomeItem>,
    state: NativeGuiState,
    appResolver: YantraAppResolver,
    onDrop: (String, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Layout(
        content = {
            items.forEach { item ->
                var itemPosition by remember { mutableStateOf(Offset.Zero) }
                val isDragging = state.draggingPackageName == item.packageName

                HomeItemView(
                    packageName = item.packageName,
                    modifier = Modifier
                        .alpha(if (isDragging) 0.4f else 1.0f)
                        .onGloballyPositioned { itemPosition = it.positionInWindow() }
                        .pointerInput(item.packageName) {
                            detectLauncherItemGestures(
                                onTap = {
                                    appResolver.launch(item.packageName)
                                },
                                onLongPressWithoutMove = {
                                    runCatching {
                                        val pm = context.packageManager
                                        val info = pm.getApplicationInfo(item.packageName, 0)
                                        state.selectedAppForMenu = AppInfo(
                                            packageName = item.packageName,
                                            label = pm.getApplicationLabel(info).toString(),
                                            icon = pm.getApplicationIcon(info)
                                        )
                                    }
                                },
                                onDragStart = { startPos ->
                                    state.dragSession = DragSession(
                                        itemId = item.packageName,
                                        source = DragSource.HOME,
                                        startPosition = itemPosition + startPos,
                                        currentPosition = itemPosition + startPos
                                    )
                                },
                                onDrag = { dragDelta ->
                                    state.dragOffset += dragDelta
                                },
                                onDrop = {
                                    onDrop(item.packageName, state.dragOffset)
                                    state.dragSession = null
                                },
                                onCancel = {
                                    state.dragSession = null
                                }
                            )
                        }
                )
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val cellWidth = constraints.maxWidth / columns
        val cellHeight = constraints.maxHeight / rows
        
        val placeables = measurables.map { measurable ->
            measurable.measure(Constraints.fixed(cellWidth, cellHeight))
        }
        
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val item = items[index]
                placeable.placeRelative(
                    x = item.column * cellWidth,
                    y = item.row * cellHeight
                )
            }
        }
    }
}
