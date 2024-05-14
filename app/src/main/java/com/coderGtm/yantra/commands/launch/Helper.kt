package com.coderGtm.yantra.commands.launch

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import com.coderGtm.yantra.R
import com.coderGtm.yantra.models.AppBlock
import com.coderGtm.yantra.models.ShortcutBlock

fun launchApp(command: Command, app: AppBlock) {
    val launcher = command.terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val component = launcher.getActivityList(app.packageName, app.user).first().componentName
    try {
        launcher.startMainActivity(component, app.user, null, null)
        command.output(command.terminal.activity.getString(R.string.rendering_display_to, Build.MANUFACTURER, Build.MODEL))
    } catch (e: Exception) {
        command.output(command.terminal.activity.getString(R.string.failed_to_launch_app), command.terminal.theme.errorTextColor)
    }
}

fun launchShortcut(command: Command, shortcut: ShortcutBlock) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
        command.output(command.terminal.activity.getString(R.string.shortcut_not_supported), command.terminal.theme.errorTextColor)
        return
    }
    val launcher = command.terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    try {
        launcher.startShortcut(shortcut.packageName, shortcut.id, null, null, shortcut.user)
        command.output(command.terminal.activity.getString(R.string.rendering_display_to, Build.MANUFACTURER, Build.MODEL))
    } catch (e: Exception) {
        command.output(command.terminal.activity.getString(R.string.failed_to_launch_app), command.terminal.theme.errorTextColor)
    }
}

fun isDefaultUser(user: UserHandle, command: Command): Boolean {
    val userManager = command.terminal.activity.getSystemService(Context.USER_SERVICE) as UserManager
    return user == userManager.userProfiles[0]
}