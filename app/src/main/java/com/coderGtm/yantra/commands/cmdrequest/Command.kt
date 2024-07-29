package com.coderGtm.yantra.commands.cmdrequest

import com.coderGtm.yantra.R
import com.coderGtm.yantra.blueprints.BaseCommand
import com.coderGtm.yantra.models.CommandMetadata
import com.coderGtm.yantra.openURL
import com.coderGtm.yantra.terminal.Terminal

class Command(terminal: Terminal) : BaseCommand(terminal) {
    override val metadata = CommandMetadata(
        name = "cmdrequest",
        helpTitle = "cmdrequest",
        description = terminal.activity.getString(R.string.cmd_cmdrequest_help)
    )

    override fun execute(command: String) {
        val args = command.split(" ")
        if (args.size > 1) {
            output(terminal.activity.getString(R.string.cmdrequest_no_args), terminal.theme.errorTextColor)
            return
        }
        openURL("https://github.com/coderGtm/yantra-app-launcher/issues/new?assignees=&labels=command-request&projects=&template=command-request.md&title=", this.terminal.activity)
    }
}