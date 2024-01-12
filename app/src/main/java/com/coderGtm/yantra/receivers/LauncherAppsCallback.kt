package com.coderGtm.yantra.receivers

import android.content.pm.LauncherApps
import android.os.UserHandle
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