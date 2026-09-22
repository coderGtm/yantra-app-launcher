package com.coderGtm.yantra.ui.screens.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.core.runtime.LauncherRuntime
import com.coderGtm.yantra.gui.desktop.controller.HomeLayoutController
import com.coderGtm.yantra.gui.desktop.model.HomeLayout
import com.coderGtm.yantra.gui.desktop.repository.HomeRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeGuiSettingsScreen(
    state: NativeGuiState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { HomeRepository(context) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Interface", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = { 
                    LauncherRuntime.switchShell("cli")
                    onClose()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Switch to CLI Shell")
            }

            HorizontalDivider()

            Text("Home Layout", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = { 
                    state.layout = HomeLayout()
                    repository.save(state.layout)
                    onClose()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Home Layout")
            }
            
            // TODO: Add more settings for Grid, Dock, Wallpaper, Appearance
        }
    }
}
