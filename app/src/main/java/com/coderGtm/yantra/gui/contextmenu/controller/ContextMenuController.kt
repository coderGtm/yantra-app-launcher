package com.coderGtm.yantra.gui.contextmenu.controller

import com.coderGtm.yantra.R
import com.coderGtm.yantra.gui.contextmenu.model.ContextAction
import com.coderGtm.yantra.gui.contextmenu.view.ContextMenuView
import com.coderGtm.yantra.gui.drawer.model.DrawerItem

class ContextMenuController(

    private val view: ContextMenuView

) {

    private var selectedApp: DrawerItem? = null

    fun show(app: DrawerItem) {

        selectedApp = app

        view.setActions(

            listOf(

                ContextAction(
                    id = "open",
                    title = "Open",
                    iconRes = R.drawable.ic_open
                ),

                ContextAction(
                    id = "home",
                    title = "Add to Home",
                    iconRes = R.drawable.ic_home
                ),

                ContextAction(
                    id = "info",
                    title = "App Info",
                    iconRes = R.drawable.ic_info
                ),

                ContextAction(
                    id = "uninstall",
                    title = "Uninstall",
                    iconRes = R.drawable.ic_delete
                )

            )

        )

        view.show()

    }

    fun hide() {

        view.hide()

    }

    fun getSelectedApp(): DrawerItem? {

        return selectedApp

    }

}