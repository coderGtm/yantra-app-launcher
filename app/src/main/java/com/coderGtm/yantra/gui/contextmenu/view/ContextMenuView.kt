package com.coderGtm.yantra.gui.contextmenu.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.gui.contextmenu.adapter.ContextMenuAdapter
import com.coderGtm.yantra.gui.contextmenu.model.ContextAction

class ContextMenuView(
    context: Context
) : FrameLayout(context) {

    private lateinit var actionList: RecyclerView

    init {

        LayoutInflater.from(context)
            .inflate(
                R.layout.view_context_menu,
                this,
                true
            )

        actionList = findViewById(R.id.actionList)

        actionList.layoutManager =
            LinearLayoutManager(context)

        visibility = View.GONE
    }

    fun show() {

        visibility = View.VISIBLE

        alpha = 0f

        animate()
            .alpha(1f)
            .setDuration(180)
            .start()
    }

    fun hide() {

        animate()
            .alpha(0f)
            .setDuration(180)
            .withEndAction {

                visibility = View.GONE

            }
            .start()
    }

    fun setActions(
        actions: List<ContextAction>
    ) {

        actionList.adapter =
            ContextMenuAdapter(actions) { action ->

                // TODO: Handle action clicks in Sprint 10.7.7

            }

    }

    fun getRecyclerView(): RecyclerView {

        return actionList

    }

}