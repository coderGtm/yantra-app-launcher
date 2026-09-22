package com.coderGtm.yantra.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coderGtm.yantra.ui.components.main.MainActivityBackground
import com.coderGtm.yantra.ui.components.main.MainActivityGuiHost
import com.coderGtm.yantra.ui.components.main.MainActivityOverlayControls
import com.coderGtm.yantra.ui.components.main.MainActivityTerminalPane
import com.coderGtm.yantra.ui.screens.gui.NativeGuiScreen

@Composable
internal fun MainActivityScreen(uiRefs: MainActivityUiRefs) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
    ) {
        MainActivityBackground(uiRefs = uiRefs)

        if (uiRefs.activeShellName == "cli") {
            MainActivityTerminalPane(
                uiRefs = uiRefs,
                modifier = Modifier.fillMaxSize(),
            )
            
            MainActivityOverlayControls(
                uiRefs = uiRefs,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (uiRefs.activeShellName == "gui") {
            NativeGuiScreen(uiRefs = uiRefs)
        } else {
            uiRefs.guiContainer?.let { container ->
                MainActivityGuiHost(
                    guiContainer = container,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

