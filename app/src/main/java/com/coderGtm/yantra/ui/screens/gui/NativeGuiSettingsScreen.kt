package com.coderGtm.yantra.ui.screens.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.SYSTEM_PKG_CLI
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.desktop.controller.HomeLayoutController
import com.coderGtm.yantra.gui.desktop.model.HomeLayout
import com.coderGtm.yantra.gui.desktop.repository.HomeRepository
import com.coderGtm.yantra.ui.components.containers.SettingsGroup
import com.coderGtm.yantra.ui.components.containers.SettingsScreen
import com.coderGtm.yantra.ui.components.settingsItem.ButtonSetting
import com.coderGtm.yantra.ui.components.settingsItem.ToggleSetting
import com.coderGtm.yantra.ui.theme.YantraSettingsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeGuiSettingsScreen(
    state: NativeGuiState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { HomeRepository(context) }
    val controller = remember { HomeLayoutController(repository, state) }

    YantraSettingsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Native GUI Settings") },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            SettingsScreen(modifier = Modifier.padding(padding)) {
                SettingsGroup(title = "Interface") {
                    ButtonSetting(
                        title = "Switch to Command Line Interface",
                        onClick = {
                            LauncherRuntime.switchShell("cli")
                            onClose()
                        }
                    )
                }

                SettingsGroup(title = "Home & Dock Layout") {
                    ToggleSetting(
                        title = "Add CLI button on homescreen",
                        checked = controller.isOnHome(SYSTEM_PKG_CLI),
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                controller.addToHome(SYSTEM_PKG_CLI)
                            } else {
                                controller.removeFromHome(SYSTEM_PKG_CLI)
                            }
                        },
                        description = "Display the CLI shortcut on the home screen"
                    )
                    HorizontalDivider()
                    ToggleSetting(
                        title = "Add CLI button in Dock",
                        checked = controller.isOnDock(SYSTEM_PKG_CLI),
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                controller.addToDock(SYSTEM_PKG_CLI)
                            } else {
                                controller.removeFromDock(SYSTEM_PKG_CLI)
                            }
                        },
                        description = "Display the CLI shortcut in the left dock"
                    )
                }

                SettingsGroup(title = "General Layout") {
                    ButtonSetting(
                        title = "Reset Home Layout",
                        onClick = {
                            state.layout = HomeLayout()
                            repository.save(state.layout)
                            onClose()
                        }
                    )
                }
            }
        }
    }
}
