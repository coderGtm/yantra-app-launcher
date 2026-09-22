package com.coderGtm.yantra.gui.drawer.repository

import android.content.Context
import android.content.Intent
import com.coderGtm.yantra.gui.desktop.model.AppInfo

class AppRepository(
    private val context: Context
) {

    fun getInstalledApps(): List<AppInfo> {

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = context.packageManager
            .queryIntentActivities(intent, 0)

        return resolveInfos
            .map {

                AppInfo(

                    packageName = it.activityInfo.packageName,

                    label = it.loadLabel(context.packageManager).toString(),

                    icon = it.loadIcon(context.packageManager)

                )

            }
            .sortedBy {
                it.label.lowercase()
            }

    }

}