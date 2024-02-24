package com.coderGtm.yantra.commands.exit

import android.graphics.Typeface
import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "exit",
        helpTitle = "exit",
        description = terminal.activity.getString(R.string.cmd_exit_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.exit_no_args), terminal.theme.errorTextColor)
            return
        }
        output(terminal.activity.getString(R.string.exiting_app), terminal.theme.resultTextColor, Typeface.ITALIC)
        terminal.activity.finish()
    }
}