package com.coderGtm.yantra.terminal

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserManager
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.listeners.launcherAppsCallback
import com.coderGtm.yantra.models.AppBlock

fun getAppsList(terminal: Terminal): ArrayList<AppBlock> {
    val alreadyFetched = terminal.appListFetched
    terminal.appListFetched = false
    if (!alreadyFetched){
        terminal.appList = ArrayList()
    }
    val userManager = terminal.activity.getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    try {
        for (profile in userManager.userProfiles) {
            for (app in launcherApps.getActivityList(null, profile)) {
                val appBlock = AppBlock(
                    app.label.toString(),
                    app.applicationInfo.packageName,
                    profile
                )
                if (!terminal.appList.contains(appBlock)) {
                    terminal.appList.add(appBlock)
                }
            }
        }
        if (!alreadyFetched || terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.A_TO_Z.value) {
            terminal.appList.sortBy { it.appName }
        }
    } catch (e: Exception) {
        terminal.output(terminal.activity.getString(R.string.app_list_fetch_error), terminal.theme.errorTextColor, null)
    }

    terminal.appListFetched = true
    return terminal.appList.distinct() as ArrayList<AppBlock>
}
fun setLauncherAppsListener(terminal: Terminal) {
    val launcherApps = terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    launcherApps.registerCallback(launcherAppsCallback(terminal), null)
}