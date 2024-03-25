package com.coderGtm.yantra.commands.screentime

import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "screentime",
        helpTitle = terminal.activity.getString(R.string.cmd_screentime_title),
        description = terminal.activity.getString(R.string.cmd_screentime_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        if (!checkUsageStatsPermission(terminal)) {
            MaterialAlertDialogBuilder(terminal.activity, R.style.Theme_AlertDialog)
                .setTitle(terminal.activity.getString(R.string.permission_required))
                .setMessage(terminal.activity.getString(R.string.screentime_permission))
                .setPositiveButton(terminal.activity.getString(R.string.grant)) { _, _ ->
                    // Navigate the user to the permission settings
                    Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS ).apply {
                        terminal.activity.startActivity( this )
                    }
                }
                .setNegativeButton(terminal.activity.getString(R.string.cancel)) { _, _ ->
                    output(terminal.activity.getString(R.string.missing_usage_access_permission), terminal.theme.errorTextColor)
                }
                .show()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            output(terminal.activity.getString(R.string.screentime_not_supported), terminal.theme.warningTextColor)
            return
        }

        if (args.size == 1) {
            // get total screen time
            val screenTime = getTotalScreenTime(terminal, startTime, endTime, terminal.appList)
            output(terminal.activity.getString(R.string.todays_screen_time, screenTime))
        }
        else if (args[1].trim().lowercase() == "-all") {
            // get screen time for all apps
            val screenTimes = getScreenTime(terminal, startTime, endTime, terminal.appList)
            if (screenTimes.isEmpty()) {
                output(terminal.activity.getString(R.string.no_apps_found), terminal.theme.warningTextColor)
                return
            }
            screenTimes.forEach { st ->
                val appBlock = terminal.appList.find { it.packageName == st.key } ?: return@forEach
                output("${appBlock.appName}: ${formatScreenTime(st.value!!.toLong(), terminal.activity)}")
            }
        }
        else {
            // get by app name
            val name = command.removePrefix(args[0]).trim().lowercase()
            for (app in terminal.appList) {
                if (app.appName.lowercase() == name) {
                    val screenTimes = getScreenTime(terminal, startTime, endTime, arrayListOf(app))
                    if (screenTimes.isEmpty()) {
                        val appBlock = terminal.appList.find { it.appName.lowercase() == name } ?: return
                        output("${appBlock.appName}: 0s")
                        return
                    }
                    screenTimes.forEach { st ->
                        val appBlock = terminal.appList.find { it.packageName == st.key } ?: return@forEach
                        output("${appBlock.appName}: ${formatScreenTime(st.value!!.toLong(), terminal.activity)}")
                    }
                    return
                }
            }
            output(terminal.activity.getString(R.string.app_not_found, name), terminal.theme.warningTextColor)
        }
    }
}