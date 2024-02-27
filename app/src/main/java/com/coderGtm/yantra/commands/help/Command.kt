package com.coderGtm.yantra.commands.help

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "help",
        helpTitle = terminal.activity.getString(R.string.cmd_help_title),
        description = terminal.activity.getString(R.string.cmd_help_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        when (args.size) {
            1 -> {
                output(terminal.activity.getString(R.string.yantra_launcher_help),terminal.theme.successTextColor, Typeface.BOLD_ITALIC)
                output("-------------------------",terminal.theme.resultTextColor)
                for (commandClass in terminal.commands.values) {
                        val cmdMetadata = commandClass.getDeclaredConstructor(Terminal::class.java)
                            .newInstance(terminal).metadata
                    output(cmdMetadata.helpTitle ,terminal.theme.warningTextColor, Typeface.BOLD)
                    output(cmdMetadata.description ,terminal.theme.resultTextColor)
                    output("-------------------------")
                }
                output("-------------------------",terminal.theme.resultTextColor)
                output(terminal.activity.getString(R.string.enjoy_with_face),terminal.theme.successTextColor, Typeface.BOLD_ITALIC)
            }
            2 -> {
                val cmd = args[1].trim().lowercase()
                val commandClass = terminal.commands[cmd]
                if (commandClass != null) {
                    val cmdMetadata =
                        commandClass.getDeclaredConstructor(Terminal::class.java)
                            .newInstance(terminal).metadata
                    output(cmdMetadata.helpTitle, terminal.theme.warningTextColor, Typeface.BOLD)
                    output(cmdMetadata.description, terminal.theme.resultTextColor)
                    output("-------------------------")
                    return
                }
                output(terminal.activity.getString(R.string.cmd_not_found), terminal.theme.errorTextColor)
            }
            else -> {
                output(terminal.activity.getString(R.string.invalid_help_cmd), terminal.theme.errorTextColor)
            }
        }
    }
}