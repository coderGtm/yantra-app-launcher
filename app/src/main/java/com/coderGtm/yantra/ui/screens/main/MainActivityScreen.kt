package com.coderGtm.yantra.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.coderGtm.yantra.ui.components.main.MainActivityBackground
import com.coderGtm.yantra.ui.components.main.MainActivityOverlayControls
import com.coderGtm.yantra.ui.components.main.MainActivityTerminalPane

@Composable
internal fun MainActivityScreen(uiRefs: MainActivityUiRefs) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
    ) {
        MainActivityBackground(uiRefs = uiRefs)
        MainActivityTerminalPane(
            uiRefs = uiRefs,
            modifier = Modifier.fillMaxSize(),
        )
        MainActivityOverlayControls(
            uiRefs = uiRefs,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

