package com.coderGtm.yantra.commands.alarm

import android.content.Intent
import android.graphics.Typeface
import android.provider.AlarmClock
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal


class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "alarm",
        helpTitle = terminal.activity.getString(R.string.cmd_alarm_title),
        description = terminal.activity.getString(R.string.cmd_alarm_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output(terminal.activity.getString(R.string.no_time_provided_launch_app), terminal.theme.resultTextColor)
            terminal.activity.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS))
        }
        else {
            output(terminal.activity.getString(R.string.parsing_time_string), terminal.theme.resultTextColor, Typeface.ITALIC)
            val timeString = args.first().trim()
            if (!isValidTimeString(timeString)) {
                output(terminal.activity.getString(R.string.invalid_time_string), terminal.theme.errorTextColor)
                return
            }
            val hr = timeString.split(":")[0]
            val min = timeString.split(":")[1]
            var msg = ""

            if (args.size > 1) {
                output(terminal.activity.getString(R.string.parsing_message_string), terminal.theme.resultTextColor, Typeface.ITALIC)
                // get the original message string from the command
                msg = command.substringAfter(timeString).trim()
            }

            output(terminal.activity.getString(R.string.setting_alarm), terminal.theme.resultTextColor)
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hr.toInt())
                putExtra(AlarmClock.EXTRA_MINUTES, min.toInt())
                putExtra(AlarmClock.EXTRA_MESSAGE, msg)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                putExtra(AlarmClock.EXTRA_VIBRATE, false)
            }
            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                terminal.activity.startActivity(intent)
                output(terminal.activity.getString(R.string.alarm_set), terminal.theme.successTextColor, Typeface.BOLD)
            }
            else {
                output(terminal.activity.getString(R.string.no_alarm_app), terminal.theme.errorTextColor)
            }
        }
    }
}