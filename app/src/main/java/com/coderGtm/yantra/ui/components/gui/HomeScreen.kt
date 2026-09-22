package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.coderGtm.yantra.gui.desktop.model.AppInfo
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState

@Composable
fun HomeScreen(
    state: NativeGuiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appResolver = remember { YantraAppResolver(context) }
    val pagerState = rememberPagerState(pageCount = { state.layout.pages.size })
    
    Box(modifier = modifier.fillMaxSize()) {
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
                    appResolver = appResolver,
                    onItemLongClick = { packageName ->
                        runCatching {
                            val context = context
                            val pm = context.packageManager
                            val info = pm.getApplicationInfo(packageName, 0)
                            state.selectedAppForMenu = AppInfo(
                                packageName = packageName,
                                label = pm.getApplicationLabel(info).toString(),
                                icon = pm.getApplicationIcon(info)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
