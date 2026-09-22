package com.coderGtm.yantra.gui.drawer.listeners

import com.coderGtm.yantra.gui.drawer.model.DrawerItem

interface DrawerActionListener {

    fun onAppClick(app: DrawerItem)

    fun onAppLongClick(app: DrawerItem)

}