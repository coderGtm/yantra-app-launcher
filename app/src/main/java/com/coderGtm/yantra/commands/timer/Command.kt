package com.coderGtm.yantra.commands.timer

import android.content.Intent
import android.graphics.Typeface
import android.provider.AlarmClock
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal


class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "timer",
        helpTitle = terminal.activity.getString(R.string.cmd_timer_title),
        description = terminal.activity.getString(R.string.cmd_timer_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ").drop(1)

        if (args.isEmpty()) {
            output(terminal.activity.getString(R.string.no_length_provided_launching_timer_app), terminal.theme.resultTextColor)
            terminal.activity.startActivity(Intent(AlarmClock.ACTION_SET_TIMER))
        }
        else {
            output(terminal.activity.getString(R.string.parsing_length_string), terminal.theme.resultTextColor, Typeface.ITALIC)
            val lengthString = args.first().trim()
            if (!isValidLengthString(lengthString)) {
                output(terminal.activity.getString(R.string.invalid_length_string_provided), terminal.theme.errorTextColor)
                return
            }
            val length = lengthString.toInt()
            var msg = ""

            if (args.size > 1) {
                output(terminal.activity.getString(R.string.parsing_message_string), terminal.theme.resultTextColor, Typeface.ITALIC)
                // get the original message string from the command
                msg = command.substringAfter(lengthString).trim()
            }

            output(terminal.activity.getString(R.string.setting_timer), terminal.theme.resultTextColor)
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, length)
                putExtra(AlarmClock.EXTRA_MESSAGE, msg)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            }
            if (intent.resolveActivity(terminal.activity.packageManager) != null) {
                terminal.activity.startActivity(intent)
                output(terminal.activity.getString(R.string.timer_set_successfully), terminal.theme.successTextColor, Typeface.BOLD)
            }
            else {
                output(terminal.activity.getString(R.string.no_supported_timer_app_found), terminal.theme.errorTextColor)
            }
        }
    }
}