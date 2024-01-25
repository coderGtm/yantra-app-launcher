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
        helpTitle = "time <utc> [GMT]",
        description = "Shows current local Date and Time or current GMT Date and time witch you provide"
    )

    override fun execute(command: String) {
        val args = command.split(" ")

        if (args.size >= 2 && args[1] != "utc") {
            output("Invalid second argument, please provide a correct parameter", terminal.theme.errorTextColor)
            return
        } else if (args.size == 2) {
            output(currentTimeWithOffset(0))
            return
        } else if (args.size >= 3) {
            val time = command.substringAfter(args[1]).trim()

            if (!isCorrectString(time)) {
                output("Invalid GMT, please provide a correct parameter", terminal.theme.errorTextColor)
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