package com.coderGtm.yantra.commands.infof

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import com.coderGtm.yantra.models.AppBlock

fun launchAppInfo(command: Command, app: AppBlock) {
    val launcher = command.terminal.activity.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val component = launcher.getActivityList(app.packageName, app.user).first().componentName
    try {
        launcher.startAppDetailsActivity(component, app.user, null, null)
        command.output(":: Rendering Display to ${Build.MANUFACTURER} ${Build.MODEL}...")
    } catch (e: Exception) {
        command.output("Failed to launch app info page :(", command.terminal.theme.errorTextColor)
    }
}