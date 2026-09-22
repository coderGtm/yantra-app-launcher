package com.coderGtm.yantra.gui.desktop.engine

import android.content.Context
import android.widget.FrameLayout
import com.coderGtm.yantra.gui.desktop.model.Anchor
import com.coderGtm.yantra.gui.desktop.model.GridPosition

class GridLayoutEngine(
    context: Context
) {

    private val density = context.resources.displayMetrics.density

    // Configuration
    private val columns = 4
    private val cellSize = dp(88)
    private val topPadding = dp(40)
    private val leftPadding = dp(16)

    fun createLayoutParams(
        position: GridPosition,
        anchor: Anchor
    ): FrameLayout.LayoutParams {

        val params = FrameLayout.LayoutParams(
            cellSize,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        params.leftMargin =
            leftPadding + (position.column * cellSize)

        params.topMargin =
            topPadding + (position.row * cellSize)

        when (anchor) {

            Anchor.TOP_LEFT -> {}

            Anchor.TOP_CENTER ->
                params.gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL

            Anchor.TOP_RIGHT ->
                params.gravity = android.view.Gravity.TOP or android.view.Gravity.END

            Anchor.CENTER_LEFT ->
                params.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START

            Anchor.CENTER ->
                params.gravity = android.view.Gravity.CENTER

            Anchor.CENTER_RIGHT ->
                params.gravity = android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.END

            Anchor.BOTTOM_LEFT ->
                params.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.START

            Anchor.BOTTOM_CENTER ->
                params.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL

            Anchor.BOTTOM_RIGHT ->
                params.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
        }

        return params
    }

    private fun dp(value: Int): Int {
        return (value * density).toInt()
    }
}