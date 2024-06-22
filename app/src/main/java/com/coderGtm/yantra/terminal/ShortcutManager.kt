package com.coderGtm.yantra.terminal

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import com.coderGtm.yantra.AppSortMode
import com.coderGtm.yantra.R
import com.coderGtm.yantra.models.ShortcutBlock

fun getShortcutList(terminal: Terminal): ArrayList<ShortcutBlock> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
        // return empty list if the device is not running Android 7.1 or higher
        return ArrayList()
    }
    val alreadyFetched = terminal.shortcutListFetched
    terminal.shortcutListFetched = false
    if (!alreadyFetched) {
        terminal.shortcutList = ArrayList()
    }
    if (terminal.appListFetched) {
        val userManager = terminal.activity.getSystemService(Context.USER_SERVICE) as UserManager
        val launcherApps = terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (!launcherApps.hasShortcutHostPermission()) {
            return terminal.shortcutList
        }
        for (profile in userManager.userProfiles) {
            for (app in terminal.appList) {
                val shortcutQuery = LauncherApps.ShortcutQuery()
                shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                shortcutQuery.setPackage(app.packageName)
                try {
                    launcherApps.getShortcuts(shortcutQuery, profile)?.forEach { shortcut ->
                        val shortcutAppBlock = ShortcutBlock(
                            shortcut.shortLabel.toString(),
                            app.packageName,
                            shortcut.id,
                            profile
                        )
                        if (!terminal.shortcutList.contains(shortcutAppBlock)) {
                            terminal.shortcutList.add(shortcutAppBlock)
                        }
                    }
                    if (!alreadyFetched || terminal.preferenceObject.getInt("appSortMode", AppSortMode.A_TO_Z.value) == AppSortMode.A_TO_Z.value) {
                        terminal.shortcutList.sortBy { it.label }
                    }
                } catch (e: Exception) {
                    // if samnsung secure folder access error occurs, ignore it
                    if (e.message?.contains("User 150 is locked") == true) {
                        continue
                    }
                    terminal.output("${terminal.activity.getString(R.string.shortcut_list_fetch_error)} (${e.message})", terminal.theme.errorTextColor, null)
                }
            }
        }
    }
    terminal.shortcutListFetched = true
    if (terminal.shortcutList.isEmpty()) {
        return terminal.shortcutList
    }
    return terminal.shortcutList.distinct() as ArrayList<ShortcutBlock>
}