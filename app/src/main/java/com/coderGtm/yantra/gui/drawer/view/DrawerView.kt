package com.coderGtm.yantra.gui.drawer.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coderGtm.yantra.R

class DrawerView(

    private val container: ViewGroup

) {

    private val root: View =
        LayoutInflater.from(container.context)
            .inflate(
                R.layout.view_app_drawer,
                container,
                false
            )

    private val recycler =
        root.findViewById<RecyclerView>(R.id.appRecycler)

    init {

        container.addView(root)

        root.visibility = View.GONE

        recycler.layoutManager =
            GridLayoutManager(
                container.context,
                4
            )

        recycler.setHasFixedSize(true)
    }

    fun getRecyclerView(): RecyclerView = recycler

    fun scrollToTop() {
        recycler.scrollToPosition(0)
    }



    fun show() {

        if (root.visibility == View.VISIBLE)
            return

        root.post {

            root.translationY = root.height.toFloat()

            root.visibility = View.VISIBLE

            root.animate()

                .translationY(0f)

                .setDuration(220)

                .setInterpolator(
                    DecelerateInterpolator()
                )

                .start()

        }

    }

    fun hide() {

        if (root.visibility != View.VISIBLE)
            return

        root.animate()

            .translationY(root.height.toFloat())

            .setDuration(220)

            .setInterpolator(
                DecelerateInterpolator()
            )

            .withEndAction {

                root.visibility = View.GONE

            }

            .start()

    }

    fun toggle() {

        if (isVisible()) {

            hide()

        } else {

            show()

        }

    }

    fun isVisible(): Boolean {

        return root.visibility == View.VISIBLE

    }

    fun getRoot(): FrameLayout {

        return root as FrameLayout

    }

    fun setAdapter(
        adapter: RecyclerView.Adapter<*>
    ) {

        recycler.adapter = adapter

    }

    fun getContext(): Context {
        return container.context
    }

}