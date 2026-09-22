package com.coderGtm.yantra.gui.desktop.renderer

import android.widget.FrameLayout
import com.coderGtm.yantra.gui.desktop.engine.GridLayoutEngine
import com.coderGtm.yantra.gui.desktop.inflater.IconInflater
import com.coderGtm.yantra.gui.desktop.model.DesktopLayout

class DesktopRenderer(
    private val iconLayer: FrameLayout
) {

    private val gridEngine = GridLayoutEngine(iconLayer.context)

    fun render(layout: DesktopLayout) {

        iconLayer.removeAllViews()

        layout.items.forEach { item ->

            val icon = IconInflater.inflate(
                iconLayer.context,
                item
            )

            icon.layoutParams = gridEngine.createLayoutParams(
                item.position,
                item.anchor
            )

            iconLayer.addView(icon)
        }
    }
}