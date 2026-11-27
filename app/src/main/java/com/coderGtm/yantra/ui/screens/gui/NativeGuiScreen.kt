package com.coderGtm.yantra.ui.screens.gui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.desktop.controller.HomeLayoutController
import com.coderGtm.yantra.gui.desktop.repository.HomeRepository
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.components.gui.AppDrawer
import com.coderGtm.yantra.ui.components.gui.HomeScreen
import com.coderGtm.yantra.ui.components.gui.AppContextMenu
import com.coderGtm.yantra.ui.components.gui.HomeItemView
import com.coderGtm.yantra.ui.components.gui.LeftDock
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs
import kotlin.math.roundToInt

@Composable
internal fun NativeGuiScreen(uiRefs: MainActivityUiRefs) {
    val state = uiRefs.nativeGuiState ?: return
    val context = LocalContext.current
    val repository = remember { HomeRepository(context) }
    val controller = remember { HomeLayoutController(repository, state) }
    val appResolver = remember { YantraAppResolver(context) }
    val density = LocalDensity.current
    
    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                screenWidth = it.size.width
                screenHeight = it.size.height
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            LeftDock(
                state = state,
                modifier = Modifier.fillMaxHeight()
            )
            
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                HomeScreen(state = state, controller = controller, modifier = Modifier.fillMaxSize())
            }
        }

        AnimatedVisibility(
            visible = state.isDrawerOpen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            AppDrawer(
                state = state,
                onClose = { state.isDrawerOpen = false },
                onDrop = { pkg, offset ->
                    val dockWidthPx = with(density) { 72.dp.toPx() }
                    
                    if (offset.x < dockWidthPx) {
                        controller.addToDock(pkg)
                    } else {
                        // Adaptive grid logic mirror
                        val widthDp = with(density) { (screenWidth - dockWidthPx).toDp() }
                        val heightDp = with(density) { screenHeight.toDp() }
                        
                        val cols = when {
                            widthDp < 400.dp -> 4
                            widthDp < 600.dp -> 5
                            else -> 6
                        }
                        
                        val rows = when {
                            heightDp < 600.dp -> 4
                            heightDp < 800.dp -> 5
                            heightDp < 1000.dp -> 6
                            else -> 7
                        }

                        val homeX = offset.x - dockWidthPx
                        val col = (homeX / ((screenWidth - dockWidthPx) / cols)).toInt().coerceIn(0, cols - 1)
                        val row = (offset.y / (screenHeight / rows)).toInt().coerceIn(0, rows - 1)
                        controller.addOrMoveToHome(pkg, pageIndex = state.currentPage, column = col, row = row)
                    }
                    state.isDrawerOpen = false
                }
            )
        }
        
        state.selectedAppForMenu?.let { app ->
            val isSystemApp = app.packageName == SYSTEM_PKG_CLI || app.packageName == SYSTEM_PKG_SETTINGS

            AppContextMenu(
                app = app,
                isOnHome = controller.isOnHome(app.packageName),
                isOnDock = controller.isOnDock(app.packageName),
                onDismiss = { state.selectedAppForMenu = null },
                onAction = { action ->
                    when (action) {
                        "open" -> {
                            when (app.packageName) {
                                SYSTEM_PKG_CLI -> LauncherRuntime.switchShell("cli")
                                SYSTEM_PKG_SETTINGS -> state.isSettingsOpen = true
                                else -> appResolver.launch(app.packageName)
                            }
                        }
                        "info" -> if (!isSystemApp) appResolver.info(app.packageName)
                        "uninstall" -> if (!isSystemApp) appResolver.uninstall(app.packageName)
                        "add_to_home" -> controller.addToHome(app.packageName)
                        "remove_from_home" -> controller.removeFromHome(app.packageName)
                        "add_to_dock" -> controller.addToDock(app.packageName)
                        "remove_from_dock" -> controller.removeFromDock(app.packageName)
                    }
                }
            )
        }

        if (state.isSettingsOpen) {
            NativeGuiSettingsScreen(
                state = state,
                onClose = { state.isSettingsOpen = false }
            )
        }

        state.draggingPackageName?.let { pkg ->
            HomeItemView(
                packageName = pkg,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            state.dragOffset.x.roundToInt(),
                            state.dragOffset.y.roundToInt()
                        )
                    }
            )
        }
    }
}
