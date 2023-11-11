package com.coderGtm.yantra.commands.exit

import android.graphics.Typeface
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "exit",
        helpTitle = "exit",
        description = "Exit Launcher. Note: Launcher will restart if set as default Launcher"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output("'exit' command does not take any parameters.", terminal.theme.errorTextColor)
            return
        }
        output("Exiting app...", terminal.theme.resultTextColor, Typeface.ITALIC)
        terminal.activity.finish()
    }
}