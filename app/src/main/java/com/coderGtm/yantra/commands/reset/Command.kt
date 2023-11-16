package com.coderGtm.yantra.commands.reset

import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "reset",
        helpTitle = "reset",
        description = "Restarts the console (Launcher) completely."
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'reset' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        terminal.activity.recreate()
    }
}