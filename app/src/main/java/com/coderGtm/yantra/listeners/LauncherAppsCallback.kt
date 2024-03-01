package com.coderGtm.yantra.listeners

import android.content.pm.LauncherApps
import android.graphics.Typeface
import android.os.UserHandle
import com.coderGtm.yantra.R
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.terminal.getAppsList

fun launcherAppsCallback(terminal: Terminal): LauncherApps.Callback {
    return object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) {
            val indexToRemove = terminal.appList.indexOfFirst {
                it.packageName == packageName
            }
            if (indexToRemove == -1) return
            terminal.appList.removeAt(indexToRemove)
            terminal.output(terminal.activity.getString(R.string.package_removed, packageName), terminal.theme.errorTextColor, Typeface.BOLD)
        }

        override fun onPackageAdded(p0: String?, p1: UserHandle?) {
            getAppsList(terminal)
        }

        override fun onPackageChanged(p0: String?, p1: UserHandle?) {
            getAppsList(terminal)
        }

        override fun onPackagesAvailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            getAppsList(terminal)
        }

        override fun onPackagesUnavailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            getAppsList(terminal)
        }
    }
}