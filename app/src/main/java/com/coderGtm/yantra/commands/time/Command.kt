package com.coderGtm.yantra.commands.time

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "time",
        helpTitle = "time [utc] [GMT]",
        description = "Shows current local Date and Time. Use the utc arg to get UTC time. An optional time difference parameter can add or subtract that from UTC time. Example:\ntime\ntime utc\ntime utc +5:30"
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size >= 2 && args[1] != "utc") {
            output("Invalid parameter provided. See 'help time' for usage info.", terminal.theme.errorTextColor)
            return
        } else if (args.size == 2) {
            output(currentTimeWithOffset(0))
            return
        } else if (args.size >= 3) {
            val time = command.substringAfter(args[1]).trim()

            if (!isValidString(time)) {
                output("Invalid time difference provided. Please provide a correct parameter. Refer 'help time' for usage info.", terminal.theme.errorTextColor)
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