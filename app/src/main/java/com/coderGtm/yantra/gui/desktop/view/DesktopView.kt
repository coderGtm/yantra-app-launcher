package com.coderGtm.yantra.gui.desktop.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.coderGtm.yantra.R
import com.coderGtm.yantra.gui.desktop.controller.DesktopController
import com.coderGtm.yantra.gui.desktop.renderer.DesktopRenderer
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DesktopView(
    private val container: ViewGroup
) {

    private lateinit var root: View

    var onDrawerButtonClick: (() -> Unit)? = null

    var onCliButtonClick: (() -> Unit)? = null

    fun show() {

        if (!::root.isInitialized) {

            root = LayoutInflater.from(container.context)
                .inflate(
                    R.layout.desktop_shell,
                    container,
                    false
                )

            container.addView(root)

            val drawerButton =
                root.findViewById<FloatingActionButton>(
                    R.id.openDrawerButton
                )

            drawerButton.setOnClickListener {
                onDrawerButtonClick?.invoke()
            }

            val cliButton =
                root.findViewById<ExtendedFloatingActionButton>(
                    R.id.cliButton
                )

            cliButton.setOnClickListener {
                onCliButtonClick?.invoke()
            }

            val iconLayer =
                root.findViewById<FrameLayout>(
                    R.id.iconLayer
                )

            val renderer =
                DesktopRenderer(iconLayer)

            val controller =
                DesktopController(
                    container.context
                )

            renderer.render(
                controller.getLayout()
            )
        }

        root.visibility = View.VISIBLE
        container.visibility = View.VISIBLE
    }

    fun hide() {

        if (::root.isInitialized) {
            root.visibility = View.GONE
        }
    }

    fun getRoot(): View {
        return root
    }
}