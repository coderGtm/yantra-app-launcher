package com.coderGtm.yantra.commands.calc

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "calc",
        helpTitle = terminal.activity.getString(R.string.cmd_calc_title),
        description = terminal.activity.getString(R.string.cmd_calc_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.clac_no_expression), terminal.theme.errorTextColor)
            return
        }
        val expression = command.removePrefix(args[0]).trim()
        try {
            val result = eval(expression, terminal.activity.baseContext)
            output(result.toString())
        }
        catch (e: RuntimeException) {
            output(e.message.toString(), terminal.theme.errorTextColor)
        }
    }
}