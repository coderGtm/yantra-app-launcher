package com.coderGtm.yantra.gui.shell

import com.coderGtm.yantra.core.shell.Shell
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs

class DesktopShell(
    private val uiRefs: MainActivityUiRefs
) : Shell {

    override fun initialize() {
        // Compose handles initialization via MainActivityScreen and NativeGuiScreen
    }

    override fun resume() {
        // Logic when the GUI shell is resumed
    }

    override fun pause() {
        // Logic when the GUI shell is paused
        uiRefs.nativeGuiState?.isDrawerOpen = false
    }

    override fun destroy() {
        // Cleanup logic
    }

    override fun onBackPressed(): Boolean {
        val state = uiRefs.nativeGuiState ?: return false
        
        if (state.isDrawerOpen) {
            state.isDrawerOpen = false
            return true
        }
        
        // Return true to indicate we've handled it (by staying in the GUI)
        return true
    }

    fun openDrawer() {
        uiRefs.nativeGuiState?.isDrawerOpen = true
    }

    fun closeDrawer() {
        uiRefs.nativeGuiState?.isDrawerOpen = false
    }

    fun toggleDrawer() {
        uiRefs.nativeGuiState?.let {
            it.isDrawerOpen = !it.isDrawerOpen
        }
    }
}
