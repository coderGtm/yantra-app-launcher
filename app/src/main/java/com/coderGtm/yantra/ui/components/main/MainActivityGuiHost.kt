package com.coderGtm.yantra.ui.components.main

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs

@Composable
internal fun MainActivityGuiHost(
    guiContainer: ViewGroup,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { _ -> guiContainer },
        modifier = modifier,
        update = {
            // The container is persistent; visibility is managed by the Shell platform.
        }
    )
}
