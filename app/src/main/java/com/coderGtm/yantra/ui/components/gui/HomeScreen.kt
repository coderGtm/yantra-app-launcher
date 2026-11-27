package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.gui.desktop.controller.HomeLayoutController
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState

@Composable
fun HomeScreen(
    state: NativeGuiState,
    controller: HomeLayoutController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appResolver = remember { YantraAppResolver(context) }
    val pagerState = rememberPagerState(pageCount = { state.layout.pages.size })
    val density = LocalDensity.current

    LaunchedEffect(pagerState.currentPage) {
        state.currentPage = pagerState.currentPage
    }
    
    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                screenWidth = it.size.width
                screenHeight = it.size.height
            }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = state.layout.pages.getOrNull(pageIndex) ?: return@HorizontalPager
            
            AdaptiveGrid(modifier = Modifier.fillMaxSize()) { gridInfo ->
                HomeGrid(
                    columns = gridInfo.columns,
                    rows = gridInfo.rows,
                    items = page.items,
                    state = state,
                    appResolver = appResolver,
                    onDrop = { pkg, offset ->
                        val dockWidthPx = with(density) { 72.dp.toPx() }
                        if (offset.x >= dockWidthPx && screenWidth > 0 && screenHeight > 0) {
                            val homeX = offset.x - dockWidthPx
                            val col = (homeX / ((screenWidth - dockWidthPx) / gridInfo.columns)).toInt().coerceIn(0, gridInfo.columns - 1)
                            val row = (offset.y / (screenHeight / gridInfo.rows)).toInt().coerceIn(0, gridInfo.rows - 1)
                            
                            controller.moveOnHome(pkg, pageIndex = pageIndex, targetColumn = col, targetRow = row)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
