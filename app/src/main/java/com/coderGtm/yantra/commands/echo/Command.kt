package com.coderGtm.yantra.commands.echo

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "echo",
        helpTitle = "echo [-mode] <text>",
        description = "Prints specified text to the terminal with the given mode. Here mode is an optional argument (e: Error text, s: Success text, w: Warning text) representing the nature of the text output. If mode is not specified, the text is printed (normal)ly\nExamples:\n'echo -e An error occurred.'\n'echo Hello, World'"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output("Invalid Syntax. See 'help echo' for usage info.", terminal.theme.errorTextColor)
            return
        }
        val inputString = command.removePrefix(args[0]).trim()
        if (inputString.isEmpty()) {
            output("Invalid Syntax. See 'help echo' for usage info.", terminal.theme.errorTextColor)
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
                output("Invalid mode Provided. Use 'e', 'w' or 's'.", terminal.theme.errorTextColor)
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