package com.coderGtm.yantra.gui.drawer.repository

import com.coderGtm.yantra.gui.drawer.model.DrawerItem

class DrawerRepository(

    private val appRepository: AppRepository

) {

    fun loadApps(): List<DrawerItem> {

        return appRepository

            .getInstalledApps()

            .map {

                DrawerItem(

                    packageName = it.packageName,

                    label = it.label,

                    icon = it.icon

                )

            }

    }

}