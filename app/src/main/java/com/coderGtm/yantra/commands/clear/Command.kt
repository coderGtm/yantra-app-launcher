package com.coderGtm.yantra.commands.clear

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "clear",
        helpTitle = "clear",
        description = "Clears the console"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'clear' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        terminal.binding.terminalOutput.removeAllViews()
    }
}