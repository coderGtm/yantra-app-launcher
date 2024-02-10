package com.coderGtm.yantra.commands.alarm

import android.content.Intent
import android.graphics.Typeface
import android.provider.AlarmClock
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal


class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "alarm",
        helpTitle = "alarm [time] [message]",
        description = "Sets alarm using a supported Alarm Clock on your device. Use without args to open the list of alarms on your device default app. The time must be in 24 hr format. It can optionally be followed by a message string to display in alarm.\nExamples:\n'alarm 14:30' sets alarm for 2:30 pm\n'alarm 0:15 Wish good night to ***' sets alarm for 12:15 am with a message to display."
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output("No time provided. Launching alarm app.", terminal.theme.resultTextColor)
            terminal.activity.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
        }
        else {
            output(":: Parsing time string...", terminal.theme.resultTextColor, Typeface.ITALIC)
            val timeString = args.first().trim()
            if (!isValidTimeString(timeString)) {
                output("Invalid time string provided. It must confront to hh:mm format and must be in 24 hr representation.", terminal.theme.errorTextColor)
                return
            }
            val hr = timeString.split(":")[0]
            val min = timeString.split(":")[1]
            var msg = ""

            if (args.size > 1) {
                output(":: Parsing message string...", terminal.theme.resultTextColor, Typeface.ITALIC)
                // get the original message string from the command
                msg = command.substringAfter(timeString).trim()
            }

            output("=> Setting alarm...", terminal.theme.resultTextColor)
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hr.toInt())
                putExtra(AlarmClock.EXTRA_MINUTES, min.toInt())
                putExtra(AlarmClock.EXTRA_MESSAGE, msg)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                putExtra(AlarmClock.EXTRA_VIBRATE, false)
            }
            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                terminal.activity.startActivity(intent)
                output("Alarm set successfully.", terminal.theme.successTextColor, Typeface.BOLD)
            }
            else {
                output("No supported alarm app found on your device.", terminal.theme.errorTextColor)
            }
        }
    }
}