package com.coderGtm.yantra.commands.username

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "username",
        helpTitle = terminal.activity.getString(R.string.cmd_username_title),
        description = terminal.activity.getString(R.string.cmd_username_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size < 2) {
            output(terminal.activity.getString(R.string.please_specify_the_new_username), terminal.theme.errorTextColor)
            return
        }
        val newUsername = command.trim().removePrefix(args[0]).trim()
        terminal.preferenceObject.edit().putString("username",newUsername).apply()
        terminal.setPromptText()
        output(terminal.activity.getString(R.string.username_set_to, newUsername), terminal.theme.successTextColor)
    }
}