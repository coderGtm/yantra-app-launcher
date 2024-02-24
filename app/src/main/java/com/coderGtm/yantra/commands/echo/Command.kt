package com.coderGtm.yantra.commands.echo

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "echo",
        helpTitle = terminal.activity.getString(R.string.cmd_echo_title),
        description = terminal.activity.getString(R.string.cmd_echo_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.echo_invalid_syntax), terminal.theme.errorTextColor)
            return
        }
        val inputString = command.removePrefix(args[0]).trim()
        if (inputString.isEmpty()) {
            output(terminal.activity.getString(R.string.echo_invalid_syntax), terminal.theme.errorTextColor)
            return
        }
        else if (inputString.length <= 2) {
            // no mode specified. output in normal text
            output(inputString)
            return
        }
        else if (inputString[0] == '-' && inputString[1].isLetter() && inputString[2] == ' ') {
            // mode is specified. output in checked mode
            val mode = if (inputString[1] == 'e') {
                terminal.theme.errorTextColor
            } else if (inputString[1] == 'w') {
                terminal.theme.warningTextColor
            } else if (inputString[1] == 's') {
                terminal.theme.successTextColor
            } else {
                output(terminal.activity.getString(R.string.echo_invalid_mode), terminal.theme.errorTextColor)
                return
            }
            output(inputString.removePrefix(inputString.substring(0,2)).trim(), mode)
        }
        else {
            // mode is not specified. output in normal text
            output(inputString)
            return
        }
    }
}