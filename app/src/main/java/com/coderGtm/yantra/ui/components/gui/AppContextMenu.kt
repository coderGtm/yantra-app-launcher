package com.coderGtm.yantra.ui.components.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coderGtm.yantra.gui.desktop.model.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppInfo,
    isOnHome: Boolean,
    isOnDock: Boolean,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ContextMenuItem(label = "Open", onClick = { onAction("open"); onDismiss() })
            ContextMenuItem(label = "App Info", onClick = { onAction("info"); onDismiss() })
            ContextMenuItem(label = "Uninstall", onClick = { onAction("uninstall"); onDismiss() })
            
            if (isOnHome) {
                ContextMenuItem(label = "Remove from Home", onClick = { onAction("remove_from_home"); onDismiss() })
            } else {
                ContextMenuItem(label = "Add to Home", onClick = { onAction("add_to_home"); onDismiss() })
            }

            if (isOnDock) {
                ContextMenuItem(label = "Remove from Dock", onClick = { onAction("remove_from_dock"); onDismiss() })
            } else {
                ContextMenuItem(label = "Add to Dock", onClick = { onAction("add_to_dock"); onDismiss() })
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ContextMenuItem(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(16.dp)
        )
    }
}
