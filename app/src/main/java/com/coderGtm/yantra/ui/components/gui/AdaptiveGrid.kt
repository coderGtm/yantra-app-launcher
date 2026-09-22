package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class GridInfo(
    val columns: Int,
    val rows: Int
)

@Composable
fun AdaptiveGrid(
    modifier: Modifier = Modifier,
    content: @Composable (GridInfo) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val width = maxWidth
        val height = maxHeight
        
        val cols = when {
            width < 400.dp -> 4
            width < 600.dp -> 5
            else -> 6
        }
        
        val rows = when {
            height < 600.dp -> 4
            height < 800.dp -> 5
            height < 1000.dp -> 6
            else -> 7
        }
        
        content(GridInfo(cols, rows))
    }
}
