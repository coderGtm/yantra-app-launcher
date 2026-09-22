package com.coderGtm.yantra.ui.screens.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.coderGtm.yantra.gui.desktop.model.AppInfo
import com.coderGtm.yantra.gui.desktop.model.HomeLayout

class NativeGuiState(
    initialLayout: HomeLayout = HomeLayout()
) {
    var layout by mutableStateOf(initialLayout)
    var isDrawerOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var selectedAppForMenu by mutableStateOf<AppInfo?>(null)
    
    var isSettingsOpen by mutableStateOf(false)
    
    var draggingPackageName by mutableStateOf<String?>(null)
    var dragOffset by mutableStateOf(Offset.Zero)
}
