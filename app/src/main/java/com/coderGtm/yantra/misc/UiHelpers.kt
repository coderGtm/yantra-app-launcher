package com.coderGtm.yantra.misc

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun android.view.View.applySystemBarsAndImePadding(includeSides: Boolean = true) {
    val startP = paddingStart
    val topP = paddingTop
    val endP = paddingEnd
    val bottomP = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val bottomInset = maxOf(systemBars.bottom, ime.bottom)
        val left = if (includeSides) systemBars.left else 0
        val right = if (includeSides) systemBars.right else 0
        v.setPaddingRelative(startP + left, topP + systemBars.top, endP + right, bottomP + bottomInset)
        insets
    }
    requestApplyInsets()
}

/**
 * On API < 30 (Android 10 and below) the `ime()` window inset type does not exist, so
 * Compose's [androidx.compose.foundation.layout.imePadding] cannot lift content above the
 * keyboard. Under `adjustNothing` the window's visible display frame also does not change,
 * so the keyboard cannot be detected by measuring the frame. The only reliable mechanism
 * on those versions is `adjustResize`, which physically resizes the window so bottom-anchored
 * content sits above the keyboard. API 30+ keeps insets-based handling instead.
 */
fun requiresLegacyImeResize(sdkInt: Int): Boolean = sdkInt < 30
