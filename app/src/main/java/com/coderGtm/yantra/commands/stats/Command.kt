package com.coderGtm.yantra.commands.stats

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
        name = "stats",
        helpTitle = "stats",
        description = "Shows Statistics like screen-time, commands used, etc."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'stats' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        if ( checkUsageStatsPermission(terminal) ) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val screenTime = getTotalScreenTime(terminal, startTime, endTime)
                output("Today's Screen Time: $screenTime")
            }
            else {
                output("Screen Time is not supported on this device. Minimum Android 5.1 is required!", terminal.theme.warningTextColor)
                return
            }
        }
        else {
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
        }
    }
}