package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.SYSTEM_PKG_SETTINGS
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.desktop.model.AppInfo
import com.coderGtm.yantra.gui.web.YantraAppResolver
import com.coderGtm.yantra.ui.screens.gui.NativeGuiState

@Composable
fun LeftDock(
    state: NativeGuiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appResolver = remember { YantraAppResolver(context) }
    
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(72.dp),
        color = Color.Black.copy(alpha = 0.4f),
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (pkg in state.layout.dockPackages) {
                HomeItemView(
                    packageName = pkg,
                    onClick = { 
                        if (pkg == SYSTEM_PKG_CLI) {
                            LauncherRuntime.switchShell("cli")
                        } else if (pkg == SYSTEM_PKG_SETTINGS) {
                            state.isSettingsOpen = true
                        } else {
                            appResolver.launch(pkg)
                        }
                    },
                    onLongClick = {
                        runCatching {
                            val pm = context.packageManager
                            if (pkg == SYSTEM_PKG_CLI || pkg == SYSTEM_PKG_SETTINGS) {
                                state.selectedAppForMenu = AppInfo(
                                    packageName = pkg,
                                    label = if (pkg == SYSTEM_PKG_CLI) "CLI" else "Settings",
                                    icon = null
                                )
                            } else {
                                val info = pm.getApplicationInfo(pkg, 0)
                                state.selectedAppForMenu = AppInfo(
                                    packageName = pkg,
                                    label = pm.getApplicationLabel(info).toString(),
                                    icon = pm.getApplicationIcon(info)
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Drawer Button
            IconButton(
                onClick = { state.isDrawerOpen = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Apps",
                    tint = Color.White
                )
            }
        }
    }
}
