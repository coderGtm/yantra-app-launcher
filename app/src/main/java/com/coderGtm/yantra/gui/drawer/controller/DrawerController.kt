package com.coderGtm.yantra.gui.drawer.controller

import android.content.Intent
import android.util.Log
import com.coderGtm.yantra.gui.drawer.adapter.AppDrawerAdapter
import com.coderGtm.yantra.gui.drawer.listeners.DrawerActionListener
import com.coderGtm.yantra.gui.drawer.model.DrawerItem
import com.coderGtm.yantra.gui.drawer.view.DrawerView

class DrawerController(

    private val drawerView: DrawerView,

    private val onLongPress: (DrawerItem) -> Unit

) : DrawerActionListener {

    fun loadApps(
        apps: List<DrawerItem>
    ) {

        drawerView.setAdapter(
            AppDrawerAdapter(
                apps,
                this
            )
        )
    }

    fun open() {
        drawerView.show()
    }

    fun close() {
        drawerView.hide()
    }

    fun toggle() {
        drawerView.toggle()
    }

    fun isOpen(): Boolean {
        return drawerView.isVisible()
    }

    override fun onAppClick(app: DrawerItem) {
        launchApp(app)
    }

    override fun onAppLongClick(app: DrawerItem) {

        onLongPress(app)

    }

    private fun launchApp(app: DrawerItem) {

        val context = drawerView.getContext()

        val intent = context.packageManager
            .getLaunchIntentForPackage(app.packageName)

        if (intent != null) {

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)

        } else {

            Log.e(
                "YANTRA",
                "Unable to launch ${app.packageName}"
            )

        }
    }
}