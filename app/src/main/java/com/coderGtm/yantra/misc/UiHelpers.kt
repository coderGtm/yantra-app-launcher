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
