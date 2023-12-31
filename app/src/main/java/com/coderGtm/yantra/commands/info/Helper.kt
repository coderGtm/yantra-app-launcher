package com.coderGtm.yantra.commands.info

import android.content.Context
import android.content.pm.LauncherApps
import android.os.UserHandle
import android.os.UserManager
import com.coderGtm.yantra.models.AppBlock

fun launchAppInfo(command: Command, app: AppBlock) {
    val launcher = command.terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val component = launcher.getActivityList(app.packageName, app.user).first().componentName
    try {
        launcher.startAppDetailsActivity(component, app.user, null, null)
    } catch (e: Exception) {
        command.output("Failed to open app info :(", command.terminal.theme.errorTextColor)
    }
}
fun isDefaultUser(user: UserHandle, command: Command): Boolean {
    val userManager = command.terminal.activity.getSystemService(Context.USER_SERVICE) as UserManager
    return user == userManager.userProfiles[0]
}