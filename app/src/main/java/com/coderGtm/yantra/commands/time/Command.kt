package com.coderGtm.yantra.commands.time

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "time",
        helpTitle = terminal.activity.getString(R.string.cmd_time_title),
        description = terminal.activity.getString(R.string.cmd_time_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size >= 2 && args[1] != "utc") {
            output(terminal.activity.getString(R.string.time_invalid_param), terminal.theme.errorTextColor)
            return
        } else if (args.size == 2) {
            output(currentTimeWithOffset(0))
            return
        } else if (args.size >= 3) {
            val time = command.substringAfter(args[1]).trim()

            if (!isValidString(time)) {
                output(terminal.activity.getString(R.string.time_invalid_diff), terminal.theme.errorTextColor)
                return
            }

            val timeInInt = getNormalTimeString(time)
            output(currentTimeWithOffset(timeInInt[0], timeInInt[1]))
            return
        }

        val time = SimpleDateFormat("HH:mm:ss E d/M/y", Locale.getDefault()).format(Date())
        output(time)
    }
}