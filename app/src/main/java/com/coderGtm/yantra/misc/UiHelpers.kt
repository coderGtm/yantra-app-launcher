package com.coderGtm.yantra.misc

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
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
 * Computes the software keyboard height on API < 30, where the `ime()` window inset
 * type does not exist. The keyboard is inferred from the difference between the view's
 * full height and the visible display frame; the navigation bar bottom inset is removed
 * so it is not double-counted (system bars are padded separately). Sub-quarter deltas
 * are treated as noise rather than a keyboard.
 */
fun legacyImeHeightFromFrame(viewHeight: Int, visibleBottom: Int, navBarBottom: Int): Int {
    val candidate = viewHeight - visibleBottom - navBarBottom
    return if (candidate > viewHeight / 4) candidate else 0
}

fun computeLegacyImeHeight(view: View): Int {
    val visible = Rect()
    view.getWindowVisibleDisplayFrame(visible)
    val navBarBottom = ViewCompat.getRootWindowInsets(view)
        ?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: 0
    return legacyImeHeightFromFrame(view.height, visible.bottom, navBarBottom)
}

/**
 * Tracks keyboard height on API < 30 via the visible display frame, reporting it through
 * [onHeightChanged] only when it actually changes. Returns a detach lambda to remove the
 * listener (required to avoid leaking [view] and its activity).
 */
fun installLegacyImeHeightTracking(view: View, onHeightChanged: (Int) -> Unit): () -> Unit {
    var lastHeight = -1
    val listener = ViewTreeObserver.OnGlobalLayoutListener {
        val height = computeLegacyImeHeight(view)
        if (height != lastHeight) {
            lastHeight = height
            onHeightChanged(height)
        }
    }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    return {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}
