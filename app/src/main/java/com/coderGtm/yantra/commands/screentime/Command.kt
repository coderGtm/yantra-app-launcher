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
        helpTitle = "screentime [app-name][-all]",
        description = "Shows Total Screen time for the day! Give app name to get screen time for particular app, or use the '-all' flag to get screentime for all apps used today.\nExample: 'screentime Instagram' or 'screentime -all'"
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
                .setTitle("Permission Required")
                .setMessage("Please grant the 'Usage Access' permission to Yantra Launcher to access the required data for screen time.")
                .setPositiveButton("Grant") { _, _ ->
                    // Navigate the user to the permission settings
                    Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS ).apply {
                        terminal.activity.startActivity( this )
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    output("Missing Usage Access Permission", terminal.theme.errorTextColor)
                }
                .show()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            output("Ouch! Screen Time is not supported on this device. Minimum Android 5.1 is required! :(", terminal.theme.warningTextColor)
            return
        }

        if (args.size == 1) {
            // get total screen time
            val screenTime = getTotalScreenTime(terminal, startTime, endTime, terminal.appList)
            output("Today's Screen Time: $screenTime")
        }
        else if (args[1].trim().lowercase() == "-all") {
            // get screen time for all apps
            val screenTimes = getScreenTime(terminal, startTime, endTime, terminal.appList)
            if (screenTimes.isEmpty()) {
                output("No apps found.", terminal.theme.warningTextColor)
                return
            }
            val appList = terminal.appList
            screenTimes.forEach { st ->
                val appBlock = appList.find { it.packageName == st.key } ?: return@forEach
                output("${appBlock.appName}: ${formatScreenTime(st.value!!.toLong())}")
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
                        output("${appBlock.appName}: ${formatScreenTime(st.value!!.toLong())}")
                    }
                    return
                }
            }
            output("'$name' app not found. Try using 'list apps' to get list of all app names.", terminal.theme.warningTextColor)
        }
    }
}