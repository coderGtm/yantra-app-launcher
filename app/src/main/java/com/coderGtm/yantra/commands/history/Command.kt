package com.coderGtm.yantra.commands.history

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "history",
        helpTitle = "history",
        description = "Shows all executed command"
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmd_takes_no_params, metadata.name), terminal.theme.errorTextColor)
            return
        }

        output("Commands History:", terminal.theme.resultTextColor)
        output("----------------", terminal.theme.resultTextColor)

        for (cmd in terminal.cmdHistory) {
            output("|-- $cmd", terminal.theme.resultTextColor)
        }

        output("----------------", terminal.theme.resultTextColor)
    }
}