package com.coderGtm.yantra.ui.components.gui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import com.coderGtm.yantra.gui.desktop.model.HomeItem
import com.coderGtm.yantra.gui.web.YantraAppResolver

@Composable
fun HomeGrid(
    columns: Int,
    rows: Int,
    items: List<HomeItem>,
    appResolver: YantraAppResolver,
    onItemLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Layout(
        content = {
            items.forEach { item ->
                HomeItemView(
                    packageName = item.packageName,
                    onClick = { appResolver.launch(item.packageName) },
                    onLongClick = { onItemLongClick(item.packageName) }
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
