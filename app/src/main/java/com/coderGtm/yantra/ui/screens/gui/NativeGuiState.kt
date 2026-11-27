package com.coderGtm.yantra.ui.screens.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.coderGtm.yantra.gui.desktop.model.AppInfo
import com.coderGtm.yantra.gui.desktop.model.HomeLayout
import com.coderGtm.yantra.ui.components.gui.interaction.DragSession
import com.coderGtm.yantra.ui.components.gui.interaction.DragSource

class NativeGuiState(
    initialLayout: HomeLayout = HomeLayout()
) {
    var layout by mutableStateOf(initialLayout)
    var isDrawerOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var selectedAppForMenu by mutableStateOf<AppInfo?>(null)
    
    var isSettingsOpen by mutableStateOf(false)
    var currentPage by mutableStateOf(0)
    
    var dragSession by mutableStateOf<DragSession?>(null)

    var draggingPackageName: String?
        get() = dragSession?.itemId
        set(value) {
            dragSession = if (value == null) null else DragSession(itemId = value, source = DragSource.DRAWER)
        }

    var dragOffset: Offset
        get() = dragSession?.currentPosition ?: Offset.Zero
        set(value) {
            dragSession = dragSession?.copy(currentPosition = value, hasMoved = true)
        }
}
