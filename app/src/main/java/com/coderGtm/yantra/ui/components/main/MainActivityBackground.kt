package com.coderGtm.yantra.ui.components.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.coderGtm.yantra.ui.screens.main.MainActivityUiRefs

@Composable
internal fun MainActivityBackground(uiRefs: MainActivityUiRefs) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(uiRefs.backgroundColorInt)),
    ) {
        uiRefs.backgroundBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}