package com.coderGtm.yantra.misc

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun android.view.View.applySystemBarsPadding(includeSides: Boolean = true) {
    val startP = paddingStart
    val topP = paddingTop
    val endP = paddingEnd
    val bottomP = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val left = if (includeSides) sb.left else 0
        val right = if (includeSides) sb.right else 0
        v.setPaddingRelative(startP + left, topP + sb.top, endP + right, bottomP + sb.bottom)
        insets
    }
    requestApplyInsets()
}